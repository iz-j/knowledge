package com.izj.dynamodb.internal.spec;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.izj.dynamodb.clause.condition.Expected;

import lombok.Getter;

@Getter
public class DeleteItemSpec implements WriteItemSpec {
    private String hashKeyName;
    private Object hashKeyValue;
    private String rangeKeyName;
    private Object rangeKeyValue;
    private Expected expected;
    private boolean throwExceptionIfNotUpdated;

    public DeleteItemSpec withHashKey(String name, Object value) {
        this.hashKeyName = name;
        this.hashKeyValue = value;
        return this;
    }

    public DeleteItemSpec withRangeKey(String name, Object value) {
        this.rangeKeyName = name;
        this.rangeKeyValue = value;
        return this;
    }

    public DeleteItemSpec withThrowExceptionIfNotUpdated(boolean throwExceptionIfNotUpdated) {
        this.throwExceptionIfNotUpdated = throwExceptionIfNotUpdated;
        return this;
    }

    public DeleteItemSpec withExpected(Expected expected) {
        this.expected = expected;
        return this;
    }

    public com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec toNative() {
        com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec spec = new com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec();
        spec.withPrimaryKey(toPrimaryKey());
        if (expected != null) {
            spec.withExpected(expected.toNative());
        }
        return spec;
    }

    public PrimaryKey toPrimaryKey() {
        return rangeKeyValue == null ? new PrimaryKey(hashKeyName, hashKeyValue)
                : new PrimaryKey(hashKeyName, hashKeyValue, rangeKeyName, rangeKeyValue);
    }

}