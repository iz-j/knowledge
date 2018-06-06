package com.izj.knowledge.service.system.counter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.izj.knowledge.service.system.counter.CounterDefinition;
import com.izj.knowledge.service.system.counter.CounterUnit;
import com.izj.knowledge.service.system.counter.ItemCounter;
import com.izj.knowledge.service.system.counter.ItemCounterConfig;
import com.izj.knowledge.service.test.RepositoryTestConfig;
import com.izj.knowledge.service.test.RepositoryTestConfig.LocalDynamoDBTestRule;

@RunWith(SpringRunner.class)
public class ItemCounterImplTest {

    @ClassRule
    public static final LocalDynamoDBTestRule dynamoDB = new LocalDynamoDBTestRule() {
        @Override
        public Collection<Class<?>> entities() {
            return Collections.emptyList();
        }
    };

    @TestConfiguration
    @Import({ RepositoryTestConfig.class, ItemCounterConfig.class })
    public static class TestConfig {
    }

    @Autowired
    private ItemCounter counter;

    @Test
    public void test() {
        long count = 0;
        long partition = 0;
        CounterUnit c = new CounterUnit(CounterDefinition.TEST).add(UUID.fromString("0-0-0-0-0"));

        count = counter.getCountOf(c);
        assertThat(count, is(0L));

        partition = counter.getLatestPartitionNumberOf(c);
        assertThat(partition, is(0L));

        partition = counter.getPartitionNumberToStore(c);
        assertThat(partition, is(0L));

        counter.increaseCount(c, CounterDefinition.TEST.getPartitionSize());
        count = counter.getCountOf(c);
        assertThat(count, is(CounterDefinition.TEST.getPartitionSize()));

        partition = counter.getLatestPartitionNumberOf(c);
        assertThat(partition, is(0L));

        partition = counter.getPartitionNumberToStore(c);
        assertThat(partition, is(1L));
    }

}
