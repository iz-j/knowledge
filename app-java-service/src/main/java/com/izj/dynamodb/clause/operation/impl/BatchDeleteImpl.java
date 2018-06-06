package com.izj.dynamodb.clause.operation.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.PrimaryKey;
import com.izj.dynamodb.clause.operation.BatchDelete;
import com.izj.dynamodb.internal.handler.UpdateHandler;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.spec.DeleteItemSpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchDeleteImpl implements BatchDelete {
    private final EntityMetadata metadata;
    private final UpdateHandler updateHandler;
    private final KeyResolver keyResolver;
    private final String suffix;

    public BatchDeleteImpl(EntityMetadata metadata, UpdateHandler updateHandler, KeyResolver keyResolver,
            String suffix) {
        super();
        this.metadata = metadata;
        this.updateHandler = updateHandler;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public void items(Collection<PrimaryKey> keys) {
        Assert.notNull(metadata.rangeKey, "There is no RangeKey in this table.");
        if (CollectionUtils.isEmpty(keys)) {
            log.debug("Because Keys is empty it did not update.");
            return;
        }

        updateHandler.batchDelete(metadata.table.getTableName(suffix), keys
            .stream()
            .map(key -> {
                HashKey hk = keyResolver.resolve(key.hashKey);
                return new DeleteItemSpec()
                    .withHashKey(metadata.hashKey.name, keyResolver.toAttributeValue(hk))
                    .withRangeKey(metadata.rangeKey.name,
                            keyResolver.toAttributeValue(key.rangeKey, metadata.rangeKey.fieldAndDigits));
            })
            .collect(Collectors.toList()));

    }

    @Override
    public void itemsByHashOnlyPrimaryKeys(Collection<HashKey> hashKeys) {
        Assert.isNull(metadata.rangeKey, "RangeKey also needs to be specified.");
        if (CollectionUtils.isEmpty(hashKeys)) {
            log.debug("Because Keys is empty it did not update.");
            return;
        }
        updateHandler.batchDelete(metadata.table.getTableName(suffix), hashKeys
            .stream()
            .map(key -> {
                HashKey hk = keyResolver.resolve(key);
                return new DeleteItemSpec().withHashKey(metadata.hashKey.name,
                        keyResolver.toAttributeValue(hk));
            })
            .collect(Collectors.toList()));
    }

}
