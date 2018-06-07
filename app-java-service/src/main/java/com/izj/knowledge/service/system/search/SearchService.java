package com.knowledge.hoge.connect.service.system.search;

import java.util.Locale;

import com.knowledge.hoge.connect.service.system.search.model.SearchRequest.DocSearchRequest;
import com.knowledge.hoge.connect.service.system.search.model.SearchRequest.PartnerSearchRequest;
import com.knowledge.hoge.connect.service.system.search.model.SearchResults;
import com.knowledge.hoge.connect.universal.id.CompanyId;

public interface SearchService {

    SearchResults searchDocuments(CompanyId companyId, Locale locale, DocSearchRequest request);

    SearchResults searchPartners(CompanyId companyId, Locale locale, PartnerSearchRequest request);

}
