package com.knowledge.hoge.connect.service.system.search.model;

import java.util.List;

import com.knowledge.hoge.connect.service.system.search.model.SearchParam.SortBy;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam.SortField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public interface SearchRequest {
    String getQuery();

    int getLimit();

    String getCursor();

    boolean isFacetEnabled();

    @Data
    @Builder
    public static class DocSearchRequest implements SearchRequest {
        private final String query;
        private final int limit;
        private final String cursor;
        private final DocSearchSort sort;

        private final boolean facetEnabled;
        private final int[] facetYears;

        private final List<String> docFilteres;
        private final List<String> dateFilteres;
        private final List<String> partnerFilteres;

        @AllArgsConstructor
        public static enum DocSearchSort {
            SCORE(SortField.SCORE, false),
            CREATE_DATE_DESC(SortField.DATE_01, false),
            CREATE_DATE_ASC(SortField.DATE_01, true),
            TOTAL_AMOUNT_DESC(SortField.DOUBLE_01, false),
            TOTAL_AMOUNT_ASC(SortField.DOUBLE_01, true),
            ;

            private final SortField field;
            private final boolean asc;

            public SortBy toSortBy() {
                return SortBy.of(field, asc);
            }
        }
    }

    @Data
    @Builder
    public static class PartnerSearchRequest implements SearchRequest {
        private final String query;
        private final int limit;
        private final String cursor;

        @Override
        public boolean isFacetEnabled() {
            return false;
        }
    }
}