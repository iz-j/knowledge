package com.izj.dynamodb.internal.spec;

import com.amazonaws.services.dynamodbv2.document.Item;

import lombok.Getter;

@Getter
public class PutItemSpec implements WriteItemSpec {

    private Item item;
    private String condition;
    private boolean throwExceptionIfExists;

    public PutItemSpec withItem(Item item) {
        this.item = item;
        return this;
    }

    public PutItemSpec withCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public PutItemSpec withThrowExceptionIfExists(boolean throwExceptionIfExists) {
        this.throwExceptionIfExists = throwExceptionIfExists;
        return this;
    }

    public com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec toNative() {
        return new com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec()
            .withItem(item)
            .withConditionExpression(condition);
    }
}
