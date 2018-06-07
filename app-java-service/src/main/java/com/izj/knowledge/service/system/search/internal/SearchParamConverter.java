package com.knowledge.hoge.connect.service.system.search.internal;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.cloudsearchdomain.model.QueryParser;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.knowledge.hoge.connect.service.base.util.json.JacksonUtils;
import com.knowledge.hoge.connect.service.base.util.json.ObjectMapperFactory;
import com.knowledge.hoge.connect.service.system.search.internal.SearchQuery.QueryBuilder;
import com.knowledge.hoge.connect.service.system.search.internal.SearchQuery.QueryBuilder.QueryOpe;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchFacetParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchFacetParam.SearchBucketParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam.SortBy;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam.SortField;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;
import com.knowledge.hoge.connect.service.system.search.utils.HighlightUtils;

public final class SearchParamConverter {
    private SearchParamConverter() {
    }

    public static SearchRequest toCloudSearchRequest(String tenantId, SearchParam param) {
        return new SearchRequest()
            .withQueryParser(QueryParser.Lucene)
            .withQuery(createQuery(param))
            .withFilterQuery(createFilterQuery(tenantId, param))
            .withSort(createSort(param))
            .withQueryOptions(createQueryOptions(param))
            .withHighlight(createHighlight(param))
            .withFacet(createFacet(param))
            .withSize(param.getLimit())
            .withCursor(createCursor(param));
    }

    private static String createQuery(SearchParam p) {
        String rawQuery = p.getQuery();
        rawQuery = AmazonCloudSearchFormatter.format(rawQuery);

        if (StringUtils.isBlank(rawQuery)) {
            return QueryBuilder
                .of(QueryParser.Lucene, QueryOpe.AND)
                .addValue("*")
                .build();
        }

        String[] keywords = rawQuery.split("( |　)");
        QueryBuilder query = QueryBuilder.of(QueryParser.Lucene, QueryOpe.AND);
        for (String keyword : keywords) {
            QueryBuilder subQuery = query.createSubQuery(QueryOpe.OR);

            subQuery.addValue(keyword + "^10");
            subQuery.addValue("*" + keyword + "^8");
            subQuery.addValue(keyword + "*^8");
            subQuery.addValue("*" + keyword + "*^8");

            if (!StringUtils.isNumeric(keyword) && 2 <= StringUtils.length(keyword)) {
                subQuery.addValue(keyword + "~0.5^1");
            }
        }

        return query.build();
    }

    private static String createFilterQuery(String tenantId, SearchParam p) {
        QueryBuilder query = QueryBuilder.of(QueryParser.Structured, QueryOpe.AND);

        boolean crossover = Arrays.stream(p.getDataTypes()).noneMatch(e -> !e.isCrossoverTenant());
        String tenantIdToUse = crossover ? "*" : tenantId;
        query.addValueWithField("tenant_id", tenantIdToUse);

        QueryBuilder dataTypeQuery = query.createSubQuery(QueryOpe.OR);
        for (SearchDataType dataType : p.getDataTypes()) {
            dataTypeQuery.addValueWithField("data_type", dataType.name());
        }

        for (Entry<FieldType, Set<String>> e : p.getFilters().entrySet()) {
            FieldType field = e.getKey();
            QueryBuilder filterQuery = query.createSubQuery(QueryOpe.OR);

            if (field.getJavaType() == ZonedDateTime.class) {
                filterQuery.withoutBrackets();
            }

            for (String value : e.getValue()) {
                filterQuery.addValueWithField(field.name().toLowerCase(), value);
            }
        }

        query.addValueWithField("owner_id", p.getOwnerId());

        return query.build();
    }

    private static String createSort(SearchParam p) {
        Locale locale = p.getLocale();
        SortBy heading = SortBy.ascOf(SortField.HEADING);
        SortBy score = SortBy.descOf(SortField.SCORE);

        List<String> sorts = Lists.newArrayList();
        switch (p.getSortBy().getField()) {
        case HEADING:
            sorts.add(heading.toSearchParam(locale));
            sorts.add(score.toSearchParam(locale));
            break;
        case SCORE:
            sorts.add(score.toSearchParam(locale));
            sorts.add(heading.toSearchParam(locale));
            break;
        default:
            sorts.add(p.getSortBy().toSearchParam(locale));
            sorts.add(score.toSearchParam(locale));
            sorts.add(heading.toSearchParam(locale));
            break;
        }

        return StringUtils.join(sorts.toArray(), ",");
    }

    private static String createQueryOptions(SearchParam p) {
        ObjectNode result = newObjectNode();

        ArrayNode fields = newArrayNode();
        for (int i = 0; i < p.getSearchFields().length; i++) {
            switch (p.getSearchFields()[i]) {
            case HEADING:
                fields.add("heading_ja");
                fields.add("heading_ja_mul");
                fields.add("heading_en");
                break;
            case DETAILS:
                for (int n = 1; n <= SearchSource.NUM_OF_DETAILS; n++) {
                    String number = StringUtils.leftPad(String.valueOf(n), 2, '0');
                    fields.add("detail_ja_" + number);
                    fields.add("detail_ja_mul_" + number);
                    fields.add("detail_en_" + number);
                }
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
        result.set("fields", fields);

        return result.toString();
    }

    private static String createHighlight(SearchParam p) {
        // XXX 必要なFieldを指定できるようにする？

        ObjectNode result = newObjectNode();

        ObjectNode options = newObjectNode();
        options.put("pre_tag", HighlightUtils.PRE_TAG);
        options.put("post_tag", HighlightUtils.POST_TAG);
        options.put("format", "text");

        result.set("heading_ja", options);
        result.set("heading_ja_mul", options);
        result.set("heading_en", options);

        for (int n = 1; n <= SearchSource.NUM_OF_DETAILS; n++) {
            String number = StringUtils.leftPad(String.valueOf(n), 2, '0');
            result.set("detail_ja_" + number, options);
        }

        return result.toString();
    }

    private static String createFacet(SearchParam param) {
        if (CollectionUtils.isEmpty(param.getFacets())) {
            return null;
        }

        ObjectNode result = newObjectNode();

        for (SearchFacetParam facet : param.getFacets()) {
            ObjectNode options = newObjectNode();

            if (CollectionUtils.isNotEmpty(facet.getBuckets())) {
                ArrayNode bucketsNode = newArrayNode();
                for (SearchBucketParam bucket : facet.getBuckets()) {
                    bucketsNode.add(bucket.toCloudSearchString());
                }
                options.set("buckets", bucketsNode);
                options.put("method", facet.getMethod().name().toLowerCase());
            } else {
                options.put("size", facet.getSize());
                options.put("sort", facet.getSort().name().toLowerCase());
            }

            result.set(facet.getField().name().toLowerCase(), options);
        }

        JacksonUtils.removeEmptyNodeRecursive(result);
        return result.toString();
    }

    private static ObjectNode newObjectNode() {
        return ObjectMapperFactory.getDefault().createObjectNode();
    }

    private static ArrayNode newArrayNode() {
        return ObjectMapperFactory.getDefault().createArrayNode();
    }

    private static String createCursor(SearchParam p) {
        return StringUtils.isNotEmpty(p.getCursor()) ? p.getCursor() : "initial";
    }
}
