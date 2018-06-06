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
public final class HashKey {
    private List<Object> hashKey = new ArrayList<>();

    public HashKey(Object... keys) {
        if (keys == null)
            throw new IllegalArgumentException("Argument is Null.");
        with(Arrays.asList(keys));
    }

    public HashKey with(Object key) {
        if (key == null)
            throw new IllegalArgumentException("The value to include in HashKey does not allow Null.");
        this.hashKey.add(key);
        return this;
    }

    public HashKey with(List<?> keys) {
        if (keys == null)
            throw new IllegalArgumentException("Argument is Null.");
        checkContainsNull(keys);
        this.hashKey.addAll(keys);
        return this;
    }

    private void checkContainsNull(List<?> keys) {
        if (keys.contains(null))
            throw new IllegalArgumentException("The value to include in HashKey does not allow Null.");
    }

    public List<Object> getAll() {
        return ImmutableList.copyOf(hashKey);
    }

}
