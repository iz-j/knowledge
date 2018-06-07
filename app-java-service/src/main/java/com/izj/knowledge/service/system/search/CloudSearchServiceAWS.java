package com.knowledge.hoge.connect.service.system.search;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.knowledge.hoge.connect.EnvironmentHolder;
import com.knowledge.hoge.connect.service.system.TenantHolder;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchClientFactory;
import com.knowledge.hoge.connect.service.system.search.internal.SearchDataUploadForAmazonCloudSearch;
import com.knowledge.hoge.connect.service.system.search.internal.SearchParamConverter;
import com.knowledge.hoge.connect.service.system.search.model.DataId;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataKey;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchResults;
import com.knowledge.hoge.connect.service.system.search.repo.TemporalSearchDataRepository;
import com.knowledge.hoge.connect.service.system.search.utils.SearchUtils;
import com.knowledge.hoge.connect.universal.Environments;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloudSearchServiceAWS implements CloudSearchService {

    @Autowired
    private EnvironmentHolder env;
    @Autowired
    private AWSCredentialsProvider awsCredentialsProvider;
    @Autowired
    private TenantHolder tenant;
    @Autowired
    private TemporalSearchDataRepository tmpRepo;

    private AmazonCloudSearchDomain searchDomain;

    @PostConstruct
    public void postConstruct() {
        if (env.get() != Environments.LOCAL) {
            client();
        }
    }

    private AmazonCloudSearchDomain client() {
        if (this.searchDomain == null) {
            try {
                this.searchDomain = AmazonCloudSearchClientFactory.createClient(awsCredentialsProvider);
            } catch (Exception e) {
                log.warn("CloudSearch is not available, so using noop client instead.");
                this.searchDomain = AmazonCloudSearchClientFactory.noopClient();
            }
        }
        return this.searchDomain;
    }

    @Override
    @Transactional
    public <M extends SearchModel> void index(M model) {
        index(Arrays.asList(model));
    }

    @Override
    @Transactional
    public <M extends SearchModel> void index(Collection<M> models) {
        if (CollectionUtils.isEmpty(models)) {
            return;
        }

        SearchDataType dataType = Iterables.get(models, 0).getDataType();
        @SuppressWarnings("unchecked")
        SearchModelConverter<M> converter = (SearchModelConverter<M>)dataType.instanthiateConverter();

        tmpRepo.put(models
            .stream()
            .map(model -> SearchUtils.toSearchData(converter, model))
            .map(SearchUtils::toAmazonCloudSearchFormat)
            .collect(Collectors.toList()));
    }

    @Override
    public <M extends SearchModel> void indexPromptly(M model) {
        indexPromptly(Arrays.asList(model));
    }

    @Override
    @Transactional
    public <M extends SearchModel> void indexPromptly(Collection<M> models) {
        if (CollectionUtils.isEmpty(models)) {
            return;
        }

        SearchDataType dataType = Iterables.get(models, 0).getDataType();
        @SuppressWarnings("unchecked")
        SearchModelConverter<M> converter = (SearchModelConverter<M>)dataType.instanthiateConverter();

        List<SearchDataUploadForAmazonCloudSearch> uploads = models
            .stream()
            .map(model -> SearchUtils.toSearchData(converter, model))
            .map(SearchUtils::toAmazonCloudSearchFormat)
            .map(data -> SearchDataUploadForAmazonCloudSearch.forAdd(tenant.get().getId(), data))
            .collect(Collectors.toList());

        uploadToCloudSearch(uploads);
    }

    @Override
    public void remove(SearchDataType dataType, OwnerId ownerId, DataId dataId) {
        remove(new SearchDataKey(dataType, ownerId, dataId));
    }

    @Transactional
    @Override
    public void remove(SearchDataKey searchDataKey) {
        remove(Arrays.asList(searchDataKey));
    }

    @Transactional
    @Override
    public void remove(Collection<SearchDataKey> searchDataKeys) {
        tmpRepo.putDeletion(searchDataKeys);
    }

    @Override
    public void removeAllOf(SearchDataType searchDataType) {
        SearchRequest req = SearchParamConverter.toCloudSearchRequest(tenant.get().getId(),
                SearchParam.builder().dataType(searchDataType).build());
        SearchResult res = client().search(req);

        Collection<SearchDataUploadForAmazonCloudSearch> uploads = res
            .getHits()
            .getHit()
            .stream()
            .map(src -> {
                OwnerId ownerId = OwnerId.of(src.getFields().get("owner_id").get(0));
                DataId dataId = DataId.of(src.getFields().get("data_id").get(0));
                return SearchDataUploadForAmazonCloudSearch.forDelete(tenant.get().getId(), ownerId, dataId);
            })
            .collect(Collectors.toList());

        uploadToCloudSearch(uploads);
    }

    @Override
    public SearchResults search(SearchParam searchParam) {
        SearchRequest req = SearchParamConverter.toCloudSearchRequest(tenant.get().getId(), searchParam);
        log.debug("Search Query: {}\n FilterQuery: {}\n Sort: {}\n Facet: {}", req.getQuery(), req.getFilterQuery(),
                req.getSort(), req.getFacet());

        SearchResult res = client().search(req);
        log.debug("Found: {}, Cursor: {}", res.getHits().getFound(), res.getHits().getCursor());

        return SearchResults.of(searchParam, res);
    }

    private void uploadToCloudSearch(Collection<SearchDataUploadForAmazonCloudSearch> uploads) {
        if (CollectionUtils.isEmpty(uploads)) {
            log.debug("No documents to upload.");
            return;
        }

        byte[] documentsAsByte = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            documentsAsByte = mapper.writeValueAsBytes(uploads);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        UploadDocumentsRequest uploadRequest = new UploadDocumentsRequest()
            .withContentType("application/json")
            .withContentLength(Long.valueOf(documentsAsByte.length))
            .withDocuments(new ByteArrayInputStream(documentsAsByte));
        UploadDocumentsResult uploadResult = client().uploadDocuments(uploadRequest);
        log.debug("Documents uploaded. add: {}, delete: {}", uploadResult.getAdds(), uploadResult.getDeletes());
    }
}
