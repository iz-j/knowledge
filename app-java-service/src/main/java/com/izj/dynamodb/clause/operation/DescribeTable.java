package com.izj.dynamodb.clause.operation;

import java.util.Collection;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author iz-j
 *
 */
public interface DescribeTable {

    Map<String, Description> execute(AmazonDynamoDB db, Collection<String> tableNames);

    @Data
    @Builder
    public static class Description {
        private final String tableName;
        private final long tableSizeBytes;
        private final long itemCount;
        private final long readCapacityUnits;
        private final long writeCapacityUnits;
    }
}
