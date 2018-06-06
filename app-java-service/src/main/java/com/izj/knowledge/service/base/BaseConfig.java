package com.izj.knowledge.service.base;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.izj.dynamodb.DynamodbClient;
import com.izj.dynamodb.MultiTenantSupport;
import com.izj.dynamodb.transaction.DynamodbTransactionManager;
import com.izj.knowledge.service.base.aop.AopConfig;
import com.izj.knowledge.service.base.aws.AwsConfig;
import com.izj.knowledge.service.base.time.SystemClock;
import com.izj.knowledge.service.system.TenantHolder;

import lombok.extern.slf4j.Slf4j;

@Import({
        AopConfig.class,
        AwsConfig.class,
})
@Slf4j
public class BaseConfig {
    private static final int ERROR_RETRY_COUNT = 3;

    @Value("${version:not-set}")
    private String version;

    @PostConstruct
    public void init() {
        log.info("Application version is '{}'.", version);

        SystemClock.setSystemTimeZone(TimeZone.getTimeZone("UTC"));
        log.info("System default time zone set to UTC.");
    }

    @Bean
    @Autowired
    public PlatformTransactionManager transactionManager(AWSCredentialsProvider awsCredentialsProvider) {
        AmazonDynamoDBAsync db = AmazonDynamoDBAsyncClientBuilder
            .standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(new ClientConfiguration().withMaxErrorRetry(ERROR_RETRY_COUNT))
            .build();
        return new DynamodbTransactionManager(db);
    }

    @Bean
    @Autowired
    public DynamodbClient dynamodbClient(final TenantHolder tenantHolder) {
        return DynamodbClient.builder().multiTenantSupport(new MultiTenantSupport() {
            @Override
            public String getTenantId() {
                Assert.notNull(tenantHolder.get(), "Tenant must be set when use dynamoDB with multi-tenant support!");
                return tenantHolder.get().getId();
            }
        }).build();
    }

}
