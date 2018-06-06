package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.operation.Query;
import com.izj.dynamodb.clause.operation.QueryResult;
import com.izj.dynamodb.exception.ThroughputExceedException;
import com.izj.dynamodb.internal.metadata.EntityMapper;
import com.izj.dynamodb.internal.metadata.EntityMetadata;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 *
 */
@Slf4j
public class QueryImpl<I> implements Query<I> {
    private static final int MAX_RESULT_SIZE = 300;
    private boolean consistentRead = false;
    private boolean asc = true;
    private int limit = MAX_RESULT_SIZE;
    private List<Filter> filter = new ArrayList<>();
    private Condition condition = null;

    private final EntityMetadata metadata;
    private final DynamoDB db;
    private final KeyResolver keyResolver;
    private final String suffix;

    public QueryImpl(EntityMetadata metadata, DynamoDB db, KeyResolver keyResolver, String suffix) {
        super();
        this.metadata = metadata;
        this.db = db;
        this.keyResolver = keyResolver;
        this.suffix = suffix;
    }

    @Override
    public QueryResult<I> items(HashKey hashKey) {
        return items(hashKey, null);
    }

    @Override
    public QueryResult<I> items(HashKey hashKey, String exclusiveStartKey) {
        Condition hashKeyCondition = Condition.eq(keyResolver.resolve(hashKey));
        ExpressionAndValueMap eaOfHash = hashKeyCondition
            .toExpressionAndAttributeValues(metadata.hashKey.name, null, keyResolver);
        List<String> expressions = new ArrayList<>();
        List<Map<String, Object>> valueMap = new ArrayList<>();
        expressions.add(eaOfHash.getExpression());
        valueMap.add(eaOfHash.getValueMap());
        if (condition != null) {
            ExpressionAndValueMap eaOfRange = condition
                .toExpressionAndAttributeValues(metadata.rangeKey.name, metadata.rangeKey.fieldAndDigits,
                        keyResolver);
            expressions.add(eaOfRange.getExpression());
            valueMap.add(eaOfRange.getValueMap());
        }

        QuerySpec spec = new QuerySpec().withKeyConditionExpression(String.join(" and ", expressions));
        if (CollectionUtils.isNotEmpty(filter)) {
            List<String> filterExpressions = new ArrayList<>();
            filter.stream().forEach(f -> {
                ExpressionAndValueMap eaOfFilter = f
                    .toExpressionAndAttributeValues(metadata.attributes.get(f.getAttributeName()));
                filterExpressions.add(eaOfFilter.getExpression());
                valueMap.add(eaOfFilter.getValueMap());
            });
            spec.withFilterExpression(String.join(" and ", filterExpressions));
        }

        spec
            .withValueMap(
                    valueMap
                        .stream()
                        .flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1)))
            .withScanIndexForward(asc)
            .withConsistentRead(consistentRead)
            .withMaxResultSize(limit);
        if (exclusiveStartKey != null) {
            spec.withExclusiveStartKey(KeyConverter
                .toMapKey(exclusiveStartKey)
                .entrySet()
                .stream()
                .map(
                        e -> new KeyAttribute(e.getKey(), DynamodbInternalUtils.toSimpleValue(e.getValue())))
                .toArray(KeyAttribute[]::new));
        }

        Table table = this.db.getTable(metadata.table.getTableName(suffix));

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            ItemCollection<QueryOutcome> outcomes = table.query(spec);
            Iterator<Item> items = outcomes.iterator();
            List<I> itemList = new ArrayList<>();
            while (items.hasNext()) {
                itemList.add(EntityMapper.map(metadata, items.next().asMap(), keyResolver));
            }
            Map<String, AttributeValue> lastEvaluatedKey = outcomes.getLastLowLevelResult() == null ? null
                    : outcomes.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey();
            QueryResult<I> result = QueryResult
                .<I> builder()
                .items(itemList)
                .lastEvaluatedKey(KeyConverter.toStringKey(lastEvaluatedKey))
                .build();

            sw.stop();
            if (log.isDebugEnabled())
                log.debug("Query complete. elapsed time: {} ms", sw.getTime());
            return result;
        } catch (ProvisionedThroughputExceededException e) {
            throw new ThroughputExceedException(e, table.getTableName(), false);
        }
    }

    @Override
    public Query<I> limit(int limit) {
        Assert.isTrue(limit > 0, "Please set a value of 0 or more.");
        this.limit = limit <= MAX_RESULT_SIZE ? limit : MAX_RESULT_SIZE;
        return this;
    }

    @Override
    public Query<I> condition(Condition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public Query<I> filter(Filter... filter) {
        if (filter != null) {
            this.filter = Arrays.asList(filter);
        }
        return this;
    }

    @Override
    public Query<I> consistentRead() {
        this.consistentRead = true;
        return this;
    }

    @Override
    public Query<I> asc() {
        this.asc = true;
        return this;
    }

    @Override
    public Query<I> desc() {
        this.asc = false;
        return this;
    }

}
