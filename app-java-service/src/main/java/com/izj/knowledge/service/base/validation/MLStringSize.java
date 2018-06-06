package com.izj.knowledge.service.base.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Map.Entry;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.apache.commons.lang3.StringUtils;

import com.izj.knowledge.service.base.i18n.MLString;
import com.izj.knowledge.service.base.validation.MLStringSize.MLStringSizeValidator;

/**
 * The size of annotated element must be between the specified boundaries (included).<br>
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
@Constraint(validatedBy = { MLStringSizeValidator.class })
public @interface MLStringSize {

    String message() default "{validation.MLStringSize.message}";// TODO メッセージ定義！

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return size the all elements must be higher or equal to
     */
    int min() default 0;

    /**
     * @return size the all elements must be lower or equal to
     */
    int max() default Integer.MAX_VALUE;

    @Target({
            ElementType.METHOD,
            ElementType.FIELD,
            ElementType.ANNOTATION_TYPE,
            ElementType.CONSTRUCTOR,
            ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        MLStringSize[] value();
    }

    static class MLStringSizeValidator implements ConstraintValidator<MLStringSize, MLString> {
        private int min;
        private int max;

        @Override
        public void initialize(MLStringSize constraintAnnotation) {
            this.min = constraintAnnotation.min();
            this.max = constraintAnnotation.max();
        }

        @Override
        public boolean isValid(MLString value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;// Leave this validation to MLStringNotEmptyValidator.
            }

            for (Entry<Locale, String> e : value.entrySet()) {
                int size = StringUtils.length(e.getValue());
                if (size < min || size > max) {
                    return false;
                }
            }

            return true;
        }
    }
}
