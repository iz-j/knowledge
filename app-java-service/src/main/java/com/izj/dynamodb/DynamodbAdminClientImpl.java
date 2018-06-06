package com.izj.dynamodb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.izj.dynamodb.clause.operation.DescribeTable;
import com.izj.dynamodb.clause.operation.DescribeTable.Description;
import com.izj.dynamodb.clause.operation.UpdateTable.TableUpdate;
import com.izj.dynamodb.clause.operation.impl.CreateTableImpl;
import com.izj.dynamodb.clause.operation.impl.DeleteTableImpl;
import com.izj.dynamodb.clause.operation.impl.DescribeTableImpl;
import com.izj.dynamodb.clause.operation.impl.ListTablesImpl;
import com.izj.dynamodb.clause.operation.impl.UpdateTableImpl;
import com.izj.dynamodb.internal.metadata.EntityAnalyzer;

/**
 *
 * @author iz-j
 *
 */
public class DynamodbAdminClientImpl implements DynamodbAdminClient {

    public DynamodbAdminClientImpl() {
    }

    @Override
    public <E> void createTable(Class<E> entityClass) {
        createTable(entityClass, null);
    }

    @Override
    public <E> void createTable(Class<E> entityClass, String tableSuffix) {
        new CreateTableImpl().execute(EntityAnalyzer.analyze(entityClass), tableSuffix, DynamodbHolder.get());
    }

    @Override
    public <E> void deleteTable(Class<E> entityClass) {
        deleteTable(entityClass, null);
    }

    @Override
    public <E> void deleteTable(Class<E> entityClass, String tableSuffix) {
        new DeleteTableImpl().execute(EntityAnalyzer.analyze(entityClass), tableSuffix, DynamodbHolder.get());
    }

    @Override
    public void deleteTable(String tableName) {
        new DeleteTableImpl().execute(tableName, DynamodbHolder.get());
    }

    @Override
    public void updateTable(TableUpdate update) {
        new UpdateTableImpl().execute(DynamodbHolder.getLowLevel(), update);
    }

    @Override
    public List<String> listTables() {
        return new ListTablesImpl().execute(DynamodbHolder.getLowLevel());
    }

    @Override
    public DescribeTable.Description describeTable(String tableName) {
        Map<String, Description> res = this.describeTables(Arrays.asList(tableName));
        return res.get(tableName);
    }

    @Override
    public Map<String, Description> describeTables(Collection<String> tableNames) {
        return new DescribeTableImpl().execute(DynamodbHolder.getLowLevel(), tableNames);
    }

}
