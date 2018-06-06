package com.izj.dynamodb.clause.condition.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;

import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.Filter;
import com.izj.dynamodb.clause.condition.Operator;
import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeMeta;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

/**
 *
 * @author ~~~~
 *
 */
public class FilterImpl implements Filter {
    private final Operator operator;
    private final String attributeName;
    private final Object value1;
    private final Object value2;
    private final Collection<?> values;

    public FilterImpl(Operator operator, String attributeName, Object value1, Object value2) {
        super();
        this.operator = operator;
        this.attributeName = attributeName;
        this.value1 = value1;
        this.value2 = value2;
        this.values = null;
    }

    public FilterImpl(Operator operator, String attributeName, Collection<?> values) {
        super();
        this.operator = operator;
        this.attributeName = attributeName;
        this.value1 = null;
        this.value2 = null;
        this.values = values;
    }

    @Override
    public ExpressionAndValueMap toExpressionAndAttributeValues(AttributeMeta meta) {
        String expression;
        Map<String, Object> attrValues;
        if (this.value1 != null) {
            String valueName1 = ":" + attributeName + "1";
            String valueName2 = ":" + attributeName + "2";
            expression = this.operator.getExpression(attributeName, valueName1, valueName2);
            attrValues = new HashMap<>();
            attrValues.put(valueName1, meta != null && meta.json ? DynamodbInternalUtils.toJson(value1)
                    : DynamodbInternalUtils.toAttributeValue(value1));
            if (value2 != null) {
                attrValues.put(valueName2, meta != null && meta.json ? DynamodbInternalUtils.toJson(value2)
                        : DynamodbInternalUtils.toAttributeValue(value2));
            }
        } else if (CollectionUtils.isNotEmpty(values)) {
            List<Object> list = new ArrayList<>(values);
            List<String> valuesName = IntStream
                .range(0, list.size())
                .boxed()
                .map(i -> ":" + attributeName + Integer.toString(i))
                .collect(Collectors.toList());
            expression = this.operator.getExpression(attributeName, valuesName);
            attrValues = IntStream
                .range(0, values.size())
                .boxed()
                .collect(Collectors.toMap(i -> valuesName.get(i),
                        i -> meta != null && meta.json ? DynamodbInternalUtils.toJson(list.get(i))
                                : DynamodbInternalUtils.toAttributeValue(list.get(i))));
        } else {
            throw new IllegalStateException();
        }

        return new ExpressionAndValueMap(expression, attrValues);
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

}
