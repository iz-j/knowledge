package com.knowledge.hoge.connect.service.system.search.internal;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knowledge.hoge.connect.service.base.i18n.MLString;
import com.knowledge.hoge.connect.service.base.i18n.MLStrings;
import com.knowledge.hoge.connect.service.base.util.datetime.DateTimeUtils;
import com.knowledge.hoge.connect.service.base.util.json.JacksonUtils;
import com.knowledge.hoge.connect.service.base.util.json.ObjectMapperFactory;
import com.knowledge.hoge.connect.service.base.util.zip.ZipUtils;
import com.knowledge.hoge.connect.service.system.search.SearchModelConverter;
import com.knowledge.hoge.connect.service.system.search.internal.SearchData.SearchDataBuilder;
import com.knowledge.hoge.connect.service.system.search.model.DataId;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.SearchSourceBuilder;
import com.knowledge.hoge.connect.service.system.search.utils.HighlightUtils;
import com.knowledge.hoge.connect.service.system.search.utils.SearchUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmazonCloudSearchFieldConverter {

    public static String toCloudSearchJSON(String tenantId, SearchData data) {
        ObjectNode node = ObjectMapperFactory.getDefault().createObjectNode();

        node
            .put("tenant_id", tenantId)
            .put("data_type", data.getDataType().name())
            .put("owner_id", data.getOwnerId().toString())
            .put("data_id", data.getDataId().toString());

        MLString heading = data.get(FieldType.HEADING);
        node
            .put("heading_ja", MLStrings.get(heading, Locale.JAPANESE))
            .put("heading_ja_mul", MLStrings.get(heading, Locale.JAPANESE))
            .put("heading_en", MLStrings.get(heading, Locale.ENGLISH));

        int n = 1;
        for (FieldType field : FieldType.getDetails()) {
            MLString value = data.get(field);
            String number = StringUtils.leftPad(String.valueOf(n++), 2, '0');
            node
                .put("detail_ja_" + number, MLStrings.get(value, Locale.JAPANESE))
                .put("detail_ja_mul_" + number, MLStrings.get(value, Locale.JAPANESE))
                .put("detail_en_" + number, MLStrings.get(value, Locale.ENGLISH));
        }

        for (FieldType field : FieldType.getDates()) {
            node.put(field.name().toLowerCase(), Optional
                .ofNullable((ZonedDateTime)data.get(field))
                .map(v -> DateTimeUtils.toRFC3339(v))
                .orElse(null));
        }

        for (FieldType field : FieldType.getDoubles()) {
            node.put(field.name().toLowerCase(), Optional
                .ofNullable((Double)data.get(field))
                .map(v -> v.toString())
                .orElse(null));
        }

        for (FieldType field : FieldType.getLiterals()) {
            node.put(field.name().toLowerCase(), Optional
                .ofNullable((String)data.get(field))
                .orElse(null));
        }

        node.put("original", ZipUtils.compressAsBase64(data.getOriginal()));

        JacksonUtils.removeEmptyNodeRecursive(node);

        return node.toString();
    }

    public static SearchData toSearchData(String contentJSON, SearchModelConverter<?> converter) {
        try {
            JsonNode node = ObjectMapperFactory.getDefault().readTree(contentJSON);
            return toSearchData(node, converter);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read contentJSON", e);
        }

    }

    public static SearchData toSearchData(Map<String, List<String>> fields, Map<String, String> highlights,
            SearchModelConverter<?> converter) {
        ObjectNode node = ObjectMapperFactory.getDefault().createObjectNode();
        node.put("data_type", getStr(fields, "data_type"));
        node.put("owner_id", getStr(fields, "owner_id"));
        node.put("data_id", getStr(fields, "data_id"));

        node.put("heading_ja", getStr(highlights, "heading_ja"));
        node.put("heading_ja_mul", getStr(highlights, "heading_ja_mul"));
        node.put("heading_en", getStr(highlights, "heading_en"));

        for (int i = 1; i <= SearchSource.NUM_OF_DETAILS; i++) {
            String number = StringUtils.leftPad(String.valueOf(i), 2, '0');
            node.put("detail_ja_" + number, getStr(highlights, "detail_ja_" + number));
            node.put("detail_ja_mul_" + number, getStr(highlights, "detail_ja_mul_" + number));
            node.put("detail_en_" + number, getStr(highlights, "detail_en_" + number));
        }

        FieldType[] fieldTypes = Stream
            .of(Arrays.stream(FieldType.getDates()),
                    Arrays.stream(FieldType.getDoubles()),
                    Arrays.stream(FieldType.getLiterals()))
            .flatMap(v -> v)
            .toArray(FieldType[]::new);

        for (FieldType field : fieldTypes) {
            String value = getStr(fields, field.name().toLowerCase());
            node.put(field.name().toLowerCase(), value);
        }

        node.put("original", getStr(fields, "original"));

        SearchData data = toSearchData(node, converter);

        /*
         * restore original value
         */
        data = restoreOriginalValue(data, converter);

        return data;
    }

    private static SearchData toSearchData(JsonNode node, SearchModelConverter<?> converter) {
        SearchSourceBuilder source = SearchSource.builder();

        source
            .set(FieldType.HEADING, new MLString()
                .set(Locale.JAPANESE, Optional
                    .ofNullable(node.get("heading_ja").asText(null))
                    .orElse(node.get("heading_ja_mul").asText(null)))
                .set(Locale.ENGLISH, node.get("heading_en").asText(null)));

        int n = 1;
        for (FieldType field : FieldType.getDetails()) {
            String number = StringUtils.leftPad(String.valueOf(n++), 2, '0');
            source
                .set(field, new MLString()
                    .set(Locale.JAPANESE, Optional
                        .ofNullable(node.get("detail_ja_" + number).asText(null))
                        .orElse(node.get("detail_ja_mul_" + number).asText(null)))
                    .set(Locale.ENGLISH, node.get("detail_en_" + number).asText(null)));
        }

        for (FieldType field : FieldType.getDates()) {
            source.set(field, Optional
                .ofNullable(node.get(field.name().toLowerCase()).asText(null))
                .map(ZonedDateTime::parse)
                .orElse(null));
        }

        for (FieldType field : FieldType.getDoubles()) {
            source.set(field, Optional
                .ofNullable(node.get(field.name().toLowerCase()).asText(null))
                .map(Double::valueOf)
                .orElse(null));
        }

        for (FieldType field : FieldType.getLiterals()) {
            source.set(field, Optional
                .ofNullable(node.get(field.name().toLowerCase()).asText(null))
                .orElse(null));
        }

        SearchModel original = null;
        String originalBase64 = node.get("original").asText(null);
        if (Objects.nonNull(originalBase64)) {
            try {
                original = ZipUtils.decompressFromBase64(originalBase64, converter.getModelClass());
            } catch (Exception e) {
                log.error("Failed to try decompress original data", e);
            }
        }

        SearchDataBuilder builder = SearchData.builder();
        builder
            .dataType(SearchDataType.valueOf(node.get("data_type").asText(null)))
            .ownerId(OwnerId.of(node.get("owner_id").asText(null)))
            .dataId(DataId.of(node.get("data_id").asText(null)))
            .fields(source.build())
            .original(original);

        return builder.build();
    }

    private static String getStr(Map<String, List<String>> fields, String name) {
        List<String> list = fields.get(name);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    private static String getStr(Map<String, String> highlights, String... names) {
        String prior = null;

        for (String name : names) {
            String str = highlights.get(name);
            str = StringUtils.defaultIfEmpty(str, null);

            if (Objects.isNull(str)) {
                continue;
            }

            if (HighlightUtils.isHighlighted(str)) {
                return str;
            }

            prior = str;
        }

        return prior;
    }

    private static <M extends SearchModel> SearchData restoreOriginalValue(SearchData data,
            SearchModelConverter<M> converter) {
        @SuppressWarnings("unchecked")
        M original = (M)data.getOriginal();

        try {
            SearchSource src = converter.toSearchSource(original);

            MLString[] details = Arrays
                .stream(FieldType.getDetails())
                .map(field -> restoreOriginalMLS(data, src, field))
                .collect(Collectors.toList())
                .toArray(new MLString[0]);

            SearchData restored = data
                .toBuilder()
                .fields(data
                    .getFields()
                    .toBuilder()
                    .heading(restoreOriginalMLS(data, src, FieldType.HEADING))
                    .details(details)
                    .build())
                .original(original)
                .build();

            return restored;
        } catch (Exception e) {
            log.error("Failed to try restore original highlighted data.", e);
            return data;
        }
    }

    private static MLString restoreOriginalMLS(SearchData data, SearchSource src, FieldType field) {
        MLString originalMLS = SearchUtils.toMLString(src.get(field));
        MLString highlightedMLS = data.get(field);
        MLString restoredMLS = HighlightUtils.restoreFullOrigin(highlightedMLS, originalMLS);
        return restoredMLS;
    }

}
