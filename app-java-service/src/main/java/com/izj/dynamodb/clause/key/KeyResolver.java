package com.izj.dynamodb.clause.key;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;
import com.izj.dynamodb.MultiTenantSupport;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta.FieldAndDigits;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

/**
 *
 * @author ~~~~
 *
 */
public class KeyResolver {
    private static final String DELIMITER = ";";
    private final MultiTenantSupport mts;

    public KeyResolver(MultiTenantSupport mts) {
        super();
        this.mts = mts;
    }

    public HashKey resolve(HashKey hashKey) {
        if (mts == null) {
            return hashKey;
        }

        return new HashKey().with(mts.getTenantId()).with(hashKey.getAll());
    }

    public Object toAttributeValue(HashKey hashKey) {
        List<Object> keyValues = hashKey.getAll();
        if (CollectionUtils.isEmpty(keyValues)) {
            return null;
        }
        if (keyValues.size() == 1) {
            return DynamodbInternalUtils.toAttributeValue(Iterables.getFirst(keyValues, null));
        }
        return _toJoinedAttributeValue(keyValues, null);
    }

    public Object toAttributeValue(RangeKey rangeKey, List<FieldAndDigits> fieldAndDigits) {
        List<Object> keyValues = rangeKey.getAll();
        if (CollectionUtils.isEmpty(keyValues)) {
            return null;
        }
        if (keyValues.size() == 1 && fieldAndDigits.get(0).digits == 0) {
            return DynamodbInternalUtils.toAttributeValue(Iterables.getFirst(keyValues, null));
        }
        Assert.isTrue(fieldAndDigits == null || fieldAndDigits.size() == keyValues.size(), "");
        return _toJoinedAttributeValue(keyValues, fieldAndDigits);
    }

    public String toJoinedAttributeValue(RangeKey rangeKey, List<FieldAndDigits> fieldAndDigits) {
        List<Object> keyValues = rangeKey.getAll();
        if (CollectionUtils.isEmpty(keyValues)) {
            return null;
        }
        return _toJoinedAttributeValue(keyValues, fieldAndDigits);
    }

    public List<Object> toSimpleValues(Object attribute, List<Field> fields, boolean isHashKey) {
        Assert.isTrue(CollectionUtils.isNotEmpty(fields), "One or more fields are required.");
        boolean hasTenantId = isHashKey && mts != null;
        if (!hasTenantId && fields.size() == 1) {
            return Collections.singletonList(attribute);
        }
        String value = attribute.toString();
        if (value == null) {
            return null;
        }
        String[] arr = StringUtils.split(value, DELIMITER);
        final String[] values = hasTenantId ? ArrayUtils.subarray(arr, 1, arr.length) : arr;
        Assert.isTrue(values.length == fields.size(), "The number of values and the number of fields are different.");
        return IntStream
            .range(0, fields.size())
            .boxed()
            .map(i -> DynamodbInternalUtils.toSimpleObject(values[i], fields.get(i).getType()))
            .collect(Collectors.toList());
    }

    public String extractTenantId(Object hashKey) {
        if (mts == null) {
            return null;
        }
        String[] arr = StringUtils.split(hashKey.toString(), DELIMITER);
        return arr[0];
    }

    private String _toJoinedAttributeValue(Collection<Object> keyValues, List<FieldAndDigits> fieldAndDigits) {
        List<String> strKeyValues = keyValues
            .stream()
            .map(k -> DynamodbInternalUtils.toAttributeValue(k).toString())
            .collect(Collectors.toList());
        Assert.isTrue(strKeyValues.stream().allMatch(s -> !s.contains(DELIMITER)),
                "Values containing semicolons can not join.");
        return String.join(DELIMITER,
                fieldAndDigits == null ? strKeyValues : IntStream.range(0, strKeyValues.size()).boxed().map(i -> {
                    int digits = fieldAndDigits.get(i).digits;
                    String key = strKeyValues.get(i);
                    if (digits == 0) {
                        return key;
                    }
                    if (key.length() > digits) {
                        throw new IllegalStateException(
                                "Field of RangeKey#" + fieldAndDigits.get(i).field.getName() + " overflowed!!");
                    }
                    return digits == 0 ? key : StringUtils.leftPad(key, digits, "0");
                }).collect(Collectors.toList()));
    }

}
