package com.izj.dynamodb.clause.condition;

import java.util.Map;

import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author ~~~~
 *
 */
@Getter
@ToString
public class ExpressionAndValueMap {
    private String expression;
    private Map<String, Object> valueMap;

    public ExpressionAndValueMap(String expression, Map<String, Object> valueMap) {
        super();
        this.expression = expression;
        this.valueMap = valueMap;
    }

}
