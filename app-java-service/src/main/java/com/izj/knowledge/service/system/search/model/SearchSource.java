package com.knowledge.hoge.connect.service.system.search.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.knowledge.hoge.connect.service.base.i18n.MLString;
import com.knowledge.hoge.connect.service.system.search.utils.SearchUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class have fields as same as CloudSearch domain fields.
 * 
 * @author ~~~~
 *
 */
@Data
@Builder(toBuilder = true)
public final class SearchSource {
    public static final int NUM_OF_DETAILS = 10;
    public static final int NUM_OF_DATES = 5;
    public static final int NUM_OF_DOUBLES = 5;
    public static final int NUM_OF_LITERALS = 3;

    @Getter(AccessLevel.PRIVATE)
    private final MLString heading;
    @Getter(AccessLevel.PRIVATE)
    private final MLString[] details;
    @Getter(AccessLevel.PRIVATE)
    private final ZonedDateTime[] dates;
    @Getter(AccessLevel.PRIVATE)
    private final Double[] doubles;
    @Getter(AccessLevel.PRIVATE)
    private final String[] literals;

    @RequiredArgsConstructor
    public enum FieldType implements Serializable {
        DATA_TYPE(FieldType.class), // for facet param

        HEADING(MLString.class),

        DETAIL_01(MLString.class),
        DETAIL_02(MLString.class),
        DETAIL_03(MLString.class),
        DETAIL_04(MLString.class),
        DETAIL_05(MLString.class),
        DETAIL_06(MLString.class),
        DETAIL_07(MLString.class),
        DETAIL_08(MLString.class),
        DETAIL_09(MLString.class),
        DETAIL_10(MLString.class),

        DATE_01(ZonedDateTime.class),
        DATE_02(ZonedDateTime.class),
        DATE_03(ZonedDateTime.class),
        DATE_04(ZonedDateTime.class),
        DATE_05(ZonedDateTime.class),

        DOUBLE_01(Double.class),
        DOUBLE_02(Double.class),
        DOUBLE_03(Double.class),
        DOUBLE_04(Double.class),
        DOUBLE_05(Double.class),

        LITERAL_01(String.class),
        LITERAL_02(String.class),
        LITERAL_03(String.class),
        ;

        public static FieldType[] getDetails() {
            return new FieldType[] {
                    DETAIL_01,
                    DETAIL_02,
                    DETAIL_03,
                    DETAIL_04,
                    DETAIL_05,
                    DETAIL_06,
                    DETAIL_07,
                    DETAIL_08,
                    DETAIL_09,
                    DETAIL_10
            };
        }

        public static FieldType[] getDates() {
            return new FieldType[] {
                    DATE_01,
                    DATE_02,
                    DATE_03,
                    DATE_04,
                    DATE_05,
            };
        }

        public static FieldType[] getDoubles() {
            return new FieldType[] {
                    DOUBLE_01,
                    DOUBLE_02,
                    DOUBLE_03,
                    DOUBLE_04,
                    DOUBLE_05,
            };
        }

        public static FieldType[] getLiterals() {
            return new FieldType[] {
                    LITERAL_01,
                    LITERAL_02,
                    LITERAL_03,
            };
        }

        @Getter
        private final Class<?> javaType;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(FieldType field) {
        switch (field) {
        case HEADING:
            return (T)getHeading();
        case DETAIL_01:
        case DETAIL_02:
        case DETAIL_03:
        case DETAIL_04:
        case DETAIL_05:
        case DETAIL_06:
        case DETAIL_07:
        case DETAIL_08:
        case DETAIL_09:
        case DETAIL_10:
            int detailIndex = field.ordinal() - FieldType.DETAIL_01.ordinal();
            return (T)details[detailIndex];
        case DATE_01:
        case DATE_02:
        case DATE_03:
        case DATE_04:
        case DATE_05:
            int dateIndex = field.ordinal() - FieldType.DATE_01.ordinal();
            return (T)dates[dateIndex];
        case DOUBLE_01:
        case DOUBLE_02:
        case DOUBLE_03:
        case DOUBLE_04:
        case DOUBLE_05:
            int doubleIndex = field.ordinal() - FieldType.DOUBLE_01.ordinal();
            return (T)doubles[doubleIndex];
        case LITERAL_01:
        case LITERAL_02:
        case LITERAL_03:
            int facetIndex = field.ordinal() - FieldType.LITERAL_01.ordinal();
            return (T)literals[facetIndex];
        default:
            throw new IllegalArgumentException("Unknown FieldType: " + field);
        }
    }

    public static class SearchSourceBuilder {
        {
            details = new MLString[NUM_OF_DETAILS];
            dates = new ZonedDateTime[NUM_OF_DATES];
            doubles = new Double[NUM_OF_DOUBLES];
            literals = new String[NUM_OF_LITERALS];
        }

        public SearchSourceBuilder set(FieldType field, Object value) {
            switch (field) {
            case HEADING:
                heading = SearchUtils.toMLString(value);
                break;
            case DETAIL_01:
            case DETAIL_02:
            case DETAIL_03:
            case DETAIL_04:
            case DETAIL_05:
            case DETAIL_06:
            case DETAIL_07:
            case DETAIL_08:
            case DETAIL_09:
            case DETAIL_10:
                int index = field.ordinal() - FieldType.DETAIL_01.ordinal();
                details[index] = SearchUtils.toMLString(value);
                break;
            case DATE_01:
            case DATE_02:
            case DATE_03:
            case DATE_04:
            case DATE_05:
                int dateIndex = field.ordinal() - FieldType.DATE_01.ordinal();
                dates[dateIndex] = (ZonedDateTime)value;
                break;
            case DOUBLE_01:
            case DOUBLE_02:
            case DOUBLE_03:
            case DOUBLE_04:
            case DOUBLE_05:
                int doubleIndex = field.ordinal() - FieldType.DOUBLE_01.ordinal();
                doubles[doubleIndex] = (Double)value;
                break;
            case LITERAL_01:
            case LITERAL_02:
            case LITERAL_03:
                int facetIndex = field.ordinal() - FieldType.LITERAL_01.ordinal();
                literals[facetIndex] = Optional.ofNullable(value).map(Object::toString).orElse(null);
                break;

            default:
                throw new IllegalArgumentException("Not support FieldType: " + field);
            }
            return this;
        }
    }
}
