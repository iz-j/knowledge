package com.izj.dynamodb.clause.operation;

import com.izj.dynamodb.clause.condition.Expected;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.RangeKey;

public interface Delete {

    Delete expected(Expected expected);

    Delete throwExceptionIfNotUpdated();

    void item(HashKey hashKey);

    void item(HashKey hashKey, RangeKey rangeKey);

}
