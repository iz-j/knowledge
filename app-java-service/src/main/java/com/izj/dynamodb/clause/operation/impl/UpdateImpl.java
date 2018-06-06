package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.izj.dynamodb.clause.condition.Expected;
import com.izj.dynamodb.clause.condition.LogicOperator;
import com.izj.dynamodb.clause.condition.UpdateValues;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.clause.operation.Update;
import com.izj.dynamodb.internal.handler.UpdateHandler;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.spec.UpdateItemSpec;
import com.izj.dynamodb.internal.util.DynamodbReflectionUtils;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public class UpdateImpl<I> implements Update<I> {
    private final EntityMetadata meta;
    private final UpdateHandler updateHandler;
    private final KeyResolver keyResolver;
    private final String suffix;

    private List<Expected> expecteds = new ArrayList<>();
    private boolean throwExceptionIfNotUpdated;

    public UpdateImpl(EntityMetadata meta, UpdateHandler updateHandler, KeyResolver keyResolver, String suffix) {
        super();
        this.meta = meta;
        this.updateHandler = updateHandler;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public void item(I entity) {
        HashKey hashKey = new HashKey();
        meta.hashKey.fields
            .stream()
            .map(f -> DynamodbReflectionUtils.getQuietly(entity, f))
            .forEach(k -> hashKey.with(k));
        RangeKey rangeKey = null;
        if (meta.rangeKey != null) {
            final RangeKey rk = new RangeKey();
            meta.rangeKey.fieldAndDigits
                .stream()
                .map(f -> DynamodbReflectionUtils.getQuietly(entity, f.field))
                .forEach(k -> rk.with(k));
            rangeKey = rk;
        }
        UpdateValues uv = new UpdateValues();
        meta.attributes.values().stream().forEach(attr -> {
            Object value = DynamodbReflectionUtils.getQuietly(entity, attr.field);
            uv.put(attr.name, value);
        });
        updateItem(hashKey, rangeKey, uv);
    }

    @Override
    public void item(HashKey hashKey, UpdateValues updateValues) {
        Assert.notNull(hashKey, "Hashkey must not be null.");
        Assert.isNull(this.meta.rangeKey, "RangeKey must also be specified.");
        updateItem(hashKey, null, updateValues);
    }

    @Override
    public void item(HashKey hashKey, RangeKey rangeKey, UpdateValues updateValues) {
        Assert.notNull(hashKey, "Hashkey must not be null.");
        Assert.notNull(rangeKey, "Rangekey must not be null.");
        Assert.notNull(this.meta.rangeKey, "RangeKey must also be specified.");
        Assert.notNull(updateValues, "updateValues must not be null.");
        updateItem(hashKey, rangeKey, updateValues);
    }

    private void updateItem(HashKey hashKey, RangeKey rangeKey, UpdateValues updateValues) {
        hashKey = keyResolver.resolve(hashKey);
        UpdateItemSpec spec = rangeKey == null
                ? new UpdateItemSpec().withHashKey(meta.hashKey.name, keyResolver.toAttributeValue(hashKey))
                : new UpdateItemSpec()
                    .withHashKey(meta.hashKey.name, keyResolver.toAttributeValue(hashKey))
                    .withRangeKey(meta.rangeKey.name,
                            keyResolver.toAttributeValue(rangeKey, meta.rangeKey.fieldAndDigits));
        spec
            .withUpdateValues(updateValues
                .toExpressionAndAttributeValues(meta.attributes))
            .withExpected(expecteds)
            .withThrowExceptionIfNotUpdated(throwExceptionIfNotUpdated);

        updateHandler.update(meta.table.getTableName(suffix), spec);
    }

    @Override
    public Update<I> expected(Expected expected) {
        this.expecteds.add(expected);
        return this;
    }

    @Override
    public Update<I> throwExceptionIfNotUpdated() {
        ifExists();
        this.throwExceptionIfNotUpdated = true;
        return this;
    }

    @Override
    public Update<I> ifExists() {
        Expected expected = new Expected(LogicOperator.OR);
        meta.attributes.keySet().stream().forEach(attribute -> expected.exists(attribute));
        this.expecteds.add(expected);
        return this;
    }

}
