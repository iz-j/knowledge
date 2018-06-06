package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.PrimaryKey;
import com.izj.dynamodb.clause.operation.BatchGet;
import com.izj.dynamodb.exception.ThroughputExceedException;
import com.izj.dynamodb.internal.metadata.EntityMapper;
import com.izj.dynamodb.internal.metadata.EntityMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchGetImpl<I> implements BatchGet<I> {
    private boolean consistentRead = false;
    private final EntityMetadata metadata;
    private final DynamoDB db;
    private final KeyResolver keyResolver;
    private final String suffix;

    public BatchGetImpl(EntityMetadata metadata, DynamoDB db, KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.db = db;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public List<I> items(Collection<PrimaryKey> keys) {
        Assert.notNull(metadata.rangeKey, "RangeKey also needs to be specified.");
        return _items(keys);
    }

    @Override
    public List<I> itemsByHashOnlyPrimaryKeys(Collection<HashKey> hashKeys) {
        Assert.isNull(metadata.rangeKey, "There is no RangeKey in this table.");
        return _items(hashKeys.stream().map(k -> new PrimaryKey(k, null)).collect(Collectors.toList()));
    }

    private List<I> _items(Collection<PrimaryKey> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            log.debug("Because Keys is empty, it did not get results.");
            return Collections.emptyList();
        }
        TableKeysAndAttributes table = new TableKeysAndAttributes(metadata.table.getTableName(suffix))
            .withConsistentRead(consistentRead);
        try {
            if (metadata.rangeKey != null) {
                keys.stream().forEach(pk -> {
                    HashKey hk = keyResolver.resolve(pk.hashKey);
                    table.addHashAndRangePrimaryKey(metadata.hashKey.name,
                            keyResolver.toAttributeValue(hk),
                            metadata.rangeKey.name,
                            keyResolver.toAttributeValue(pk.rangeKey, metadata.rangeKey.fieldAndDigits));
                });
            } else {
                Object[] hashKeys = keys
                    .stream()
                    .map(pk -> {
                        HashKey hk = keyResolver.resolve(pk.hashKey);
                        return keyResolver.toAttributeValue(hk);
                    })
                    .toArray(Object[]::new);
                table.addHashOnlyPrimaryKeys(metadata.hashKey.name, hashKeys);
            }
            BatchGetItemOutcome outcome = db.batchGetItem(table);
            Map<String, KeysAndAttributes> unprocessed = null;
            List<I> result = new ArrayList<>();
            do {
                List<Item> items = outcome.getTableItems().get(metadata.table.getTableName(suffix));
                for (Item item : items) {
                    result.add(EntityMapper.map(metadata, item.asMap(), keyResolver));
                }
                unprocessed = outcome.getUnprocessedKeys();
                if (!unprocessed.isEmpty()) {
                    outcome = db.batchGetItemUnprocessed(unprocessed);
                }
            } while (!unprocessed.isEmpty());

            return result;
        } catch (Exception e) {
            if (e instanceof ProvisionedThroughputExceededException) {
                throw new ThroughputExceedException((ProvisionedThroughputExceededException)e,
                        table.getTableName(), false);
            }
            log.error("Exception occured.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public BatchGet<I> consistentRead() {
        this.consistentRead = true;
        return this;
    }

}
