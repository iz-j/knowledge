package com.izj.knowledge.service.system.counter;

import org.springframework.context.annotation.Bean;

import com.izj.knowledge.service.system.counter.repo.ItemCounterRepository;
import com.izj.knowledge.service.system.counter.repo.ItemCounterRepositoryDynamo;

public class ItemCounterConfig {

    @Bean
    public ItemCounterRepository itemCounterRepository() {
        return new ItemCounterRepositoryDynamo();
    }

    @Bean
    public ItemCounter itemCounter() {
        return new ItemCounterImpl();
    }
}
