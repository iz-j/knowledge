package com.izj.dynamodb.clause.operation.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.izj.dynamodb.clause.operation.DeleteTable;
import com.izj.dynamodb.internal.metadata.EntityMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteTableImpl implements DeleteTable {

    @Override
    public void execute(EntityMetadata meta, String tableSuffix, DynamoDB db) {
        this.execute(meta.table.getTableName(tableSuffix), db);
    }

    @Override
    public void execute(String tableName, DynamoDB db) {
        com.amazonaws.services.dynamodbv2.document.Table table = db.getTable(tableName);
        try {
            table.delete();
            if (log.isDebugEnabled()) {
                log.debug("Waiting for deleting {}...", table.getTableName());
            }
            table.waitForDelete();
            if (log.isDebugEnabled()) {
                log.debug("{} table deletion completed!", table.getTableName());
            }
        } catch (InterruptedException ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Fatal delete {} table.: {}", table.getTableName(), ignore.getMessage());
            }
        }
    }

}
