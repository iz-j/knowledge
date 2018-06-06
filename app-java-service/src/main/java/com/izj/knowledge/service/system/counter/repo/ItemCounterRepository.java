package com.izj.knowledge.service.system.counter.repo;

import com.izj.knowledge.service.system.counter.CounterUnit;

public interface ItemCounterRepository {

    void addCount(CounterUnit unit, long increment);

    long getCount(CounterUnit unit);
}
