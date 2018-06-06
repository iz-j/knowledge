package com.izj.knowledge.service.system.counter.repo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Year;
import java.util.Collection;
import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.izj.knowledge.service.system.counter.CounterDefinition;
import com.izj.knowledge.service.system.counter.CounterUnit;
import com.izj.knowledge.service.system.counter.repo.ItemCounterRepository;
import com.izj.knowledge.service.system.counter.repo.ItemCounterRepositoryDynamo;
import com.izj.knowledge.service.test.RepositoryTestConfig;
import com.izj.knowledge.service.test.RepositoryTestConfig.LocalDynamoDBTestRule;

@RunWith(SpringRunner.class)
public class ItemCounterRepositoryDynamoTest {

    @ClassRule
    public static final LocalDynamoDBTestRule dynamoDB = new LocalDynamoDBTestRule() {
        @Override
        public Collection<Class<?>> entities() {
            return Collections.emptyList();
        }
    };

    @TestConfiguration
    @Import({ RepositoryTestConfig.class })
    public static class TestConfig {

        @Bean
        public ItemCounterRepository repo() {
            return new ItemCounterRepositoryDynamo();
        }
    }

    @Autowired
    private ItemCounterRepository repo;

    @Test
    public void test() {
        CounterUnit c1 = new CounterUnit(CounterDefinition.TEST);
        repo.addCount(c1, 1);
        assertThat(repo.getCount(c1), is(1L));

        CounterUnit c2 = new CounterUnit(CounterDefinition.TEST).add(Year.of(1999));
        repo.addCount(c2, 10);
        assertThat(repo.getCount(c2), is(10L));

        repo.addCount(c2, -5);
        assertThat(repo.getCount(c2), is(5L));
    }
}
