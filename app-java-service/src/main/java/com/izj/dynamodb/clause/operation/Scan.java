package com.izj.dynamodb.clause.operation;

import java.util.Collection;

import com.izj.dynamodb.clause.condition.Filter;

public interface Scan<I> {

    Scan<I> filter(Filter... filters);

    Scan<I> filter(Collection<Filter> filters);

    Scan<I> limit(int limit);

    Scan<I> consistentRead();

    ScanResult<I> items(String exclusiveStartKey);

    ScanResult<I> items();

}
