package com.izj.dynamodb.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.google.common.collect.Lists;
import com.izj.dynamodb.exception.ConditionalUpdateFailedException;
import com.izj.dynamodb.exception.ThroughputExceedException;
import com.izj.dynamodb.internal.spec.DeleteItemSpec;
import com.izj.dynamodb.internal.spec.PutItemSpec;
import com.izj.dynamodb.internal.spec.UpdateItemSpec;
import com.izj.dynamodb.internal.spec.WriteItemSpec;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 */
@Slf4j
public final class DynamodbPseidoTransaction {

    @Override
    public String toString() {
        return "DynamodbPseidoTransaction [name=" + name + "]";
    }

    private final AmazonDynamoDB lowLevelDb;
    private final DynamoDB db;
    private final String name;

    private Map.Entry<String, PutItemSpec> putItemSpecPrimary = null;
    private Map.Entry<String, UpdateItemSpec> updateItemSpecPrimary = null;
    private Map.Entry<String, DeleteItemSpec> deleteItemSpecPrimary = null;

    private final Map<String, List<PutItemSpec>> putItemSpecWithConditionMap = new ConcurrentHashMap<>();
    private final Map<String, List<DeleteItemSpec>> deleteItemSpecWithConditionMap = new ConcurrentHashMap<>();
    private final Map<String, List<WriteItemSpec>> writeItemSpecMap = new ConcurrentHashMap<>();
    private final Map<String, List<UpdateItemSpec>> updateItemSpecMap = new ConcurrentHashMap<>();

    DynamodbPseidoTransaction(String name, AmazonDynamoDB db) {
        super();
        this.name = name;
        this.lowLevelDb = db;
        this.db = new DynamoDB(db);
    }

    public void batchWrite(String tableName, Collection<? extends WriteItemSpec> specs) {
        if (!writeItemSpecMap.containsKey(tableName)) {
            writeItemSpecMap.put(tableName, new ArrayList<>());
        }
        writeItemSpecMap.get(tableName).addAll(specs);
    }

    public void put(String tableName, PutItemSpec spec) {
        if (spec.getCondition() != null) {
            if (spec.isThrowExceptionIfExists()) {
                if (putItemSpecPrimary != null || updateItemSpecPrimary != null || deleteItemSpecPrimary != null) {
                    throw new IllegalStateException("");
                }
                this.putItemSpecPrimary = Pair.of(tableName, spec);
            } else {
                if (!putItemSpecWithConditionMap.containsKey(tableName)) {
                    putItemSpecWithConditionMap.put(tableName, new ArrayList<>());
                }
                putItemSpecWithConditionMap.get(tableName).add(spec);
            }
        } else {
            if (!writeItemSpecMap.containsKey(tableName)) {
                writeItemSpecMap.put(tableName, new ArrayList<>());
            }
            writeItemSpecMap.get(tableName).add(spec);
        }
    }

    public void delete(String tableName, com.izj.dynamodb.internal.spec.DeleteItemSpec spec) {
        if (spec.getExpected() != null && spec.isThrowExceptionIfNotUpdated()) {
            if (spec.isThrowExceptionIfNotUpdated()) {
                if (putItemSpecPrimary != null || updateItemSpecPrimary != null || deleteItemSpecPrimary != null) {
                    throw new UnsupportedOperationException("Conditional updates can only once per transaction.");
                }
                this.deleteItemSpecPrimary = Pair.of(tableName, spec);
            } else {
                if (!deleteItemSpecWithConditionMap.containsKey(tableName)) {
                    deleteItemSpecWithConditionMap.put(tableName, new ArrayList<>());
                }
                deleteItemSpecWithConditionMap.get(tableName).add(spec);
            }
        } else {
            if (!writeItemSpecMap.containsKey(tableName)) {
                writeItemSpecMap.put(tableName, new ArrayList<>());
            }
            writeItemSpecMap.get(tableName).add(spec);
        }
    }

    public void update(String tableName, UpdateItemSpec spec) {
        if (CollectionUtils.isNotEmpty(spec.getExpecteds()) && spec.isThrowExceptionIfNotUpdated()) {
            if (putItemSpecPrimary != null || updateItemSpecPrimary != null || deleteItemSpecPrimary != null) {
                throw new UnsupportedOperationException("Conditional updates can only once per transaction.");
            }
            this.updateItemSpecPrimary = Pair.of(tableName, spec);
        } else {
            if (!updateItemSpecMap.containsKey(tableName)) {
                updateItemSpecMap.put(tableName, new ArrayList<>());
            }
            updateItemSpecMap.get(tableName).add(spec);
        }
    }

    public DynamoDB getDb() {
        return db;
    }

    public AmazonDynamoDB getLowLebelDb() {
        return lowLevelDb;
    }

    public void flush() {
        if (log.isDebugEnabled()) {
            log.debug("Transaction#{} flushing...", this.name);
        }
        StopWatch sw = new StopWatch();
        sw.start();
        flushPrimaryItems();
        updateItems();
        putItems();
        deleteItems();
        batchWriteItems();
        sw.stop();
        if (log.isDebugEnabled()) {
            log.debug("Transaction#{} flush complete. execution time: {} ms", this.name, sw.getTime());
        }
    }

    private void flushPrimaryItems() {
        StopWatch sw = new StopWatch();
        if (updateItemSpecPrimary != null) {
            sw.start();
            Table table = this.db.getTable(updateItemSpecPrimary.getKey());
            try {
                table.updateItem(updateItemSpecPrimary.getValue().toNative());
            } catch (ConditionalCheckFailedException ex) {
                throw new ConditionalUpdateFailedException(table.getTableName(), ex);
            } catch (ProvisionedThroughputExceededException ex) {
                throw new ThroughputExceedException(ex, table.getTableName(), true);
            } catch (AmazonDynamoDBException e) {
                log.error("Failed update table#{}.", table.getTableName());
                throw e;
            }
            sw.stop();
            if (log.isDebugEnabled())
                log.debug("Updated 1 item with condition. execution time: {} ms", sw.getTime());
            return;
        }
        if (putItemSpecPrimary != null) {
            sw.start();
            Table table = this.db.getTable(putItemSpecPrimary.getKey());
            try {
                table.putItem(putItemSpecPrimary.getValue().toNative());
            } catch (ConditionalCheckFailedException ex) {
                throw new ConditionalUpdateFailedException(table.getTableName(), ex);
            } catch (ProvisionedThroughputExceededException ex) {
                throw new ThroughputExceedException(ex, table.getTableName(), true);
            } catch (AmazonDynamoDBException e) {
                log.error("Failed update table#{}.", table.getTableName());
                throw e;
            }
            sw.stop();
            if (log.isDebugEnabled())
                log.debug("Put 1 item with condition. execution time: {} ms", sw.getTime());
            return;
        }
        if (deleteItemSpecPrimary != null) {
            sw.start();
            Table table = this.db.getTable(deleteItemSpecPrimary.getKey());
            try {
                table.deleteItem(deleteItemSpecPrimary.getValue().toNative());
            } catch (ConditionalCheckFailedException ex) {
                throw new ConditionalUpdateFailedException(table.getTableName(), ex);
            } catch (ProvisionedThroughputExceededException ex) {
                throw new ThroughputExceedException(ex, table.getTableName(), true);
            } catch (AmazonDynamoDBException e) {
                log.error("Failed update table#{}.", table.getTableName());
                throw e;
            }
            sw.stop();
            if (log.isDebugEnabled())
                log.debug("Deleted 1 item with condition. execution time: {} ms", sw.getTime());
            return;
        }
    }

    private void updateItems() {
        // TODO 非同期にしたい！！！けどRequestを作る必要がある・・・
        StopWatch sw = new StopWatch();
        sw.start();
        updateItemSpecMap.entrySet().stream().forEach(spec -> {
            Table table = this.db.getTable(spec.getKey());
            spec.getValue().stream().forEach(update -> {
                try {
                    final com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec s = update.toNative();
                    table.updateItem(s);
                } catch (ConditionalCheckFailedException ex) {
                    log.debug("Occured ConditionalCheckFailedException, but ignored. Table:{}",
                            table.getTableName());
                } catch (ProvisionedThroughputExceededException ex) {
                    throw new ThroughputExceedException(ex, table.getTableName(), true);
                } catch (AmazonDynamoDBException e) {
                    log.error("Failed update table#{}.", table.getTableName());
                    throw e;
                }
            });
        });
        sw.stop();
        if (log.isDebugEnabled()) {
            int itemCount = updateItemSpecMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
            if (itemCount > 0)
                log.debug("Updated {} items. execution time: {} ms", itemCount, sw.getTime());
        }
    }

    private void putItems() {
        StopWatch sw = new StopWatch();
        sw.start();
        putItemSpecWithConditionMap.entrySet().stream().forEach(spec -> {
            Table table = this.db.getTable(spec.getKey());
            spec.getValue().stream().forEach(put -> {
                try {
                    table.putItem(put.toNative());
                } catch (ConditionalCheckFailedException ex) {
                    log.debug("Occured ConditionalCheckFailedException, but ignored. Table:{}",
                            table.getTableName());
                } catch (ProvisionedThroughputExceededException ex) {
                    throw new ThroughputExceedException(ex, table.getTableName(), true);
                } catch (AmazonDynamoDBException e) {
                    log.error("Failed update table#{}.", table.getTableName());
                    throw e;
                }
            });
        });
        sw.stop();
        if (log.isDebugEnabled()) {
            int itemCount = putItemSpecWithConditionMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
            if (itemCount > 0)
                log.debug("Put {} items. execution time: {} ms", itemCount, sw.getTime());
        }
    }

    private void deleteItems() {
        StopWatch sw = new StopWatch();
        sw.start();
        deleteItemSpecWithConditionMap.entrySet().stream().forEach(spec -> {
            Table table = this.db.getTable(spec.getKey());
            spec.getValue().stream().forEach(delete -> {
                try {
                    table.deleteItem(delete.toNative());
                } catch (ConditionalCheckFailedException ex) {
                    log.debug("Occured ConditionalCheckFailedException, but ignored. :{}", ex.getMessage());
                } catch (ProvisionedThroughputExceededException ex) {
                    throw new ThroughputExceedException(ex, table.getTableName(), true);
                } catch (AmazonDynamoDBException e) {
                    log.error("Failed update table#{}.", table.getTableName());
                    throw e;
                }
            });
        });
        sw.stop();
        if (log.isDebugEnabled()) {
            int itemCount = deleteItemSpecWithConditionMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
            if (itemCount > 0)
                log.debug("Delete {} items. execution time: {} ms", itemCount, sw.getTime());
        }
    }

    private void batchWriteItems() {
        Map.Entry<Integer, List<BatchWriteItemSpec>> batchWriteItemSpecs = createBatchWriteSpec();
        if (CollectionUtils.isNotEmpty(batchWriteItemSpecs.getValue())) {
            StopWatch sw = new StopWatch();
            sw.start();
            batchWriteItemSpecs.getValue().stream().forEach(spec -> {
                try {
                    BatchWriteItemOutcome outcome = this.db.batchWriteItem(spec);
                    do {
                        Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();
                        if (outcome.getUnprocessedItems().size() == 0) {
                            if (log.isTraceEnabled()) {
                                log.trace("No unprocessed items found");
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace("Retrieving the unprocessed items");
                            }
                            outcome = this.db.batchWriteItemUnprocessed(unprocessedItems);
                        }
                    } while (outcome.getUnprocessedItems().size() > 0);
                } catch (ProvisionedThroughputExceededException ex) {
                    throw new ThroughputExceedException(ex);
                }
            });
            sw.stop();
            if (log.isDebugEnabled()) {
                log.debug("Batch wrote {} items with {} requests. execution time: {} ms", batchWriteItemSpecs.getKey(),
                        batchWriteItemSpecs.getValue().size(),
                        sw.getTime());
            }
        }
    }

    private Map.Entry<Integer, List<BatchWriteItemSpec>> createBatchWriteSpec() {
        List<Map.Entry<String, WriteItemSpec>> writeItemSpecs = writeItemSpecMap
            .entrySet()
            .stream()
            .flatMap(entry -> {
                String table = entry.getKey();
                Stream<WriteItemSpec> specs = entry.getValue().stream();
                Stream<Map.Entry<String, WriteItemSpec>> mapped = specs.map(spec -> Pair.of(table, spec));
                return mapped;
            })
            .collect(Collectors.toList());

        List<List<Map.Entry<String, WriteItemSpec>>> partitions = Lists.partition(writeItemSpecs, 25);
        List<BatchWriteItemSpec> batchWriteSpecs = partitions.stream().map(partition -> {
            Map<String, List<WriteItemSpec>> partitionByTable = partition
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(
                        Map.Entry::getValue,
                        Collectors.toList())));
            TableWriteItems[] tableWriteItemsArr = partitionByTable.entrySet().stream().map(entry -> {
                TableWriteItems tableWriteItems = new TableWriteItems(entry.getKey());
                List<WriteItemSpec> specs = entry.getValue();
                List<DeleteItemSpec> deletes = specs
                    .stream()
                    .filter(spec -> spec instanceof DeleteItemSpec)
                    .map(spec -> (DeleteItemSpec)spec)
                    .collect(Collectors.toList());
                List<PutItemSpec> puts = specs
                    .stream()
                    .filter(spec -> spec instanceof PutItemSpec)
                    .map(spec -> (PutItemSpec)spec)
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(deletes)) {
                    tableWriteItems.withPrimaryKeysToDelete(deletes
                        .stream()
                        .map(DeleteItemSpec::toPrimaryKey)
                        .toArray(PrimaryKey[]::new));
                }
                if (CollectionUtils.isNotEmpty(puts)) {
                    tableWriteItems.withItemsToPut(puts
                        .stream()
                        .map(PutItemSpec::getItem)
                        .collect(Collectors.toList()));
                }
                return tableWriteItems;
            }).toArray(TableWriteItems[]::new);
            return new BatchWriteItemSpec().withTableWriteItems(tableWriteItemsArr);
        }).collect(Collectors.toList());
        return Pair.of(writeItemSpecs.size(), batchWriteSpecs);
    }

}
