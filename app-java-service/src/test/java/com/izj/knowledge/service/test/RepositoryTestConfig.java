package com.izj.knowledge.service.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.junit.rules.ExternalResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.izj.dynamodb.DynamodbClient;
import com.izj.dynamodb.MultiTenantSupport;
import com.izj.knowledge.service.system.counter.ItemCounter;
import com.izj.knowledge.service.system.counter.ItemCounterImpl;
import com.izj.knowledge.service.system.counter.repo.ItemCounterEntity;
import com.izj.knowledge.service.system.counter.repo.ItemCounterRepository;
import com.izj.knowledge.service.system.counter.repo.ItemCounterRepositoryDynamo;

import lombok.extern.slf4j.Slf4j;

@EnableAspectJAutoProxy
@Import({ TestBaseConfig.class })
public class RepositoryTestConfig {

    private static final Collection<Class<?>> PRESET_ENTITIES = Arrays.asList(
            ItemCounterEntity.class);

    @Slf4j
    public static abstract class LocalDynamoDBTestRule extends ExternalResource {
        private static AmazonDynamoDBLocal localDB;

        public LocalDynamoDBTestRule() {
            log.info("Creating AmazonDynamoDBLocal ...");
            System.setProperty("sqlite4java.library.path", ".dynamodb-local-libs");
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            localDB = DynamoDBEmbedded.create();
            log.info("AmazonDynamoDBLocal has been created.");
        }

        static AmazonDynamoDB getLocalDynamoDb() {

            return localDB.amazonDynamoDB();
        }

        /**
         * Return classes to create table.
         *
         * @return entities
         */
        public abstract Collection<Class<?>> entities();

        @Override
        protected void before() throws Throwable {

            DynamodbClient client = DynamodbClient
                .builder()
                .db(localDB.amazonDynamoDB())
                .build();

            Consumer<Class<?>> creator = (entity) -> {
                client.forAdmin().createTable(entity);
            };

            log.info("Creating preset tables ...");
            PRESET_ENTITIES.stream().forEach(creator);

            log.info("Creating specified tables ... ...");
            Collection<Class<?>> entities = entities();
            if (CollectionUtils.isNotEmpty(entities)) {
                entities.stream().forEach(creator);
            }

            log.info("All tables have been created.");
        }

        @Override
        protected void after() {
            if (localDB != null) {
                localDB.shutdown();// XXX mvnでパラレルにテストが走ったらこける？shoutdownは不要かも。
            }
        }
    }

    @Bean
    public DynamodbClient dynamodbClient() {
        return DynamodbClient.builder().multiTenantSupport(new MultiTenantSupport() {
            @Override
            public String getTenantId() {
                return UUID.randomUUID().toString();
            }
        }).db(LocalDynamoDBTestRule.getLocalDynamoDb()).build();
    }

    @Bean
    public ItemCounter itemCounter() {
        return new ItemCounterImpl();
    }

    @Bean
    public ItemCounterRepository itemCounterRepository() {
        return new ItemCounterRepositoryDynamo();
    }

}
