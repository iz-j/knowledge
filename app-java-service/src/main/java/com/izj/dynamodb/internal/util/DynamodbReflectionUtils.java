package com.izj.dynamodb.internal.util;

import java.lang.reflect.Field;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamodbReflectionUtils {
    @FunctionalInterface
    public static interface FieldVisitor {
        void visit(Field field);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class FieldAndValue {
        private final Field field;
        private final Object value;
    }

    @FunctionalInterface
    public static interface FieldValueVisitor {
        void visit(FieldAndValue fieldAndValue);
    }

    private DynamodbReflectionUtils() {
    }

    /**
     * Walk all fields of target class.
     *
     * @param targetClass
     * @param visitor
     */
    public static void walkFields(Class<?> targetClass, FieldVisitor visitor) {
        for (Field f : targetClass.getDeclaredFields()) {
            visitor.visit(f);
        }

        Class<?> superClass = targetClass.getSuperclass();
        while (superClass != null) {
            for (Field f : superClass.getDeclaredFields()) {
                visitor.visit(f);
            }
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * Walk all fields with its value of target object.
     *
     * @param target
     * @param visitor
     */
    public static <T> void walkFieldAndValues(T target, FieldValueVisitor visitor) {
        walkFields(target.getClass(), f -> {
            Object v = getQuietly(target, f);
            visitor.visit(new FieldAndValue(f, v));
        });
    }

    public static <T> T newInstanceOf(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object getQuietly(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get {} of {}. cause = {}", field.getName(), obj.getClass().getName(), e.getMessage());
            return null;
        } catch (IllegalAccessException e) {
            log.warn("Failed to get {} of {}. cause = {}", field.getName(), obj.getClass().getName(), e.getMessage());
            return null;
        }
    }

    public static void setQuietly(Object obj, Field field, Object value) {
        if (value == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to set {} to {} of {}. cause = {}", value.getClass().getName(), field.getName(), obj
                .getClass()
                .getName(), e.getMessage());
        } catch (IllegalAccessException e) {
            log.warn("Failed to set {} to {} of {}. cause = {}", value.getClass().getName(), field.getName(), obj
                .getClass()
                .getName(), e.getMessage());
        }
    }
}
