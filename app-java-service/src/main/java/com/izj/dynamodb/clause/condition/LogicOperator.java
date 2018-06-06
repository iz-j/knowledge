package com.izj.dynamodb.clause.condition;

/**
 *
 * @author ~~~~
 *
 */
public enum LogicOperator {
    AND(" AND "), OR(" OR ");

    public final String operator;

    private LogicOperator(String operator) {
        this.operator = operator;
    }

}
