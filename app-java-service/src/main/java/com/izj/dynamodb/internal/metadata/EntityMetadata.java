package com.izj.dynamodb.internal.metadata;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.RangeKey;
import com.izj.dynamodb.entity.annotation.Table.MultiTableStrategy;
import com.izj.dynamodb.internal.util.DynamodbReflectionUtils;

import lombok.ToString;

/**
 *
 * Hold metadata of a certain entity class.
 *
 * @author ~~~~
 *
 */
@lombok.Builder
@ToString
public final class EntityMetadata {

    @lombok.Builder(builderClassName = "Builder")
    @ToString
    public static class TableMeta {
        public final Class<?> entityClass;
        public final String simpleName;
        public final MultiTableStrategy multi;

        public String getTableName(String suffix) {
            switch (multi) {
            case NONE:
                Assert.isTrue(StringUtils.isEmpty(suffix),
                        "Table suffix can not be specified for tables without a multi table strategy.");
                return simpleName;
            case YEAR:
            case MONTH:
            case CUSTOM:
                Assert.isTrue(StringUtils.isNotEmpty(suffix), "Table suffix must be specified.");
                return StringUtils.join(simpleName, "_", suffix);
            default:
                throw new IllegalStateException("Should not reach here!");
            }
        }

        public Object instanthiateEntity() {
            try {
                return entityClass.newInstance();
            } catch (Throwable t) {
                throw new IllegalStateException("Can't instantiate entity with Default Constructor: " + entityClass, t);
            }
        }
    }

    public enum AttributeType {
        S, N, B;
    }

    @lombok.Builder(builderClassName = "Builder")
    @ToString
    public static class HashKeyMeta {
        public static final String HASH_KEY_NAME = "H";
        public final String name = HASH_KEY_NAME;
        public final List<Field> fields;
        public final AttributeType attributeType;

        public HashKey of(Object entity) {
            return of(null, entity);
        }

        public HashKey of(String tenantId, Object entity) {
            HashKey hashKey = new HashKey();

            if (Objects.nonNull(tenantId)) {
                hashKey.with(tenantId);
            }

            fields
                .stream()
                .map(f -> DynamodbReflectionUtils.getQuietly(entity, f))
                .forEach(k -> hashKey.with(k));
            return hashKey;
        }
    }

    @lombok.Builder(builderClassName = "Builder")
    @ToString
    public static class RangeKeyMeta {
        public static final String RANGE_KEY_NAME = "R";
        public final String name = RANGE_KEY_NAME;
        public final List<FieldAndDigits> fieldAndDigits;
        public final AttributeType attributeType;

        public static class FieldAndDigits {
            public final Field field;
            public final int digits;

            public FieldAndDigits(Field field, int digits) {
                super();
                this.field = field;
                this.digits = digits;
            }
        }

        public RangeKey of(Object entity) {
            RangeKey rangeKey = new RangeKey();
            fieldAndDigits
                .stream()
                .map(f -> DynamodbReflectionUtils.getQuietly(entity, f.field))
                .forEach(k -> rangeKey.with(k));
            return rangeKey;
        }
    }

    @lombok.Builder(builderClassName = "Builder")
    @ToString
    public static class AttributeMeta {
        public static final String MARK_ATTRIBUTE_NAME = "M";
        public final Field field;
        public final String name;
        public final boolean json;
        public final boolean marker;
    }

    public final TableMeta table;
    public final Map<String, AttributeMeta> attributes;
    public final HashKeyMeta hashKey;
    public final RangeKeyMeta rangeKey;

    private EntityMetadata(TableMeta table, Map<String, AttributeMeta> attributes, HashKeyMeta hashKey,
            RangeKeyMeta rangeKey) {
        super();
        this.table = table;
        this.attributes = attributes;
        this.hashKey = hashKey;
        this.rangeKey = rangeKey;
    }

    public static EntityMetadataBuilder builder() {
        return new EntityMetadataBuilder();
    }

    public static class EntityMetadataBuilder {
        public EntityMetadata build() {
            Assert.isInstanceOf(LinkedHashMap.class, attributes,
                    "Order of columns must be guaranteed! So, column map should be LinkedHashMap!");

            return new EntityMetadata(table, attributes, hashKey, rangeKey);
        }
    }
}
