package com.izj.dynamodb.clause.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeMeta;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

/**
 *
 * @author ~~~~
 *
 */
public class UpdateValues {
    private List<UpdateValue> updateValues = new ArrayList<>();

    private enum UpdateOperation {
        PUT("SET", "{attribute} = {value}"),
        DELETE("REMOVE", "{attribute}"),
        ADD_NUMERIC("ADD", "{attribute} {value}"),
        ADD_ELEMENTS("ADD", "{attribute} {value}"),
        REMOVE_ELEMENTS("DELETE", "{attribute} {value}"),
        LIST_APPEND("SET", "{attribute} = list_append({attribute}, {value})");

        private final String operation;
        private final String action;

        private UpdateOperation(String operation, String action) {
            this.operation = operation;
            this.action = action;
        }
    }

    private static class UpdateValue {
        private final UpdateOperation operation;
        private final String attributeName;
        private final Object[] value;

        private UpdateValue(UpdateOperation operation, String attributeName, Object... value) {
            super();
            this.operation = operation;
            this.attributeName = attributeName;
            this.value = value;
        }

        private Object getValue(AttributeMeta meta) {
            Object[] v = Arrays
                .stream(this.value)
                .map(
                        val -> meta.json ? DynamodbInternalUtils.toJson(val)
                                : DynamodbInternalUtils.toAttributeValue(val))
                .map(val -> meta.marker ? DynamodbInternalUtils.toMarkerValue(val) : val)
                .toArray(Object[]::new);
            switch (this.operation) {
            case PUT:
            case ADD_NUMERIC:
                return v[0];
            case REMOVE_ELEMENTS:
            case ADD_ELEMENTS:
                return new HashSet<>(Arrays.asList(v));
            case LIST_APPEND:
                return Arrays.asList(v);
            case DELETE:
                return null;
            default:
                break;
            }
            return null;
        }
    }

    /**
     * Modify or add an attribute.
     *
     * @param attributeName
     * @param value
     * @return
     */
    public UpdateValues put(String attributeName, Object value) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            delete(attributeName);
        } else {
            updateValues.add(new UpdateValue(UpdateOperation.PUT, attributeName, value));
        }
        return this;
    }

    /**
     * Remove elements from the set attribute.
     *
     * @param attributeName
     * @param values
     * @return
     */
    public UpdateValues removeElements(String attributeName, Object... values) {
        updateValues.add(new UpdateValue(UpdateOperation.REMOVE_ELEMENTS, attributeName, values));
        return this;
    }

    /**
     * Add elements to the set attribute.
     *
     * @param attributeName
     * @param values
     * @return
     */
    public UpdateValues addElements(String attributeName, Object... values) {
        updateValues.add(new UpdateValue(UpdateOperation.ADD_ELEMENTS, attributeName, values));
        return this;
    }

    /**
     * Increase or decrease the value of attribute.<br>
     * It can only be used for numeric attributes.
     *
     * @param attributeName
     * @param number
     * @return
     */
    public UpdateValues addNumeric(String attributeName, Number number) {
        updateValues.add(new UpdateValue(UpdateOperation.ADD_NUMERIC, attributeName, number));
        return this;
    }

    /**
     * Delete attribute from item.
     */
    public UpdateValues delete(String attributeName) {
        updateValues.add(new UpdateValue(UpdateOperation.DELETE, attributeName));
        return this;
    }

    /**
     * Add elements to the end of the list attribute.
     *
     * @param attributeName
     * @param value
     * @return
     */
    public UpdateValues listAppend(String attributeName, Object... value) {
        updateValues.add(new UpdateValue(UpdateOperation.LIST_APPEND, attributeName, value));
        return this;
    }

    public ExpressionAndValueMap toExpressionAndAttributeValues(Map<String, AttributeMeta> meta) {
        Map<String, List<UpdateValue>> updateValuesMap = updateValues
            .stream()
            .collect(Collectors.groupingBy(update -> update.operation.operation));
        StringBuilder expression = new StringBuilder();
        Map<String, Object> attributeValues = new HashMap<>();
        updateValuesMap.entrySet().stream().forEach(entry -> {
            String operation = entry.getKey();
            List<String> parts = new ArrayList<>();
            expression.append(operation).append(" ");
            entry.getValue().stream().forEach(updateValue -> {
                AttributeMeta attrMeta = meta.get(updateValue.attributeName);
                Assert.notNull(attrMeta, "Not found attribute: " + updateValue.attributeName);
                String updateValueLabel = ":update_" + updateValue.attributeName;
                parts.add(updateValue.operation.action.replace("{attribute}", updateValue.attributeName).replace(
                        "{value}",
                        updateValueLabel));
                if (updateValue.operation != UpdateOperation.DELETE) {
                    attributeValues.put(updateValueLabel, updateValue.getValue(attrMeta));
                }
            });
            expression.append(String.join(", ", parts)).append(" ");
        });
        return new ExpressionAndValueMap(expression.toString(), attributeValues);
    }

}
