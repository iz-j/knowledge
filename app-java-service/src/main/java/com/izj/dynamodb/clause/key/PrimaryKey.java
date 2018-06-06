package com.izj.dynamodb.clause.key;

import lombok.ToString;

/**
 *
 * @author ~~~~
 *
 */
@ToString
public final class PrimaryKey {

    public final HashKey hashKey;
    public final RangeKey rangeKey;

    public PrimaryKey(HashKey hashKey, RangeKey rangeKey) {
        super();
        this.hashKey = hashKey;
        this.rangeKey = rangeKey;
    }

}
