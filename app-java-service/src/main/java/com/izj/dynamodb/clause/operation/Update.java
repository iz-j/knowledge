package com.izj.dynamodb.clause.operation;

import com.izj.dynamodb.clause.condition.Expected;
import com.izj.dynamodb.clause.condition.UpdateValues;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.RangeKey;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public interface Update<I> {
    Update<I> ifExists();

    Update<I> expected(Expected expected);

    /**
     * this method contains ifExists().
     */
    Update<I> throwExceptionIfNotUpdated();

    void item(I item);

    void item(HashKey hashKey, UpdateValues updateValues);

    void item(HashKey hashKey, RangeKey rangeKey, UpdateValues updateValues);

}
