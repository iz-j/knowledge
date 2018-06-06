package com.izj.dynamodb.clause.operation;

import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.RangeKey;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public interface Get<I> {

    I item(HashKey hashKey);

    I item(HashKey hashKey, RangeKey rangeKey);

    Get<I> consistentRead();

}
