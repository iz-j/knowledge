package com.izj.dynamodb.clause.condition;

import java.util.List;

import com.izj.dynamodb.clause.condition.impl.ConditionImpl;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta.FieldAndDigits;

/**
 *
 * @author ~~~~
 *
 */
public interface Condition {

    ExpressionAndValueMap toExpressionAndAttributeValues(String keyName,
            List<FieldAndDigits> fieldAndDigits, KeyResolver resolver);

    public static <I> Condition eq(HashKey hashKey) {
        return new ConditionImpl(Operator.EQ, hashKey);
    }

    public static <I> Condition eq(RangeKey rangeKey) {
        return new ConditionImpl(Operator.EQ, rangeKey);
    }

    public static <I> Condition beginsWith(RangeKey rangeKey) {
        return new ConditionImpl(Operator.BEGINS_WITH, rangeKey);
    }

    public static <I> Condition between(RangeKey low, RangeKey high) {
        return new ConditionImpl(Operator.BETWEEN, low, high);
    }

    public static <I> Condition ge(RangeKey rangeKey) {
        return new ConditionImpl(Operator.GE, rangeKey);
    }

    public static <I> Condition gt(RangeKey rangeKey) {
        return new ConditionImpl(Operator.GT, rangeKey);
    }

    public static <I> Condition le(RangeKey rangeKey) {
        return new ConditionImpl(Operator.LE, rangeKey);
    }

    public static <I> Condition lt(RangeKey rangeKey) {
        return new ConditionImpl(Operator.LT, rangeKey);
    }

}
