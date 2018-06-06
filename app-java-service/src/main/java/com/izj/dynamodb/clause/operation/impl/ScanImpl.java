package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.operation.Scan;
import com.izj.dynamodb.exception.ThroughputExceedException;
import com.izj.dynamodb.internal.metadata.EntityMapper;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.metadata.EntityMetadata.HashKeyMeta;

public class ScanImpl<I> implements Scan<I> {
    private final EntityMetadata metadata;
    private final AmazonDynamoDB db;
    private final KeyResolver keyResolver;
    private final String suffix;

    private int limit = 300;
    private List<Filter> filters;
    private boolean consistentRead;

    public ScanImpl(EntityMetadata metadata, AmazonDynamoDB db, KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.db = db;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public com.izj.dynamodb.clause.operation.ScanResult<I> items() {
        return _items(null);
    }

    @Override
    public com.izj.dynamodb.clause.operation.ScanResult<I> items(String exclusiveStartKey) {
        Assert.notNull(exclusiveStartKey, "Key must not be null.");
        return _items(exclusiveStartKey);
    }

    public com.izj.dynamodb.clause.operation.ScanResult<I> _items(String exclusiveStartKey) {
        ScanRequest req = new ScanRequest(metadata.table.getTableName(suffix));
        if (CollectionUtils.isNotEmpty(filters)) {
            List<ExpressionAndValueMap> eaList = filters
                .stream()
                .map(f -> f.toExpressionAndAttributeValues(metadata.attributes.get(f.getAttributeName())))
                .collect(Collectors.toList());
            String exp = eaList.stream().map(ea -> ea.getExpression()).collect(Collectors.joining(" and "));
            Map<String, AttributeValue> values = new HashMap<>();
            eaList
                .stream()
                .map(ea -> ea.getValueMap())
                .forEach(attributeVelues -> attributeVelues.forEach((k, v) -> {
                    values.put(k, InternalUtils.toAttributeValue(v));
                }));
            req.withFilterExpression(exp).withExpressionAttributeValues(values);
        }
        if (exclusiveStartKey != null) {
            req.withExclusiveStartKey(KeyConverter.toMapKey(exclusiveStartKey));
        }

        try {
            ScanResult result = db.scan(req.withLimit(limit).withConsistentRead(consistentRead));
            List<com.izj.dynamodb.clause.operation.ScanResult.ScannedItem<I>> list = result
                .getItems()
                .stream()
                .map(item -> {
                    Map<String, Object> values = InternalUtils.toSimpleMapValue(item);
                    String tenantId = keyResolver.extractTenantId(values.get(HashKeyMeta.HASH_KEY_NAME));
                    @SuppressWarnings("unchecked")
                    I parsedItem = (I)EntityMapper.map(metadata, values, keyResolver);
                    return new com.izj.dynamodb.clause.operation.ScanResult.ScannedItem<>(tenantId, parsedItem);
                })
                .collect(Collectors.toList());
            return new com.izj.dynamodb.clause.operation.ScanResult<I>(
                    KeyConverter.toStringKey(result.getLastEvaluatedKey()), list);
        } catch (ProvisionedThroughputExceededException e) {
            throw new ThroughputExceedException(e);
        }
    }

    @Override
    public Scan<I> filter(Filter... filters) {
        if (filters != null) {
            this.filters = Arrays.asList(filters);
        }
        return this;
    }

    @Override
    public Scan<I> filter(Collection<Filter> filters) {
        this.filters = new ArrayList<>(filters);
        return this;
    }

    @Override
    public Scan<I> limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Scan<I> consistentRead() {
        this.consistentRead = true;
        return this;
    }
}
