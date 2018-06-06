package com.izj.dynamodb.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import com.izj.dynamodb.entity.annotation.Table;

public final class DynamodbEntityScanner {

    private DynamodbEntityScanner() {
    }

    public static Map<String, Class<?>> scan(String packageName, String... tableNames) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName)))
                    .setUrls(ClasspathHelper.forPackage(packageName))
                    .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner()));

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Table.class);

        if (tableNames != null && tableNames.length > 0) {
            annotated = annotated.stream().filter(c -> {
                String tableName = getTableName(c);
                return ArrayUtils.contains(tableNames, tableName);
            }).collect(Collectors.toSet());
        }

        Map<String, Class<?>> classesByTableName = new HashMap<>();
        annotated.forEach(c -> {
            String tableName = getTableName(c);
            if (classesByTableName.containsKey(tableName)) {
                throw new IllegalStateException("Duplicate table name -> "
                        + tableName
                        + " between "
                        + c.getName()
                        + " and "
                        + classesByTableName.get(tableName).getName());
            }
            classesByTableName.put(tableName, c);
        });
        return classesByTableName;
    }

    private static String getTableName(Class<?> entityClass) {
        AnnotationAttributes aa = AnnotatedElementUtils
            .getMergedAnnotationAttributes(entityClass, Table.class);
        return aa.getString("name");
    }

}
