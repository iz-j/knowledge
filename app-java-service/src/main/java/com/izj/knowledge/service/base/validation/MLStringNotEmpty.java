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

import com.izj.knowledge.service.base.i18n.MLString;
import com.izj.knowledge.service.base.i18n.MLStrings;
import com.izj.knowledge.service.base.validation.MLStringNotEmpty.MLStringNotEmptyValidator;

/**
 * The annotated element must have at least one locale value.<br>
 * Accepts MLString only.
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
@Constraint(validatedBy = { MLStringNotEmptyValidator.class })
public @interface MLStringNotEmpty {

    String message() default "{validation.MLStringNotEmplty.message}";// TODO メッセージ定義！

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
        MLStringNotEmpty[] value();
    }

    static class MLStringNotEmptyValidator implements ConstraintValidator<MLStringNotEmpty, MLString> {
        @Override
        public void initialize(MLStringNotEmpty constraintAnnotation) {
            // NOOP
        }

        @Override
        public boolean isValid(MLString value, ConstraintValidatorContext context) {
            return MLStrings.isNotEmpty(value);
        }
    }
}
