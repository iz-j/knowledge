package com.izj.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.izj.dynamodb.clause.operation.BatchDelete;
import com.izj.dynamodb.clause.operation.BatchGet;
import com.izj.dynamodb.clause.operation.Delete;
import com.izj.dynamodb.clause.operation.Get;
import com.izj.dynamodb.clause.operation.Put;
import com.izj.dynamodb.clause.operation.Query;
import com.izj.dynamodb.clause.operation.Scan;
import com.izj.dynamodb.clause.operation.Update;

/**
 * Entity class based DynamoDB client.
 *
 * @author ~~~~
 *
 */
public interface DynamodbClient {

    <E> Scan<E> scan(Class<E> entityClass);

    <E> Scan<E> scan(Class<E> entityClass, String tableSuffix);

    <E> Put<E> put(Class<E> entityClass);

    <E> Put<E> put(Class<E> entityClass, String tableSuffix);

    <E> Get<E> get(Class<E> entityClass);

    <E> Get<E> get(Class<E> entityClass, String tableSuffix);

    <E> Query<E> query(Class<E> entityClass);

    <E> Query<E> query(Class<E> entityClass, String tableSuffix);

    <E> Update<E> update(Class<E> entityClass);

    <E> Update<E> update(Class<E> entityClass, String tableSuffix);

    <E> Delete delete(Class<E> entityClass);

    <E> Delete delete(Class<E> entityClass, String tableSuffix);

    <E> BatchGet<E> batchGet(Class<E> entityClass);

    <E> BatchGet<E> batchGet(Class<E> entityClass, String tableSuffix);

    <E> BatchDelete batchDelete(Class<E> entityClass);

    <E> BatchDelete batchDelete(Class<E> entityClass, String tableSuffix);

    <E> DynamodbAsyncClient async();

    DynamodbAdminClient forAdmin();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MultiTenantSupport multiTenantSupport;
        private AmazonDynamoDB db;

        public Builder multiTenantSupport(MultiTenantSupport multiTenantSupport) {
            this.multiTenantSupport = multiTenantSupport;
            return this;
        }

        public Builder db(AmazonDynamoDB db) {
            this.db = db;
            return this;
        }

        public DynamodbClient build() {
            return new DynamodbClientImpl(multiTenantSupport, db);
        }
    }

}
