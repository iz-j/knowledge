package com.izj.knowledge.service.base.aop;

import org.springframework.context.annotation.Bean;

public class AopConfig {

    @Bean
    public RepositoryLoggingInterceptor repositoryLoggingInterceptor() {
        return new RepositoryLoggingInterceptor();
    }
}
