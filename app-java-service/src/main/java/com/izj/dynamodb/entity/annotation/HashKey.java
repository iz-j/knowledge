package com.izj.dynamodb.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to save as hash key in DB.<br>
 * If it is added to multiple fields, it is combined with a semicolon according to the order and stored.
 *
 * @author ~~~~
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HashKey {
    /**
     * If you want to specify multiple fields as HashKey,<br>
     * please do not duplicate order. Ignored if there is only one field of HashKey.
     *
     * @return order
     */
    int order() default 0;

}
