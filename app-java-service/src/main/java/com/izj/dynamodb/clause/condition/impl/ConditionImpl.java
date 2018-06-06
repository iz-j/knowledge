package com.izj.dynamodb.clause.condition.impl;

import java.util.HashMap;
import java.util.List;

import com.izj.dynamodb.clause.condition.Condition;
import com.izj.dynamodb.clause.condition.ExpressionAndValueMap;
import com.izj.dynamodb.clause.condition.Operator;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta.FieldAndDigits;

/**
 *
 * @author ~~~~
 *
 */
public class ConditionImpl implements Condition {

    private final Operator operator;
    private final HashKey hashKey;
    private final RangeKey rangeKey1;
    private final RangeKey rangeKey2;

    public ConditionImpl(Operator operator, RangeKey rangeKey) {
        super();
        this.operator = operator;
        this.hashKey = null;
        this.rangeKey1 = rangeKey;
        this.rangeKey2 = null;
    }

    public ConditionImpl(Operator operator, RangeKey rangeKey1, RangeKey rangeKey2) {
        super();
        this.operator = operator;
        this.hashKey = null;
        this.rangeKey1 = rangeKey1;
        this.rangeKey2 = rangeKey2;
    }

    public ConditionImpl(Operator operator, HashKey hashKey) {
        super();
        this.operator = operator;
        this.hashKey = hashKey;
        this.rangeKey1 = null;
        this.rangeKey2 = null;
    }

    @Override
    public ExpressionAndValueMap toExpressionAndAttributeValues(String keyName,
            List<FieldAndDigits> fieldAndDigits, KeyResolver resolver) {
        if (hashKey != null) {
            String hashKeyName = ":" + keyName + "1";
            String expression = this.operator.getExpression(keyName, hashKeyName, null);
            return new ExpressionAndValueMap(expression, new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;
                {
                    put(hashKeyName, resolver.toAttributeValue(hashKey));
                }
            });
        } else {
            String rangeKeyName1 = ":" + keyName + "1";
            String rangeKeyName2 = ":" + keyName + "2";
            String expression = this.operator.getExpression(keyName, rangeKeyName1, rangeKeyName2);
            return new ExpressionAndValueMap(expression, new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L;
                {
                    Object rangeKey = operator == Operator.BEGINS_WITH
                            ? resolver.toJoinedAttributeValue(rangeKey1, fieldAndDigits)
                            : resolver.toAttributeValue(rangeKey1, fieldAndDigits);
                    put(rangeKeyName1,
                            rangeKey);
                    if (rangeKey2 != null)
                        put(rangeKeyName2, resolver.toAttributeValue(rangeKey2, fieldAndDigits));
                }
            });
        }
    }

}
