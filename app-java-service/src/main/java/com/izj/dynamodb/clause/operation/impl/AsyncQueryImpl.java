package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.operation.AsyncQuery;
import com.izj.dynamodb.internal.metadata.EntityMapper;
import com.izj.dynamodb.internal.metadata.EntityMetadata;

public class AsyncQueryImpl<I> implements AsyncQuery<I> {
    private final EntityMetadata metadata;
    private final AmazonDynamoDBAsync async;
    private final KeyResolver keyResolver;
    private final String suffix;

    private final static int MAX_RESULT_SIZE = 300;
    private int limit = MAX_RESULT_SIZE;
    private boolean consistentRead = false;
    private boolean asc = true;
    private Condition condition;
    private List<Filter> filter;

    public AsyncQueryImpl(EntityMetadata metadata, AmazonDynamoDBAsync async,
            KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.async = async;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public Future<com.izj.dynamodb.clause.operation.QueryResult<I>> items(HashKey hashKey) {
        ExpressionAndValueMap hk = Condition
            .eq(keyResolver.resolve(hashKey))
            .toExpressionAndAttributeValues(metadata.hashKey.name, null, keyResolver);
        StringBuilder expression = new StringBuilder(hk.getExpression());
        Map<String, Object> values = hk.getValueMap();

        if (condition != null) {
            ExpressionAndValueMap c = condition.toExpressionAndAttributeValues(metadata.rangeKey.name,
                    metadata.rangeKey.fieldAndDigits,
                    keyResolver);
            expression.append(" AND ").append(c.getExpression());
            values.putAll(c.getValueMap());
        }

        List<String> filterExpressions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filter)) {
            filter.forEach(f -> {
                ExpressionAndValueMap ea = f
                    .toExpressionAndAttributeValues(metadata.attributes.get(f.getAttributeName()));
                filterExpressions.add(ea.getExpression());
                values.putAll(ea.getValueMap());
            });
        }

        Future<QueryResult> future = async.queryAsync(new QueryRequest(metadata.table.getTableName(suffix))
            .withConsistentRead(consistentRead)
            .withScanIndexForward(asc)
            .withLimit(limit)
            .withKeyConditionExpression(expression.toString())
            .withFilterExpression(
                    CollectionUtils.isEmpty(filterExpressions) ? null : String.join(" and ", filterExpressions))
            .withExpressionAttributeValues(values.entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> InternalUtils.toAttributeValue(e.getValue())))));

        return new QueryResultFuture<I>(future, metadata, keyResolver);
    }

    @Override
    public AsyncQuery<I> consistentRead() {
        this.consistentRead = true;
        return this;
    }

    private static class QueryResultFuture<I> implements Future<com.izj.dynamodb.clause.operation.QueryResult<I>> {
        private final Future<QueryResult> future;
        private final EntityMetadata metadata;
        private final KeyResolver keyResolver;

        private QueryResultFuture(Future<QueryResult> future, EntityMetadata metadata, KeyResolver keyResolver) {
            super();
            this.future = future;
            this.metadata = metadata;
            this.keyResolver = keyResolver;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return this.future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return this.future.isDone();
        }

        @Override
        public com.izj.dynamodb.clause.operation.QueryResult<I> get()
                throws InterruptedException, ExecutionException {
            QueryResult result = this.future.get();
            return createQueryResult(result);
        }

        @Override
        public com.izj.dynamodb.clause.operation.QueryResult<I> get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            QueryResult result = this.future.get(timeout, unit);
            return createQueryResult(result);
        }

        @SuppressWarnings("unchecked")
        private com.izj.dynamodb.clause.operation.QueryResult<I> createQueryResult(QueryResult result) {
            return (com.izj.dynamodb.clause.operation.QueryResult<I>)com.izj.dynamodb.clause.operation.QueryResult
                .builder()
                .items(result
                    .getItems()
                    .stream()
                    .map(item -> EntityMapper.map(metadata, InternalUtils.toSimpleMapValue(item),
                            keyResolver))
                    .collect(Collectors.toList()))
                .lastEvaluatedKey(KeyConverter.toStringKey(result.getLastEvaluatedKey()))
                .build();
        }

    }

    @Override
    public AsyncQuery<I> asc() {
        this.asc = true;
        return this;
    }

    @Override
    public AsyncQuery<I> desc() {
        this.asc = false;
        return this;
    }

    @Override
    public AsyncQuery<I> limit(int limit) {
        this.limit = limit > MAX_RESULT_SIZE ? MAX_RESULT_SIZE : limit;
        return this;
    }

    @Override
    public AsyncQuery<I> condition(Condition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public AsyncQuery<I> filter(Filter... filter) {
        if (filter != null) {
            this.filter = Arrays.asList(filter);
        }
        return this;
    }

}
