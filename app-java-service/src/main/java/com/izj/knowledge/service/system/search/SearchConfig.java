package com.knowledge.hoge.connect.service.system.search;

import org.springframework.context.annotation.Bean;

import com.knowledge.hoge.connect.service.system.search.repo.TemporalSearchDataRepository;
import com.knowledge.hoge.connect.service.system.search.repo.TemporalSearchDataRepositoryDynamo;

public class SearchConfig {

    @Bean
    public CloudSearchService searchService() {
        return new CloudSearchServiceAWS();
    }

    @Bean
    public TemporalSearchDataRepository temporalSearchDataRepository() {
        return new TemporalSearchDataRepositoryDynamo();
    }

    @Bean
    public SearchService glovalSearchService() {
        return new SearchServiceImpl();
    }
}
