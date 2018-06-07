package com.knowledge.hoge.connect.service.system.search.model;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.knowledge.hoge.connect.service.system.search.model.SearchFacetParam.SearchBucketParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
@Builder
public final class SearchParam {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class SortBy {
        private final SortField field;
        private final boolean asc;

        public static SortBy of(SortField field, boolean asc) {
            return new SortBy(field, asc);
        }

        public static SortBy descOf(SortField field) {
            return new SortBy(field, false);
        }

        public static SortBy ascOf(SortField field) {
            return new SortBy(field, false);
        }

        public String toSearchParam(Locale locale) {
            String order = asc ? " asc" : " desc";

            switch (field) {
            case HEADING:
                if (Locale.JAPANESE.equals(locale)) {
                    return "heading_ja" + order;
                } else {
                    return "heading_en" + order;
                }
            case SCORE:
                return "_score" + order;
            default:
                return field.name().toLowerCase() + order;
            }
        }
    }

    public static enum SortField {
        SCORE,
        HEADING,
        DATE_01,
        DATE_02,
        DATE_03,
        DATE_04,
        DATE_05,
        DOUBLE_01,
        DOUBLE_02,
        DOUBLE_03,
        DOUBLE_04,
        DOUBLE_05,
        ;
    }

    public static enum SearchField {
        HEADING, DETAILS;
    }

    private final SearchDataType[] dataTypes;

    private final OwnerId ownerId;

    /**
     * Set how to sort.<br>
     * Default is 'SCORE'.
     *
     * @param sortBy
     * @return builder
     */
    @Default
    private final SortBy sortBy = SortBy.descOf(SortField.SCORE);

    /**
     * Set what field to search.<br>
     * Default is all fields.
     *
     * @param searchFields
     * @return builder
     */
    @Default
    private final SearchField[] searchFields = SearchField.values();

    /**
     * Set limit of search results.<br>
     * Default is 25.
     *
     * @param limit
     * @return builder
     */
    @Default
    private final long limit = 25;

    private final Locale locale;

    /**
     * Set the key to retrieve next page of search results.<br>
     * You can get this key from search results if results have next page.
     *
     * @param cursor
     * @return builder
     */
    private final String cursor;

    private final String query;

    @Default
    private final Map<FieldType, Set<String>> filters = Maps.newHashMap();

    @Default
    private List<SearchFacetParam> facets = Lists.newArrayList();

    public SearchFacetParam findFacetOf(FieldType field) {
        if (Objects.isNull(facets)) {
            return null;
        }

        for (SearchFacetParam facet : facets) {
            if (facet.getField() == field) {
                return facet;
            }
        }

        return null;
    }

    public SearchBucketParam findBucketOf(FieldType field, String bucketValue) {
        if (Objects.isNull(facets)) {
            return null;
        }

        for (SearchFacetParam facet : facets) {
            if (facet.getField() != field) {
                continue;
            }
            for (SearchBucketParam bucket : facet.getBuckets()) {
                if (bucket.toCloudSearchString().equals(bucketValue)) {
                    return bucket;
                }
            }
        }

        return null;
    }

    public static class SearchParamBuilder {
        public SearchParamBuilder dataType(SearchDataType... dataTypes) {
            this.dataTypes = dataTypes;
            return this;
        }

        public SearchParamBuilder searchFields(SearchField... searchFields) {
            this.searchFields = searchFields;
            return this;
        }

        public SearchParamBuilder addFilter(FieldType field, String... filters) {
            return this.addFilter(field, Lists.newArrayList(filters));
        }

        public SearchParamBuilder addFilter(FieldType field, Collection<String> filters) {
            if (Objects.isNull(this.filters)) {
                filters(Maps.newHashMap());
            }

            if (!this.filters.containsKey(field)) {
                this.filters.put(field, Sets.newHashSet());
            }

            this.filters.get(field).addAll(filters);
            return this;
        }
    }
}
