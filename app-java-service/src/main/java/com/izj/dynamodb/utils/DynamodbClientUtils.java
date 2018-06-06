package com.izj.dynamodb.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;

/**
 *
 * @author ~~~~
 *
 */
public final class DynamodbClientUtils {

    private DynamodbClientUtils() {
    }

    public static String toJson(Object src) {
        return DynamodbInternalUtils.toJson(src);
    }

    public static <T> T fromJson(Class<T> type, String json) {
        return DynamodbInternalUtils.fromJson(type, json);
    }

    public static ObjectMapper customizedObjectMapper() {
        return DynamodbInternalUtils.createCustomizedObjectMapper();
    }
}
