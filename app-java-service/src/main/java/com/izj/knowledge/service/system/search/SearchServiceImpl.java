package com.knowledge.hoge.connect.service.system.search;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.knowledge.hoge.connect.service.common.partner.PartnerService;
import com.knowledge.hoge.connect.service.common.partner.model.Partnership;
import com.knowledge.hoge.connect.service.common.partner.search.PartnerSearchModel;
import com.knowledge.hoge.connect.service.supplier.order.internal.OrderSearchModel;
import com.knowledge.hoge.connect.service.supplier.quotation.internal.QuotationSearchModel;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchFacetParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam.SearchField;
import com.knowledge.hoge.connect.service.system.search.model.SearchRequest.DocSearchRequest;
import com.knowledge.hoge.connect.service.system.search.model.SearchRequest.PartnerSearchRequest;
import com.knowledge.hoge.connect.service.system.search.model.SearchResults;
import com.knowledge.hoge.connect.service.system.search.model.SearchResults.SearchBucket;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;
import com.knowledge.hoge.connect.service.system.storage.ResourceStorage;
import com.knowledge.hoge.connect.service.system.storage.keys.PartnerIconKey;
import com.knowledge.hoge.connect.universal.id.CompanyId;
import com.knowledge.hoge.connect.universal.id.PartnerId;

public class SearchServiceImpl implements SearchService {

    @Autowired
    private CloudSearchService search;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ResourceStorage storage;

    @Override
    public SearchResults searchDocuments(CompanyId companyId, Locale locale, DocSearchRequest request) {
        List<SearchFacetParam> facets = null;
        if (request.isFacetEnabled()) {
            facets = Arrays.asList(
                    SearchFacetParam
                        .of(FieldType.DATA_TYPE)
                        .name("docType")
                        .addValues(SearchDataType.DOC_QUOTATION.name(), SearchDataType.DOC_QUOTATION.name())
                        .addValues(SearchDataType.DOC_ORDER.name(), SearchDataType.DOC_ORDER.name())
                        .size(2),
                    dateFacet(SearchFacetParam
                        .of(FieldType.DATE_01)
                        .name("createDate"), request.getFacetYears()),
                    SearchFacetParam
                        .of(FieldType.LITERAL_01)
                        .name("partner")
                        .size(5));
        }

        SearchParam param = SearchParam
            .builder()
            .dataType(SearchDataType.DOC_QUOTATION, SearchDataType.DOC_ORDER)
            .ownerId(OwnerId.of(companyId.getId()))
            .locale(locale)
            .searchFields(SearchField.HEADING, SearchField.DETAILS)
            .query(request.getQuery())
            .limit(request.getLimit())
            .cursor(request.getCursor())
            .sortBy(request.getSort().toSortBy())
            .facets((request.isFacetEnabled()) ? facets : null)
            .addFilter(FieldType.DATA_TYPE, request.getDocFilteres())
            .addFilter(FieldType.DATE_01, request.getDateFilteres())
            .addFilter(FieldType.LITERAL_01, request.getPartnerFilteres())
            .build();

        SearchResults results = search.search(param);

        results.completeHits(SearchDataType.DOC_QUOTATION, (e) -> {
            QuotationSearchModel model = ((QuotationSearchModel)e);
            return model
                .toBuilder()
                .partnerIcon(toPartnerIcon(model.getPartnerId()))
                .build();
        });

        results.completeHits(SearchDataType.DOC_ORDER, (e) -> {
            OrderSearchModel model = ((OrderSearchModel)e);
            return model
                .toBuilder()
                .partnerIcon(toPartnerIcon(model.getPartnerId()))
                .build();
        });

        if (request.isFacetEnabled()) {
            // convert bucket's name to partnerName from partnerId.
            Set<PartnerId> partnerIds = results
                .getFacets()
                .get("partner")
                .values()
                .stream()
                .map(SearchBucket::getValue)
                .map(PartnerId::of)
                .collect(Collectors.toSet());
            Map<PartnerId, Partnership> partners = partnerService.getPartnersAsMap(companyId, partnerIds);

            results.completeBuckets("partner", bucket -> {
                Partnership partner = partners.get(PartnerId.of(bucket.getValue()));
                Assert.notNull(partner, "Partner not found: " + bucket.getValue());
                return bucket
                    .toBuilder()
                    .name(partner.getName().get(locale))
                    .build();
            });
        }

        return results;
    }

    private SearchFacetParam dateFacet(SearchFacetParam facet, int[] facetYears) {
        for (int y : facetYears) {
            ZonedDateTime startInclusive = ZonedDateTime.of(y, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            ZonedDateTime endExclusive = startInclusive.plusYears(1);
            facet.addRange(String.valueOf(y), startInclusive, endExclusive);

            for (int m = 1; m <= 12; m++) {
                startInclusive = ZonedDateTime.of(y, m, 1, 0, 0, 0, 0, ZoneOffset.UTC);
                endExclusive = startInclusive.plusMonths(1);
                facet.addRange(y + "_" + m, startInclusive, endExclusive);
            }
        }

        return facet;
    }

    @Override
    public SearchResults searchPartners(CompanyId companyId, Locale locale, PartnerSearchRequest request) {
        SearchParam searchParam = SearchParam
            .builder()
            .dataType(SearchDataType.PARTNER)
            .ownerId(OwnerId.of(companyId.getId()))
            .locale(locale)
            .searchFields(SearchField.HEADING, SearchField.DETAILS)
            .query(request.getQuery())
            .limit(request.getLimit())
            .cursor(request.getCursor())
            .build();

        SearchResults results = search.search(searchParam);
        results.completeHits(e -> {
            PartnerSearchModel model = (PartnerSearchModel)e;
            return model
                .toBuilder()
                .icon(toPartnerIcon(model.getPartnerId()))
                .build();
        });

        return results;
    }

    private String toPartnerIcon(PartnerId partnerId) {
        if (Objects.isNull(partnerId)) {
            return null;
        }
        return storage.getResourcePath(new PartnerIconKey(partnerId));
    }
}
