package com.izj.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.operation.AsyncQuery;
import com.izj.dynamodb.clause.operation.impl.AsyncQueryImpl;
import com.izj.dynamodb.internal.metadata.EntityAnalyzer;

class DynamodbAsyncClientImpl implements DynamodbAsyncClient {

    private final AmazonDynamoDBAsync async;
    private final KeyResolver keyResolver;

    public DynamodbAsyncClientImpl(AmazonDynamoDBAsync async,
            KeyResolver keyResolver) {
        super();
        this.async = async;
        this.keyResolver = keyResolver;
    }

    @Override
    public <I> AsyncQuery<I> query(Class<I> entityClass) {
        return new AsyncQueryImpl<>(EntityAnalyzer.analyze(entityClass), async, keyResolver, null);
    }

    @Override
    public <I> AsyncQuery<I> query(Class<I> entityClass, String suffix) {
        return new AsyncQueryImpl<>(EntityAnalyzer.analyze(entityClass), async, keyResolver, suffix);

    }

}
