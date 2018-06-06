package com.izj.dynamodb.clause.operation.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.izj.dynamodb.clause.operation.UpdateTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateTableImpl implements UpdateTable {

    @Override
    public void execute(AmazonDynamoDB db, TableUpdate update) {
        log.info("Update Table {}", update);

        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
            .withReadCapacityUnits(update.getReadCapacityUnits())
            .withWriteCapacityUnits(update.getWriteCapacityUnits());

        UpdateTableRequest request = new UpdateTableRequest()
            .withTableName(update.getTableName())
            .withProvisionedThroughput(provisionedThroughput);

        if (AmazonDynamoDBAsync.class.isAssignableFrom(db.getClass())) {
            ((AmazonDynamoDBAsync)db).updateTableAsync(request);
        } else {
            db.updateTable(request);
        }
    }
}
