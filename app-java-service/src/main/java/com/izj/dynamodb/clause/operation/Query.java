package com.izj.dynamodb.clause.operation;

import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.HashKey;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public interface Query<I> {

    /**
     * Return the query.<br>
     * It is a specification that acquires only 300 items.<br>
     * Please use {@link Query#limit(int)} if you want to further limit the number of acquisitions.
     *
     * @param hashKey
     * @return list of items
     */
    QueryResult<I> items(HashKey hashKey);

    /**
     * Return the query.<br>
     * It is a specification that acquires only 300 items.<br>
     * Please use {@link Query#limit(int)} if you want to further limit the number of acquisitions.
     *
     * @param hashKey
     * @return list of items
     */
    QueryResult<I> items(HashKey hashKey, String exclusiveStartKey);

    /**
     * Set the number of items to be retrieved.
     *
     * @param limit
     * @return query
     */
    Query<I> limit(int limit);

    /**
     * Set the condition.
     *
     * @param condition
     * @return query
     */
    Query<I> condition(Condition condition);

    /**
     * Set the filter.
     *
     * @param filter
     * @return query
     */
    Query<I> filter(Filter... filter);

    /**
     * Query with consistent read.
     *
     * @return query
     */
    Query<I> consistentRead();

    /**
     * Set the sorting order in ascending order.
     *
     * @return query
     */
    Query<I> asc();

    /**
     * Set the sorting order in descending order.
     *
     * @return query
     */
    Query<I> desc();

}
