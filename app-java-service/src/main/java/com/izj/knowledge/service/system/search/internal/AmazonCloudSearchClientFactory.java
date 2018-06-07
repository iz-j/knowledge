package com.knowledge.hoge.connect.service.system.search.internal;

import java.util.Collections;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClientBuilder;
import com.amazonaws.services.cloudsearchdomain.model.Hits;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.SuggestRequest;
import com.amazonaws.services.cloudsearchdomain.model.SuggestResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClientBuilder;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.ServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonCloudSearchClientFactory {
    private AmazonCloudSearchClientFactory() {
    }

    public static AmazonCloudSearchDomain createClient(AWSCredentialsProvider credentialsProvider) {
        log.debug("Detecting SearchDomain ...");
        AmazonCloudSearch serviceClient = AmazonCloudSearchClientBuilder
            .standard()
            .withCredentials(credentialsProvider)
            .build();
        DescribeDomainsResult domains = serviceClient
            .describeDomains(new DescribeDomainsRequest().withDomainNames("hoge-connect-search"));
        ServiceEndpoint endpoint = domains
            .getDomainStatusList()
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("SearchDomain was not found!"))
            .getDocService();
        log.debug("SearchDomain endpoint determined -> {}", endpoint.getEndpoint());

        return AmazonCloudSearchDomainClientBuilder
            .standard()
            .withCredentials(credentialsProvider)
            .withEndpointConfiguration(new EndpointConfiguration(endpoint.getEndpoint(), "ap-northeast-1"))
            .build();
    }

    public static AmazonCloudSearchDomain noopClient() {
        return new AmazonCloudSearchDomain() {

            @Override
            public UploadDocumentsResult uploadDocuments(UploadDocumentsRequest uploadDocumentsRequest) {
                log.warn("This is noop client for CloudSearch...");
                return new UploadDocumentsResult().withAdds(0L).withDeletes(0L);
            }

            @Override
            public SuggestResult suggest(SuggestRequest suggestRequest) {
                return null;
            }

            @Override
            public void shutdown() {
            }

            @Override
            public void setRegion(Region region) {
            }

            @Override
            public void setEndpoint(String endpoint) {
            }

            @Override
            public SearchResult search(SearchRequest searchRequest) {
                log.warn("This is noop client for CloudSearch...");
                Hits hits = new Hits().withCursor(null).withFound(0L).withHit(Collections.emptyList());
                return new SearchResult().withFacets(Collections.emptyMap()).withHits(hits);
            }

            @Override
            public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
                return null;
            }
        };
    }
}
