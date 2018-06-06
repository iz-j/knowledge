package com.izj.dynamodb.clause.condition;

import java.util.Collection;

import com.izj.dynamodb.clause.condition.impl.FilterImpl;
import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeMeta;

public interface Filter {

    ExpressionAndValueMap toExpressionAndAttributeValues(AttributeMeta meta);

    String getAttributeName();

    public static <I> Filter eq(String itemName, Object value) {
        return new FilterImpl(Operator.EQ, itemName, value, null);
    }

    public static <I> Filter beginsWith(String itemName, Object value) {
        return new FilterImpl(Operator.BEGINS_WITH, itemName, value, null);
    }

    public static <I> Filter between(String itemName, Object low, Object high) {
        return new FilterImpl(Operator.BETWEEN, itemName, low,
                high);
    }

    public static <I> Filter ge(String itemName, Object value) {
        return new FilterImpl(Operator.GE, itemName, value,
                null);
    }

    public static <I> Filter gt(String itemName, Object value) {
        return new FilterImpl(Operator.GT, itemName, value,
                null);
    }

    public static <I> Filter le(String itemName, Object value) {
        return new FilterImpl(Operator.LE, itemName, value,
                null);
    }

    public static <I> Filter lt(String itemName, Object value) {
        return new FilterImpl(Operator.LT, itemName, value,
                null);
    }

    public static <I> Filter in(String itemName, Collection<?> values) {
        return new FilterImpl(Operator.IN, itemName, values);
    }

    public static <I> Filter contains(String itemName, Object value) {
        return new FilterImpl(Operator.CONTAINS, itemName, value, null);
    }

    public static <I> Filter notContains(String itemName, Object value) {
        return new FilterImpl(Operator.NOT_CONTAINS, itemName, value, null);
    }

}
