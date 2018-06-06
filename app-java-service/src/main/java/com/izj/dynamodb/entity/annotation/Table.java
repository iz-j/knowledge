package com.izj.dynamodb.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 *
 * @author ~~~~
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    MultiTableStrategy multi() default MultiTableStrategy.NONE;

    public enum MultiTableStrategy {
        YEAR,
        MONTH,
        CUSTOM,
        NONE;
    }
}
