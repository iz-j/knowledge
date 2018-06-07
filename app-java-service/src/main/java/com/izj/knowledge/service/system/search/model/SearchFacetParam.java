package com.knowledge.hoge.connect.service.system.search.model;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.knowledge.hoge.connect.service.base.util.datetime.DateTimeUtils;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
public class SearchFacetParam {
    private FieldType field;

    /**
     * Create {@link SearchResults}'s facet map with name.<br>
     * Use field.name() if name is null.
     */
    private String name;

    /**
     * Sort order of facet results, "count" or "bucket". Default "count".
     */
    private FacetSort sort = FacetSort.COUNT;

    /**
     * Search facet count, Default 5.<br>
     * When ranges uses.
     */
    private long size = 5;

    /**
     * "filter" or "interval", Default "filter".<br>
     * Use "interval" for specify case that occurring performance problems with facets.
     * 
     * @see AWS Document
     */
    private FacetMethod method = FacetMethod.FILTER;

    private List<SearchBucketParam> buckets = Lists.newArrayList();

    public static SearchFacetParam of(FieldType field) {
        return new SearchFacetParam().field(field);
    }

    public SearchFacetParam field(FieldType field) {
        this.field = field;
        return this;
    }

    public SearchFacetParam name(String name) {
        this.name = name;
        return this;
    }

    public SearchFacetParam size(long size) {
        this.size = size;
        return this;
    }

    /**
     * Add a range to buckets with endExclusive.
     */
    public <T> SearchFacetParam addRange(String name, T startInclusive, T endExclusive) {
        checkRangeType(field, startInclusive, endExclusive);

        this.buckets.add(RangeBucket
            .builder()
            .name(name)
            .endIncluded(false)
            .start(startInclusive)
            .end(endExclusive)
            .build());
        return this;
    }

    /**
     * Add a range to buckets with endInclusive.
     */
    public <T> SearchFacetParam addClosedRange(String name, T startInclusive, T endInclusive) {
        checkRangeType(field, startInclusive, endInclusive);
        this.buckets.add(RangeBucket
            .builder()
            .name(name)
            .endIncluded(true)
            .start(startInclusive)
            .end(endInclusive)
            .build());
        return this;
    }

    /**
     * Add list to buckets.
     */
    public <T> SearchFacetParam addValues(String name, String... values) {
        this.buckets.add(ListBucket
            .builder()
            .name(name)
            .values(Arrays.asList(values))
            .build());
        return this;
    }

    private <T> void checkRangeType(FieldType field, T start, T end) {
        Assert.noNullElements(new Object[] { start, end }, "start/end should be nonNull");

        if (!field.getJavaType().isAssignableFrom(start.getClass())) {
            throw new IllegalArgumentException("Unknown range type of "
                    + field
                    + " : "
                    + start.getClass().getSimpleName());
        }
    }

    public static interface SearchBucketParam {
        String getName();

        String toCloudSearchString();
    }

    /**
     * This field against CloudSearch "buckets" field.<br>
     * Facet range for {@link Double} or {@link ZonedDateTime}.<br>
     * Add "[begin, end]"(endIncluded) or "[begin, end}"(endExcluded) to buckets.<br>
     * <br>
     * ex) When endExcluded with range(5.0, 10.0), grouping bucket 5 to 9.99999....
     */
    @Data
    @Builder
    public static class RangeBucket implements SearchBucketParam {
        /**
         * Search result created by with this name.<br>
         * If set null, return with name as begin-end range string. (ex: "[5.0,10.0]")
         */
        private final String name;
        @Default
        private final boolean endIncluded = true;
        private final Object start;
        private final Object end;

        @Override
        public String toCloudSearchString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(toValue(start));
            sb.append(",");
            sb.append(toValue(end));
            sb.append((endIncluded) ? "]" : "}");
            return sb.toString();
        }

        private String toValue(Object obj) {
            if (obj.getClass() == Double.class) {
                return obj.toString();
            }

            if (obj.getClass() == ZonedDateTime.class) {
                return "'" + DateTimeUtils.toRFC3339((ZonedDateTime)obj) + "'";
            }

            throw new IllegalStateException("Unsupported range type: " + obj.getClass().getSimpleName());
        }
    }

    /**
     * This field against CloudSearch "buckets" field.<br>
     * Facet range for String, enum or something.<br>
     * Add "['v1','v2',...]" to buckets.<br>
     */
    @Data
    @Builder
    public static class ListBucket implements SearchBucketParam {
        /**
         * Search result created by with this name.<br>
         * If set null, return with name as list string. (ex: "['v1','v2','v3']")
         */
        private final String name;
        private final List<String> values;

        @Override
        public String toCloudSearchString() {
            StringBuilder sb = new StringBuilder();
            sb.append((values.size() == 1) ? "" : "[");
            boolean first = true;
            for (String value : values) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(value);
            }
            sb.append((values.size() == 1) ? "" : "]");
            return sb.toString();
        }
    }

    public static enum FacetSort {
        COUNT, BUCKET;
    }

    public static enum FacetMethod {
        FILTER, INTERVAL;
    }
}