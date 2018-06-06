package com.izj.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public final class DynamodbHolder {

    private DynamodbHolder() {
    }

    private static DynamoDB client;
    private static AmazonDynamoDB lowLevelClient;

    public static void set(AmazonDynamoDB db) {
        if (db != null) {
            DynamodbHolder.client = new DynamoDB(db);
            DynamodbHolder.lowLevelClient = db;
        }
    }

    public static DynamoDB get() {
        return client;
    }

    public static AmazonDynamoDB getLowLevel() {
        return lowLevelClient;
    }

}
