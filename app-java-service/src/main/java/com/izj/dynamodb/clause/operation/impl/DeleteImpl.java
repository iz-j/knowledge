package com.izj.dynamodb.clause.operation.impl;

import org.springframework.util.Assert;

import com.izj.dynamodb.clause.condition.Expected;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.clause.operation.Delete;
import com.izj.dynamodb.internal.handler.UpdateHandler;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.spec.DeleteItemSpec;

public class DeleteImpl implements Delete {
    private final EntityMetadata metadata;
    private final UpdateHandler updateHandler;
    private final KeyResolver keyResolver;
    private final String suffix;

    private Expected expected;
    private boolean throwExceptionIfNotUpdated;

    public DeleteImpl(EntityMetadata metadata, UpdateHandler updateHandler, KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.updateHandler = updateHandler;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public void item(HashKey hashKey) {
        Assert.isNull(metadata.rangeKey, "RangeKey also needs to be specified.");
        this._item(hashKey);
    }

    @Override
    public void item(HashKey hashKey, RangeKey rangeKey) {
        Assert.notNull(metadata.rangeKey, "There is no RangeKey in this table.");
        this._item(hashKey, rangeKey);
    }

    private void _item(HashKey hashKey) {
        HashKey hk = keyResolver.resolve(hashKey);
        this.updateHandler.delete(metadata.table.getTableName(suffix),
                new DeleteItemSpec()
                    .withHashKey(metadata.hashKey.name, keyResolver.toAttributeValue(hk))
                    .withExpected(expected)
                    .withThrowExceptionIfNotUpdated(throwExceptionIfNotUpdated));
    }

    private void _item(HashKey hashKey, RangeKey rangeKey) {
        HashKey hk = keyResolver.resolve(hashKey);

        this.updateHandler.delete(metadata.table.getTableName(suffix),
                new DeleteItemSpec()
                    .withHashKey(metadata.hashKey.name, keyResolver.toAttributeValue(hk))
                    .withRangeKey(metadata.rangeKey.name,
                            keyResolver.toAttributeValue(rangeKey, metadata.rangeKey.fieldAndDigits))
                    .withExpected(expected)
                    .withThrowExceptionIfNotUpdated(throwExceptionIfNotUpdated));
    }

    @Override
    public Delete expected(Expected expected) {
        this.expected = expected;
        return this;
    }

    @Override
    public Delete throwExceptionIfNotUpdated() {
        this.throwExceptionIfNotUpdated = true;
        return this;
    }

}
