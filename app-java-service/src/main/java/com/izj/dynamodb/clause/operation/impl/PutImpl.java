package com.izj.dynamodb.clause.operation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.clause.operation.Put;
import com.izj.dynamodb.internal.handler.UpdateHandler;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.spec.PutItemSpec;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;
import com.izj.dynamodb.internal.util.DynamodbReflectionUtils;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public class PutImpl<I> implements Put<I> {
    private boolean onlyNotExists = false;
    private boolean throwExceptionIfExists = false;
    private final EntityMetadata meta;
    private final UpdateHandler updateHandler;
    private final KeyResolver keyResolver;
    private final String suffix;

    public PutImpl(EntityMetadata meta, UpdateHandler updateHandler, KeyResolver keyResolver, String suffix) {
        super();
        this.meta = meta;
        this.updateHandler = updateHandler;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public void item(I item) {
        PutItemSpec spec = new PutItemSpec().withItem(toItem(item, meta));
        if (onlyNotExists) {
            StringBuilder expression = new StringBuilder();
            expression.append("attribute_not_exists(").append(meta.hashKey.name).append(")");
            if (meta.rangeKey != null) {
                expression.append(" and attribute_not_exists(").append(meta.rangeKey.name).append(")");
            }
            spec.withCondition(expression.toString()).withThrowExceptionIfExists(throwExceptionIfExists);
        }
        updateHandler.put(meta.table.getTableName(suffix), spec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void items(I... items) {
        items(Arrays.asList(items));
    }

    @Override
    public void items(Collection<I> items) {
        if (!onlyNotExists) {
            updateHandler
                .batchPut(meta.table.getTableName(suffix), items.stream().map(item -> {
                    return new com.izj.dynamodb.internal.spec.PutItemSpec().withItem(toItem(item, meta));
                }).collect(Collectors.toList()));
        } else {
            items.stream().forEach(item -> item(item));
        }
    }

    private <E> Item toItem(E entity, EntityMetadata metadata) {
        HashKey hk = new HashKey();
        metadata.hashKey.fields
            .stream()
            .forEach(f -> {
                Object val = DynamodbReflectionUtils.getQuietly(entity, f);
                Assert.notNull(val, "Field of hash key is null! -> " + f.getName());
                hk.with(val);
            });

        HashKey hashKey = keyResolver.resolve(hk);
        Object hashKeyValue = keyResolver.toAttributeValue(hashKey);

        List<?> rangeKeyValues = metadata.rangeKey != null ? metadata.rangeKey.fieldAndDigits
            .stream()
            .map(e -> DynamodbReflectionUtils.getQuietly(entity, e.field))
            .collect(Collectors.toList()) : null;
        RangeKey rangeKey = rangeKeyValues != null ? new RangeKey().with(rangeKeyValues) : null;

        Item item = rangeKey == null
                ? new Item().withPrimaryKey(metadata.hashKey.name, hashKeyValue)
                : new Item().withPrimaryKey(metadata.hashKey.name, hashKeyValue,
                        metadata.rangeKey.name,
                        keyResolver.toAttributeValue(rangeKey, metadata.rangeKey.fieldAndDigits));
        metadata.attributes.values().stream().forEach(a -> {
            Object value = DynamodbReflectionUtils.getQuietly(entity, a.field);
            if (value != null) {
                Object itemValue = a.json ? DynamodbInternalUtils.toJson(value)
                        : DynamodbInternalUtils.toAttributeValue(value);
                item.with(a.name, a.marker ? DynamodbInternalUtils.toMarkerValue(itemValue) : itemValue);
            }
        });
        return item;
    }

    @Override
    public Put<I> ifNotExists() {
        this.onlyNotExists = true;
        return this;
    }

    @Override
    public Put<I> throwExceptionIfExists() {
        ifNotExists();
        this.throwExceptionIfExists = true;
        return this;
    }

}
