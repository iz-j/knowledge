package com.izj.dynamodb.clause.operation;

import java.util.concurrent.Future;

import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.HashKey;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public interface AsyncQuery<I> {

    /**
     * Query with consistent read.
     *
     * @return query
     */
    AsyncQuery<I> consistentRead();

    /**
     * Set the sorting order in ascending order.
     *
     * @return query
     */
    AsyncQuery<I> asc();

    /**
     * Set the sorting order in descending order.
     *
     * @return query
     */
    AsyncQuery<I> desc();

    /**
     * Set the number of items to be retrieved.
     *
     * @param limit
     * @return query
     */
    AsyncQuery<I> limit(int limit);

    /**
     * Set the condition.
     *
     * @param condition
     * @return query
     */
    AsyncQuery<I> condition(Condition condition);

    /**
     * Set the filter.
     *
     * @param filter
     * @return query
     */
    AsyncQuery<I> filter(Filter... filter);

    /**
     * Return the future of query.<br>
     * It is a specification that acquires only 300 items.<br>
     * Please use {@link Query#limit(int)} if you want to further limit the number of acquisitions.
     *
     * @param hashKey
     * @return future of query
     */
    Future<QueryResult<I>> items(HashKey hashKey);

}
