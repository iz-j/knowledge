package com.knowledge.hoge.connect.service.system.search.utils;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.knowledge.hoge.connect.service.base.i18n.MLString;
import com.knowledge.hoge.connect.service.base.i18n.MLStrings;
import com.knowledge.hoge.connect.service.base.i18n.SupportedLocale;
import com.knowledge.hoge.connect.service.base.util.function.MultiArgsFunction.Consumer4Args;
import com.knowledge.hoge.connect.service.system.search.SearchModelConverter;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchFormatter;
import com.knowledge.hoge.connect.service.system.search.internal.SearchData;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author ~~~~, ~~~~
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SearchUtils {
    private static final List<Locale> DEFAULT_LOCALES = Arrays.asList(Locale.JAPANESE, Locale.ENGLISH);

    public static SearchData toAmazonCloudSearchFormat(SearchData searchData) {
        return searchData
            .toBuilder()
            .fields(SearchSource
                .builder()
                .heading(AmazonCloudSearchFormatter.format((MLString)searchData.get(FieldType.HEADING)))

                .details(Arrays
                    .stream(FieldType.getDetails())
                    .map(field -> AmazonCloudSearchFormatter.format((MLString)searchData.get(field)))
                    .collect(Collectors.toList())
                    .toArray(new MLString[0]))

                .dates(Arrays
                    .stream(FieldType.getDates())
                    .map(field -> searchData.get(field))
                    .collect(Collectors.toList())
                    .toArray(new ZonedDateTime[0]))

                .doubles(Arrays
                    .stream(FieldType.getDoubles())
                    .map(field -> searchData.get(field))
                    .collect(Collectors.toList())
                    .toArray(new Double[0]))

                .literals(Arrays
                    .stream(FieldType.getLiterals())
                    .map(field -> searchData.get(field))
                    .collect(Collectors.toList())
                    .toArray(new String[0]))

                .build())
            .build();
    }

    /**
     * Walk value field of search data.
     *
     * @param targetClass
     * @param visitor
     */
    public static void walkMLStringFields(SearchSource src,
            Consumer4Args<FieldType, Locale, Boolean, String> visitor) {
        for (FieldType field : FieldType.values()) {
            if (field.getJavaType() != MLString.class) {
                continue;
            }

            MLString value = src.get(field);

            Stream.of(SupportedLocale.values()).forEach(sl -> {
                Locale l = sl.get();
                String v = MLStrings.get(value, l);
                visitor.apply(field, l, HighlightUtils.isHighlighted(v), v);
            });
        }
    }

    public static MLString toMLString(Object value) {
        if (Objects.isNull(value)) {
            return null;
        } else if (value instanceof String) {
            return new MLString(DEFAULT_LOCALES
                .stream()
                .collect(Collectors.toMap(e -> e, e -> (String)value))
                .entrySet());
        } else if (value instanceof MLString) {
            return (MLString)value;
        } else {
            throw new IllegalArgumentException("value should be String or MLString type");
        }
    }

    public static String joinString(String separator, String... nullableStrings) {
        if (Objects.isNull(nullableStrings)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";

        for (String string : nullableStrings) {
            if (Objects.isNull(string)) {
                continue;
            }
            sb.append(sep).append(string);
            sep = separator;
        }

        return sb.toString();
    }

    public static <M extends SearchModel> SearchData toSearchData(SearchModelConverter<M> converter, M model) {
        SearchSource src = converter.toSearchSource(model);

        return SearchData
            .builder()

            .dataType(model.getDataType())
            .ownerId(model.getOwnerId())
            .dataId(model.getDataId())

            .fields(SearchSource
                .builder()
                .heading(toMLString(src.get(FieldType.HEADING)))

                .details(Arrays
                    .stream(FieldType.getDetails())
                    .map(field -> toMLString(src.get(field)))
                    .collect(Collectors.toList())
                    .toArray(new MLString[0]))

                .dates(Arrays
                    .stream(FieldType.getDates())
                    .map(field -> src.get(field))
                    .collect(Collectors.toList())
                    .toArray(new ZonedDateTime[0]))

                .doubles(Arrays
                    .stream(FieldType.getDoubles())
                    .map(field -> src.get(field))
                    .collect(Collectors.toList())
                    .toArray(new Double[0]))

                .literals(Arrays
                    .stream(FieldType.getLiterals())
                    .map(field -> src.get(field))
                    .collect(Collectors.toList())
                    .toArray(new String[0]))

                .build())

            .original(model)
            .build();
    }
}
