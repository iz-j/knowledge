package com.izj.dynamodb.clause.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.ToString;

/**
 *
 * @author ~~~~
 *
 */
@ToString
public final class RangeKey {
    private List<Object> rangeKey = new ArrayList<>();

    public RangeKey(Object... keys) {
        if (keys == null)
            throw new IllegalArgumentException("Argument is Null.");
        with(Arrays.asList(keys));
    }

    public RangeKey with(Object key) {
        if (key == null)
            throw new IllegalArgumentException("The value to include in RangeKey does not allow Null.");
        this.rangeKey.add(key);
        return this;
    }

    public RangeKey with(List<?> keys) {
        if (keys == null)
            throw new IllegalArgumentException("Argument is Null.");
        checkContainsNull(keys);
        this.rangeKey.addAll(keys);
        return this;
    }

    public List<Object> getAll() {
        return ImmutableList.copyOf(rangeKey);
    }

    private void checkContainsNull(List<?> keys) {
        if (keys.contains(null))
            throw new IllegalArgumentException("The value to include in HashKey does not allow Null.");
    }

}
