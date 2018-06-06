package com.izj.dynamodb.internal.handler;

import java.util.Collection;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.izj.dynamodb.internal.spec.DeleteItemSpec;
import com.izj.dynamodb.internal.spec.PutItemSpec;
import com.izj.dynamodb.internal.spec.UpdateItemSpec;
import com.izj.dynamodb.transaction.DynamodbPseidoTransaction;

/**
 *
 * @author ~~~~
 *
 */

public interface UpdateHandler {

    public static class Factory {

        public static UpdateHandler transactionally(DynamodbPseidoTransaction tx) {
            return new UpdateHandlerTransactionally(tx);
        }

        public static UpdateHandler promptly(DynamoDB db) {
            return new UpdateHandlerPromptly(db);
        }

    }

    void put(String tableName, PutItemSpec spec);

    void batchPut(String tableName, Collection<PutItemSpec> specs);

    void update(String tableName, UpdateItemSpec spec);

    void delete(String tableName, DeleteItemSpec spec);

    void batchDelete(String tableName, Collection<DeleteItemSpec> specs);

}
