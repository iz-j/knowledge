package com.izj.knowledge.service.base.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import com.izj.knowledge.service.base.validation.UUIDString.UUIDValidator;

/**
 * The annotated element must be convertible into UUID.<br>
 * Accepts String only.
 *
 * @author iz-j
 *
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { UUIDValidator.class })
public @interface UUIDString {

    String message() default "{validation.UUIDString.message}";// TODO メッセージ定義

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({
            ElementType.METHOD,
            ElementType.FIELD,
            ElementType.ANNOTATION_TYPE,
            ElementType.CONSTRUCTOR,
            ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        UUIDString[] value();
    }

    static class UUIDValidator implements ConstraintValidator<UUIDString, String> {
        @Override
        public void initialize(UUIDString constraintAnnotation) {
            // NOOP
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.length() == 0) {
                return true;
            }

            try {
                java.util.UUID.fromString(value);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
