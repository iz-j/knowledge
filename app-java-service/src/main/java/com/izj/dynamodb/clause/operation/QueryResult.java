package com.izj.dynamodb.clause.operation;

import java.util.List;

import lombok.Builder;
import lombok.ToString;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
@Builder
@ToString
public class QueryResult<I> {
    private final String lastEvaluatedKey;
    private final List<I> items;

    public String getLastEvaluateKey() {
        return lastEvaluatedKey;
    }

    /**
     * Returns the item list of the query result.
     *
     * @return list of items
     */
    public List<I> getItems() {
        return items;
    }
}
