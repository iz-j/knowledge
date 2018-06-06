package com.izj.dynamodb.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to save as range key in DB.<br>
 * If it is added to multiple fields, it is combined with a semicolon according to the order and stored.
 *
 * @author ~~~~
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RangeKey {
    /**
     * To include numerical values in RangeKey,<br>
     * please specify the number of digits to fill.<br>
     * This is necessary for accurate ordering. Ignored if field is not a numeric type.
     *
     * @return number of digits
     */
    int digits() default 0;

    /**
     * If you want to specify multiple fields as RangeKey,<br>
     * please do not duplicate order. Ignored if there is only one field of RangeKey
     *
     * @return order
     */
    int order() default 0;
}
