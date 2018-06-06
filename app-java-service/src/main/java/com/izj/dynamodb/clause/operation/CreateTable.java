package com.izj.dynamodb.clause.operation;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.izj.dynamodb.internal.metadata.EntityMetadata;

public interface CreateTable {

    <E> void execute(EntityMetadata meta, String tableSuffix, DynamoDB db);

}
