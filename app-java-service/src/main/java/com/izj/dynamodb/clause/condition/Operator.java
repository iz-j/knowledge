package com.izj.dynamodb.clause.condition;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum Operator {
    EQ("{attribute} = {value1}"),
    NE("{attribute} <> {value1}"),
    BETWEEN("{attribute} between {value1} and {value2}"),
    GE("{attribute} >= {value1}"),
    GT("{attribute} > {value1}"),
    LE("{attribute} <= {value1}"),
    LT("{attribute} < {value1}"),
    BEGINS_WITH("begins_with ({attribute}, {value1})"),
    IN("{attribute} in ({values})"),
    EXISTS("attribute_exists ({attribute})"),
    NOT_EXISTS("attribute_not_exists ({attribute})"),
    CONTAINS("contains ({attribute}, {value1})"),
    NOT_CONTAINS("NOT contains ({attribute}, {value1})");

    private final String expression;

    private Operator(String expression) {
        this.expression = expression;
    }

    public String getExpression(String itemName, String valueName1, String valueName2) {
        if (this == IN)
            throw new UnsupportedOperationException();
        return this.expression
            .replace("{attribute}", itemName)
            .replace("{value1}", valueName1 == null ? StringUtils.EMPTY : valueName1)
            .replace("{value2}",
                    valueName2 == null ? StringUtils.EMPTY : valueName2);
    }

    public String getExpression(String itemName, List<String> valuesName) {
        if (this != IN)
            throw new UnsupportedOperationException();
        return this.expression.replace("{attribute}", itemName).replace("{values}", String.join(", ", valuesName));
    }
}
