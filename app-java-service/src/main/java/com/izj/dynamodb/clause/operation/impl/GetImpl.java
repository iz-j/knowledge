package com.izj.dynamodb.clause.operation.impl;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.clause.operation.Get;
import com.izj.dynamodb.exception.ThroughputExceedException;
import com.izj.dynamodb.internal.metadata.EntityMapper;
import com.izj.dynamodb.internal.metadata.EntityMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 *
 */
@Slf4j
public class GetImpl<I> implements Get<I> {
    private boolean consistentRead = false;
    private final EntityMetadata metadata;
    private final DynamoDB db;
    private final KeyResolver keyResolver;
    private final String suffix;

    public GetImpl(EntityMetadata metadata, DynamoDB db, KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.db = db;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public I item(HashKey hashKey) {
        Assert.isNull(metadata.rangeKey, "RangeKey also needs to be specified.");
        return _item(hashKey, null);
    }

    @Override
    public I item(HashKey hashKey, RangeKey rangeKey) {
        Assert.notNull(metadata.rangeKey, "There is no RangeKey in this table.");
        return _item(hashKey, rangeKey);
    }

    private I _item(HashKey hashKey, RangeKey rangeKey) {
        hashKey = keyResolver.resolve(hashKey);
        Table table = db.getTable(this.metadata.table.getTableName(suffix));
        Object hkValue = keyResolver.toAttributeValue(hashKey);
        GetItemSpec spec = rangeKey == null
                ? new GetItemSpec().withPrimaryKey(metadata.hashKey.name, hkValue)
                : new GetItemSpec().withPrimaryKey(metadata.hashKey.name, hkValue,
                        metadata.rangeKey.name,
                        keyResolver.toAttributeValue(rangeKey, metadata.rangeKey.fieldAndDigits));
        spec.withConsistentRead(consistentRead);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Item outcome = table.getItem(spec);
            sw.stop();
            if (log.isDebugEnabled())
                log.debug("Get item complete. elapsed time: {} ms", sw.getTime());
            return outcome == null ? null : EntityMapper.map(metadata, outcome.asMap(), keyResolver);
        } catch (Exception e) {
            if (e instanceof ProvisionedThroughputExceededException) {
                throw new ThroughputExceedException((ProvisionedThroughputExceededException)e, table.getTableName(),
                        false);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Get<I> consistentRead() {
        this.consistentRead = true;
        return this;
    }

}
