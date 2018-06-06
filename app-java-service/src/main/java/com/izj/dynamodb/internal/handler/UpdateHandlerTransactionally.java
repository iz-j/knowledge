package com.izj.dynamodb.internal.handler;

import java.util.Collection;

import com.izj.dynamodb.internal.spec.DeleteItemSpec;
import com.izj.dynamodb.internal.spec.PutItemSpec;
import com.izj.dynamodb.internal.spec.UpdateItemSpec;
import com.izj.dynamodb.transaction.DynamodbPseidoTransaction;

public class UpdateHandlerTransactionally implements UpdateHandler {
    private final DynamodbPseidoTransaction tx;

    UpdateHandlerTransactionally(DynamodbPseidoTransaction tx) {
        super();
        this.tx = tx;
    }

    @Override
    public void update(String tableName, UpdateItemSpec spec) {
        this.tx.update(tableName, spec);
    }

    @Override
    public void delete(String tableName, DeleteItemSpec spec) {
        this.tx.delete(tableName, spec);
    }

    @Override
    public void put(String tableName, PutItemSpec spec) {
        this.tx.put(tableName, spec);
    }

    @Override
    public void batchDelete(String tableName, Collection<DeleteItemSpec> specs) {
        this.tx.batchWrite(tableName, specs);
    }

    @Override
    public void batchPut(String tableName, Collection<PutItemSpec> specs) {
        this.tx.batchWrite(tableName, specs);
    }

}
