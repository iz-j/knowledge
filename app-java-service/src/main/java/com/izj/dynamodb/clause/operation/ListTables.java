package com.izj.dynamodb.clause.operation;

import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 *
 * @author iz-j
 *
 */
public interface ListTables {

    List<String> execute(AmazonDynamoDB db);

}
