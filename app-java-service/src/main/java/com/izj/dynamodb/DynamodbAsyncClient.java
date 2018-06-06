package com.izj.dynamodb;

import com.izj.dynamodb.clause.operation.AsyncQuery;

public interface DynamodbAsyncClient {

    <I> AsyncQuery<I> query(Class<I> entityClass);

    <I> AsyncQuery<I> query(Class<I> entityClass, String suffix);

}
