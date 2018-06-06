package com.izj.dynamodb.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.google.common.collect.Lists;
import com.izj.dynamodb.exception.ConditionalUpdateFailedException;
import com.izj.dynamodb.internal.spec.DeleteItemSpec;
import com.izj.dynamodb.internal.spec.PutItemSpec;
import com.izj.dynamodb.internal.spec.UpdateItemSpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateHandlerPromptly implements UpdateHandler {
    private final DynamoDB db;

    UpdateHandlerPromptly(DynamoDB db) {
        super();
        this.db = db;
    }

    @Override
    public void update(String tableName, UpdateItemSpec spec) {
        try {
            this.db.getTable(tableName).updateItem(spec.toNative());
        } catch (ConditionalCheckFailedException ex) {
            if (spec.isThrowExceptionIfNotUpdated()) {
                throw new ConditionalUpdateFailedException(ex);
            } else if (log.isDebugEnabled()) {
                log.debug("Occured ConditionalCheckFailedException, but ignored.: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void delete(String tableName, DeleteItemSpec spec) {
        try {
            this.db.getTable(tableName).deleteItem(spec.toNative());
        } catch (ConditionalCheckFailedException ex) {
            if (spec.isThrowExceptionIfNotUpdated()) {
                throw new ConditionalUpdateFailedException(ex);
            } else if (log.isDebugEnabled()) {
                log.debug("Occured ConditionalCheckFailedException, but ignored.: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void put(String tableName, PutItemSpec spec) {
        try {
            this.db.getTable(tableName).putItem(spec.toNative());
        } catch (ConditionalCheckFailedException ex) {
            if (spec.isThrowExceptionIfExists()) {
                throw new ConditionalUpdateFailedException(ex);
            } else if (log.isDebugEnabled()) {
                log.debug("Occured ConditionalCheckFailedException, but ignored.: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void batchDelete(String tableName, Collection<DeleteItemSpec> specs) {
        List<List<DeleteItemSpec>> partitions = Lists.partition(new ArrayList<>(specs), 25);
        partitions.stream().forEach(partition -> {
            this.db.batchWriteItem(new TableWriteItems(tableName)
                .withPrimaryKeysToDelete(
                        partition.stream().map(spec -> spec.toPrimaryKey()).toArray(PrimaryKey[]::new)));
        });
    }

    @Override
    public void batchPut(String tableName, Collection<PutItemSpec> specs) {
        List<List<PutItemSpec>> partitions = Lists.partition(new ArrayList<>(specs), 25);
        partitions.stream().forEach(partition -> {
            this.db.batchWriteItem(new TableWriteItems(tableName)
                .withItemsToPut(partition.stream().map(spec -> spec.getItem()).toArray(Item[]::new)));
        });

    }

}
