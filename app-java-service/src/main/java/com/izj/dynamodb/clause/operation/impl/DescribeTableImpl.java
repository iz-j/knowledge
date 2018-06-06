package com.izj.dynamodb.clause.operation.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.izj.dynamodb.clause.operation.DescribeTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DescribeTableImpl implements DescribeTable {
    @Override
    public Map<String, Description> execute(AmazonDynamoDB db, Collection<String> tableNames) {
        List<com.amazonaws.services.dynamodbv2.model.TableDescription> descriptions;

        if (AmazonDynamoDBAsync.class.isAssignableFrom(db.getClass())) {
            descriptions = describe((AmazonDynamoDBAsync)db, tableNames);
        } else {
            descriptions = describe(db, tableNames);
        }
        log.trace("{} tables were described.", tableNames.size());

        return descriptions.stream().map(td -> {
            return Description
                .builder()
                .tableName(td.getTableName())
                .tableSizeBytes(td.getTableSizeBytes())
                .itemCount(td.getItemCount())
                .readCapacityUnits(td.getProvisionedThroughput().getReadCapacityUnits())
                .writeCapacityUnits(td.getProvisionedThroughput().getWriteCapacityUnits())
                .build();
        }).collect(Collectors.toMap(d -> d.getTableName(), d -> d));
    }

    private List<com.amazonaws.services.dynamodbv2.model.TableDescription> describe(AmazonDynamoDB db,
            Collection<String> tableNames) {
        return tableNames.stream().map(tableName -> {
            log.trace("Retrieving description of {} ...", tableName);
            DescribeTableResult res = db.describeTable(tableName);
            return res.getTable();
        }).collect(Collectors.toList());
    }

    private List<com.amazonaws.services.dynamodbv2.model.TableDescription> describe(AmazonDynamoDBAsync db,
            Collection<String> tableNames) {

        log.trace("Retrieving descriptions by async ...");
        List<Future<DescribeTableResult>> futures = tableNames.stream().map(tableName -> {
            return db.describeTableAsync(tableName);
        }).collect(Collectors.toList());

        return futures.stream().map(future -> {
            try {
                return future.get().getTable();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(Collectors.toList());
    }
}
