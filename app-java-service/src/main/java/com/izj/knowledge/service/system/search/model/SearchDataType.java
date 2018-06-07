package com.knowledge.hoge.connect.service.system.search.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.knowledge.hoge.connect.service.common.company.search.CompanySearchConverter;
import com.knowledge.hoge.connect.service.common.partner.search.PartnerSearchConverter;
import com.knowledge.hoge.connect.service.supplier.order.internal.OrderSearchConverter;
import com.knowledge.hoge.connect.service.supplier.quotation.internal.QuotationSearchConverter;
import com.knowledge.hoge.connect.service.system.search.SearchModelConverter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SearchDataType {
    DOC_QUOTATION(QuotationSearchConverter.class, false),
    DOC_ORDER(OrderSearchConverter.class, false),

    COMPANY(CompanySearchConverter.class, false),
    PARTNER(PartnerSearchConverter.class, false),
    ITEM(null, false),
    FILE(null, false),
    ADDRESS(null, true),

    TEST(null, false),
    ;

    private final Class<? extends SearchModelConverter<?>> converterClass;
    private final boolean crossoverTenant;

    public boolean isCrossoverTenant() {
        return crossoverTenant;
    }

    public SearchModelConverter<?> instanthiateConverter() {
        try {
            return converterClass.newInstance();
        } catch (Throwable t) {
            throw new IllegalStateException("Can't instantiate converter of " + this, t);
        }
    }

    public static Map<SearchDataType, SearchModelConverter<?>> instanthiateConverters(SearchDataType... dataTypes) {
        Map<SearchDataType, SearchModelConverter<?>> map = Arrays
            .stream(dataTypes)
            .collect(Collectors.toMap(e -> e, e -> e.instanthiateConverter()));
        return map;
    }
}
