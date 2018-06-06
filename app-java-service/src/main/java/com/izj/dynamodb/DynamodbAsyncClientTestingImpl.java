package com.izj.dynamodb;

import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.springframework.util.Assert;

import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.operation.AsyncQuery;
import com.izj.dynamodb.clause.operation.Query;
import com.izj.dynamodb.clause.operation.QueryResult;

/**
 * AmazonDynamoDBLocal does not provide async client.<br>
 * So, turn on this client if async client not assigned.
 *
 * @author iz-j
 *
 */
class DynamodbAsyncClientTestingImpl implements DynamodbAsyncClient {

    private final DynamodbClient client;

    public DynamodbAsyncClientTestingImpl(DynamodbClient client) {
        this.client = client;
    }

    @Override
    public <I> AsyncQuery<I> query(Class<I> entityClass) {
        return new AsyncQueryTestingImpl<>(client.query(entityClass));
    }

    @Override
    public <I> AsyncQuery<I> query(Class<I> entityClass, String suffix) {
        return new AsyncQueryTestingImpl<>(client.query(entityClass, suffix));
    }

    private static class AsyncQueryTestingImpl<I> implements AsyncQuery<I> {
        private final Query<I> query;

        private AsyncQueryTestingImpl(Query<I> query) {
            this.query = query;
        }

        @Override
        public AsyncQuery<I> consistentRead() {
            query.consistentRead();
            return this;
        }

        @Override
        public AsyncQuery<I> asc() {
            query.asc();
            return this;
        }

        @Override
        public AsyncQuery<I> desc() {
            query.desc();
            return this;
        }

        private int limit = 300;

        @Override
        public AsyncQuery<I> limit(int limit) {
            Assert.isTrue(limit > 0, "Please set a value of 0 or more.");
            this.limit = limit;
            return this;
        }

        private Condition condition;

        @Override
        public AsyncQuery<I> condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        private Filter[] filter;

        @Override
        public AsyncQuery<I> filter(Filter... filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public Future<QueryResult<I>> items(HashKey hashKey) {
            return ConcurrentUtils
                .constantFuture(query.limit(limit).condition(condition).filter(filter).items(hashKey));
        }

    }
}
