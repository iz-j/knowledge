package com.izj.dynamodb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.izj.dynamodb.clause.operation.DescribeTable;
import com.izj.dynamodb.clause.operation.UpdateTable.TableUpdate;

/**
 * Provide operations for DynamoDB management.
 *
 * @author iz-j
 *
 */
public interface DynamodbAdminClient {

    <E> void createTable(Class<E> entityClass);

    <E> void createTable(Class<E> entityClass, String tableSuffix);

    <E> void deleteTable(Class<E> entityClass);

    <E> void deleteTable(Class<E> entityClass, String tableSuffix);

    void deleteTable(String tableName);

    void updateTable(TableUpdate update);

    List<String> listTables();

    DescribeTable.Description describeTable(String tableName);

    Map<String, DescribeTable.Description> describeTables(Collection<String> tableNames);
}
