package com.izj.dynamodb.clause.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

/**
 *
 * @author ~~~~
 *
 */
public class Expected {
    private final List<com.amazonaws.services.dynamodbv2.document.Expected> expectedList = new ArrayList<>();
    private final LogicOperator logicOperator;
    private List<InnerExpected> expecteds = new ArrayList<>();

    public Expected() {
        super();
        this.logicOperator = LogicOperator.AND;
    }

    public Expected(LogicOperator logicOperator) {
        super();
        this.logicOperator = logicOperator;
    }

    private static class InnerExpected {
        public final Operator operator;
        public final String attribute;
        public final Object value1;
        public final Object value2;
        public final List<?> values;

        private InnerExpected(Operator operator, String attribute) {
            this(operator, attribute, null, null, null);
        }

        private InnerExpected(Operator operator, String attribute, Object value1) {
            this(operator, attribute, value1, null, null);
        }

        private InnerExpected(Operator operator, String attribute, Object value1, Object value2) {
            this(operator, attribute, value1, value2, null);
        }

        private InnerExpected(Operator operator, String attribute, List<?> values) {
            this(operator, attribute, null, null, values);
        }

        private InnerExpected(Operator operator, String attribute, Object value1, Object value2, List<?> values) {
            super();
            this.operator = operator;
            this.attribute = attribute;
            this.value1 = DynamodbInternalUtils.toAttributeValue(value1);
            this.value2 = DynamodbInternalUtils.toAttributeValue(value2);
            this.values = DynamodbInternalUtils.toAttributeValueList(values);
        }

        private static final String PLACEHOLDER_FORMAT = ":%s_expected_%s%o";

        private ExpressionAndValueMap toExpressionAndValueMap() {
            String expression = null;
            Map<String, Object> valueMap = null;
            if (values == null && value1 == null && value2 == null) {
                expression = this.operator.getExpression(attribute, null, null);
                valueMap = new HashMap<>();
            } else if (values != null) {
                List<String> placeHolders = IntStream
                    .range(0, values.size())
                    .boxed()
                    .map(i -> String.format(PLACEHOLDER_FORMAT, attribute, operator.name(), i))
                    .collect(Collectors.toList());
                expression = this.operator.getExpression(attribute, placeHolders);
                valueMap = IntStream
                    .range(0, values.size())
                    .boxed()
                    .collect(Collectors.toMap(i -> placeHolders.get(i), i -> values.get(i)));
            } else {
                String ph1 = String.format(PLACEHOLDER_FORMAT, attribute, operator.name(), 1);
                String ph2 = String.format(PLACEHOLDER_FORMAT, attribute, operator.name(), 2);
                expression = this.operator.getExpression(attribute, ph1, ph2);
                valueMap = new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put(ph1, value1);
                        if (value2 != null) {
                            put(ph2, value2);
                        }
                    }
                };
            }
            return new ExpressionAndValueMap(expression, valueMap);
        }
    }

    public Expected eq(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).eq(val));
        expecteds.add(new InnerExpected(Operator.EQ, attrName, val));
        return this;
    }

    public Expected ne(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).ne(val));
        expecteds.add(new InnerExpected(Operator.NE, attrName, val));
        return this;
    }

    public Expected exists(String attrName) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).exists());
        expecteds.add(new InnerExpected(Operator.EXISTS, attrName));
        return this;
    }

    public Expected notExists(String attrName) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).notExist());
        expecteds.add(new InnerExpected(Operator.NOT_EXISTS, attrName));
        return this;
    }

    public Expected beginsWith(String attrName, String val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).beginsWith(val));
        expecteds.add(new InnerExpected(Operator.BEGINS_WITH, attrName, val));
        return this;
    }

    public Expected between(String attrName, Object low, Object high) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).between(low, high));
        expecteds.add(new InnerExpected(Operator.BETWEEN, attrName, low, high));
        return this;
    }

    public Expected contains(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).contains(val));
        expecteds.add(new InnerExpected(Operator.CONTAINS, attrName, val));
        return this;
    }

    public Expected notContains(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).notContains(val));
        expecteds.add(new InnerExpected(Operator.NOT_CONTAINS, attrName, val));
        return this;
    }

    public Expected ge(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).ge(val));
        expecteds.add(new InnerExpected(Operator.GT, attrName, val));
        return this;
    }

    public Expected gt(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).gt(val));
        expecteds.add(new InnerExpected(Operator.GE, attrName, val));
        return this;
    }

    public Expected le(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).le(val));
        expecteds.add(new InnerExpected(Operator.LE, attrName, val));
        return this;
    }

    public Expected lt(String attrName, Object val) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).lt(val));
        expecteds.add(new InnerExpected(Operator.LT, attrName, val));
        return this;
    }

    public Expected in(String attrName, Object... values) {
        expectedList.add(new com.amazonaws.services.dynamodbv2.document.Expected(attrName).in(values));
        List<?> list = Arrays.asList(values);
        expecteds.add(new InnerExpected(Operator.IN, attrName, list));
        return this;
    }

    public List<com.amazonaws.services.dynamodbv2.document.Expected> toNative() {
        return ImmutableList.copyOf(expectedList);
    }

    public ExpressionAndValueMap toExpressionAndValueMap() {
        List<ExpressionAndValueMap> expressionAndValueMaps = expecteds
            .stream()
            .map(InnerExpected::toExpressionAndValueMap)
            .collect(Collectors.toList());
        String expression = expressionAndValueMaps
            .stream()
            .map(ExpressionAndValueMap::getExpression)
            .collect(Collectors.joining(logicOperator.operator));
        Map<String, Object> valueMap = expressionAndValueMaps
            .stream()
            .flatMap(e -> e.getValueMap().entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ExpressionAndValueMap(expression.toString(), valueMap);
    }

}
