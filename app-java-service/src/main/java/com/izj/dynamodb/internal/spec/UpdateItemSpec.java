package com.izj.dynamodb.internal.spec;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.izj.dynamodb.clause.condition.Expected;
import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.LogicOperator;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UpdateItemSpec {
    private String hashKeyName;
    private Object hashKeyValue;
    private String rangeKeyName;
    private Object rangeKeyValue;
    private ExpressionAndValueMap updateValues;
    private List<Expected> expecteds;
    private boolean throwExceptionIfNotUpdated;

    public UpdateItemSpec withHashKey(String name, Object value) {
        this.hashKeyName = name;
        this.hashKeyValue = value;
        return this;
    }

    public UpdateItemSpec withRangeKey(String name, Object value) {
        this.rangeKeyName = name;
        this.rangeKeyValue = value;
        return this;
    }

    public UpdateItemSpec withUpdateValues(ExpressionAndValueMap updateValues) {
        this.updateValues = updateValues;
        return this;
    }

    public UpdateItemSpec withThrowExceptionIfNotUpdated(boolean throwExceptionIfNotUpdated) {
        this.throwExceptionIfNotUpdated = throwExceptionIfNotUpdated;
        return this;
    }

    public UpdateItemSpec withExpected(List<Expected> expecteds) {
        this.expecteds = expecteds;
        return this;
    }

    public com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec toNative() {
        com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec spec = new com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec();
        spec.withPrimaryKey(toPrimaryKey());
        spec.withUpdateExpression(updateValues.getExpression());
        final Map<String, Object> valueMap = updateValues.getValueMap();
        if (CollectionUtils.isNotEmpty(expecteds)) {
            List<ExpressionAndValueMap> expAndVals = expecteds
                .stream()
                .map(Expected::toExpressionAndValueMap)
                .collect(Collectors.toList());
            spec.withConditionExpression(expAndVals
                .stream()
                .map(ExpressionAndValueMap::getExpression)
                .map(exp -> String.format("(%s)", exp))
                .collect(
                        Collectors.joining(LogicOperator.AND.operator)));
            expAndVals.stream().forEach(ev -> valueMap.putAll(ev.getValueMap()));
        }
        spec.withValueMap(MapUtils.isEmpty(valueMap) ? null : valueMap);
        return spec;
    }

    public PrimaryKey toPrimaryKey() {
        return rangeKeyValue == null ? new PrimaryKey(hashKeyName, hashKeyValue)
                : new PrimaryKey(hashKeyName, hashKeyValue, rangeKeyName, rangeKeyValue);
    }

}