package com.izj.dynamodb.internal.metadata;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.entity.TypeReferenceProvider;
import com.izj.dynamodb.internal.metadata.EntityMetadata.HashKeyMeta;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;
import com.izj.dynamodb.internal.util.DynamodbReflectionUtils;

public final class EntityMapper {

    public static <E> E map(EntityMetadata metadata, Map<String, Object> item, KeyResolver keyResolver) {
        @SuppressWarnings("unchecked")
        final E entity = mapRangeKeyValues(
                mapHashKeyValues((E)DynamodbReflectionUtils.newInstanceOf(metadata.table.entityClass),
                        metadata.hashKey, item, keyResolver),
                metadata.rangeKey, item, keyResolver);
        metadata.attributes.values().forEach(attribute -> {
            if (attribute.json) {
                TypeReference<?> tr = (entity instanceof TypeReferenceProvider)
                        ? ((TypeReferenceProvider)entity).get(attribute.field.getName()) : null;
                Object val = item.get(attribute.name);
                if (val != null) {
                    DynamodbReflectionUtils.setQuietly(entity, attribute.field,
                            tr == null
                                    ? DynamodbInternalUtils.fromJson(
                                            attribute.field.getType(), val.toString())
                                    : DynamodbInternalUtils.fromJson(val.toString(), tr));
                }
            } else {
                Object value;
                value = DynamodbInternalUtils.toValueObject(item.get(attribute.name), attribute.field.getType());
                DynamodbReflectionUtils.setQuietly(entity, attribute.field, value);
            }
        });
        return entity;
    }

    private static <E> E mapHashKeyValues(E entity, HashKeyMeta meta,
            Map<String, Object> item, KeyResolver keyResolver) {
        List<Object> keys = keyResolver.toSimpleValues(item.get(meta.name), meta.fields, true);
        IntStream.range(0, keys.size()).boxed().forEach(i -> {
            Field field = meta.fields.get(i);
            Object value = DynamodbInternalUtils.toValueObject(keys.get(i), field.getType());
            DynamodbReflectionUtils.setQuietly(entity, field, value);
        });
        return entity;
    }

    private static <E> E mapRangeKeyValues(E entity, RangeKeyMeta meta,
            Map<String, Object> item, KeyResolver keyResolver) {
        if (meta == null) {
            return entity;
        }
        List<Field> keyFields = meta.fieldAndDigits.stream().map(f -> f.field).collect(Collectors.toList());
        List<Object> keys = keyResolver.toSimpleValues(item.get(meta.name), keyFields, false);
        IntStream.range(0, keys.size()).boxed().forEach(i -> {
            Field field = keyFields.get(i);
            Object value = DynamodbInternalUtils.toValueObject(keys.get(i), field.getType());
            DynamodbReflectionUtils.setQuietly(entity, field, value);
        });
        return entity;
    }
}
