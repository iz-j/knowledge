package com.izj.dynamodb.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import com.izj.dynamodb.entity.TypeReferenceProvider;

/**
 * Annotation to save as normal attribute in DB.<br>
 *
 * @author ~~~~
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    /**
     * @see TypeReferenceProvider
     * @return is jsonable attribute
     */
    boolean json() default false;

    /**
     *
     * @return
     */
    boolean marker() default false;
}
