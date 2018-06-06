package com.izj.dynamodb.clause.operation;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import lombok.Builder;
import lombok.Data;

public interface UpdateTable {

    void execute(AmazonDynamoDB db, TableUpdate update);

    @Data
    @Builder
    public static class TableUpdate {
        private final String tableName;
        private final long readCapacityUnits;
        private final long writeCapacityUnits;
    }
}
