package com.izj.dynamodb.internal.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import com.google.common.collect.Iterables;
import com.izj.dynamodb.entity.annotation.Attribute;
import com.izj.dynamodb.entity.annotation.HashKey;
import com.izj.dynamodb.entity.annotation.RangeKey;
import com.izj.dynamodb.entity.annotation.Table;
import com.izj.dynamodb.entity.annotation.Table.MultiTableStrategy;
import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeMeta;
import com.izj.dynamodb.internal.metadata.EntityMetadata.AttributeType;
import com.izj.dynamodb.internal.metadata.EntityMetadata.HashKeyMeta;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta;
import com.izj.dynamodb.internal.metadata.EntityMetadata.TableMeta;
import com.izj.dynamodb.internal.metadata.EntityMetadata.RangeKeyMeta.FieldAndDigits;
import com.izj.dynamodb.internal.util.DynamodbInternalUtils;
import com.izj.dynamodb.internal.util.DynamodbReflectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 */
@Slf4j
public final class EntityAnalyzer {

    private EntityAnalyzer() {
    }

    private static final Comparator<Field> H_KEY_ORDER_COMPARATOR = new Comparator<Field>() {
        @Override
        public int compare(Field f1, Field f2) {
            return f1.getAnnotation(HashKey.class).order() - f2.getAnnotation(HashKey.class).order();
        }
    };

    private static final Comparator<FieldAndDigits> R_KEY_ORDER_COMPARATOR = new Comparator<FieldAndDigits>() {
        @Override
        public int compare(FieldAndDigits f1, FieldAndDigits f2) {
            return f1.field.getAnnotation(RangeKey.class).order()
                    - f2.field.getAnnotation(RangeKey.class).order();
        }
    };

    public static <E> EntityMetadata analyze(Class<E> entityClass) {
        TableMeta table = analyzeTable(entityClass);
        List<Field> hashKeyFields = new ArrayList<>();
        List<FieldAndDigits> rangeKeyFields = new ArrayList<>();
        Map<String, AttributeMeta> attributes = new LinkedHashMap<>();
        DynamodbReflectionUtils.walkFields(entityClass, f -> {
            if (Modifier.isStatic(f.getModifiers()))
                return;

            AttributeMeta.Builder builder = AttributeMeta.builder().field(f);
            HashKey hashKeyAnno = f.getAnnotation(HashKey.class);
            RangeKey rangeKeyAnno = f.getAnnotation(RangeKey.class);
            if (hashKeyAnno != null && rangeKeyAnno != null) {
                throw new IllegalStateException(
                        "Only one annotation of HashKey or RangeKey can be attached to one field.");
            } else if (hashKeyAnno != null) {
                hashKeyFields.add(f);
            } else if (rangeKeyAnno != null) {
                rangeKeyFields.add(new FieldAndDigits(f, f.getAnnotation(RangeKey.class).digits()));
            } else {
                AnnotationAttributes aa = AnnotatedElementUtils
                    .getMergedAnnotationAttributes(f, Attribute.class);
                if (aa != null) {
                    String name = aa.getString("name");
                    boolean json = aa.getBoolean("json");
                    boolean marker = aa.getBoolean("marker");
                    name = marker ? AttributeMeta.MARK_ATTRIBUTE_NAME : StringUtils.isEmpty(name) ? f.getName() : name;
                    attributes.put(name, builder.name(name).json(json).marker(marker).build());
                } else if (log.isTraceEnabled()) {
                    log.trace("{} ignored, because @Attribute annotation not found.", f.getName());
                }
            }
        });

        HashKeyMeta hashKeyMeta = new HashKeyMeta(
                hashKeyFields.stream().sorted(H_KEY_ORDER_COMPARATOR).collect(Collectors.toList()),
                hashKeyFields.size() > 1 ? AttributeType.S : DynamodbInternalUtils
                    .toAttributeType(Iterables.getFirst(hashKeyFields, null).getType()));

        RangeKeyMeta rangeKeyMeta = CollectionUtils.isEmpty(rangeKeyFields) ? null
                : new RangeKeyMeta(
                        rangeKeyFields
                            .stream()
                            .sorted(R_KEY_ORDER_COMPARATOR)
                            .collect(Collectors.toList()),
                        rangeKeyFields.size() > 1
                                || rangeKeyFields.stream().filter(f -> f.digits > 0).findFirst().isPresent()
                                        ? AttributeType.S : DynamodbInternalUtils
                                            .toAttributeType(
                                                    Iterables
                                                        .getFirst(rangeKeyFields, null).field.getType()));

        return EntityMetadata
            .builder()
            .table(table)
            .hashKey(hashKeyMeta)
            .rangeKey(rangeKeyMeta)
            .attributes(attributes)
            .build();
    }

    private static <E> TableMeta analyzeTable(Class<E> entityClass) {
        TableMeta.Builder builder = TableMeta.builder().entityClass(entityClass);
        AnnotationAttributes aa = AnnotatedElementUtils
            .getMergedAnnotationAttributes(entityClass, Table.class);
        MultiTableStrategy multi = aa.getEnum("multi");
        String name = aa.getString("name");

        return builder
            .simpleName(StringUtils.isEmpty(name) ? entityClass.getSimpleName() : name)
            .multi(multi)
            .build();
    }

}
