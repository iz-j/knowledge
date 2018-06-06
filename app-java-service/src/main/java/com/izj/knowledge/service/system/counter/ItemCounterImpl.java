package com.izj.knowledge.service.system.counter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.izj.knowledge.service.system.counter.repo.ItemCounterRepository;

@Slf4j
@Transactional
public class ItemCounterImpl implements ItemCounter {

    @Autowired
    private ItemCounterRepository repo;

    @Override
    public long getCountOf(CounterUnit unit) {
        Assert.notNull(unit, "CounterUnit required!");
        return repo.getCount(unit);
    }

    @Override
    public long getLatestPartitionNumberOf(CounterUnit unit) {
        long count = this.getCountOf(unit);
        if (count == 0) {
            return 0;
        }

        long partition = (count - 1) / unit.getDefinition().getPartitionSize();
        if (log.isDebugEnabled()) {
            log.debug("Latest partition number of {} -> {} / {} = {}",
                    unit.getDefinition(), (count - 1), unit.getDefinition().getPartitionSize(), partition);
        }
        return partition;
    }

    @Override
    public long getPartitionNumberToStore(CounterUnit unit) {
        long count = this.getCountOf(unit);
        if (count == 0) {
            return 0;
        }

        long partition = count / unit.getDefinition().getPartitionSize();
        if (log.isDebugEnabled()) {
            log.debug("Next partition number of {} -> {} / {} = {}",
                    unit.getDefinition(), count, unit.getDefinition().getPartitionSize(), partition);
        }
        return partition;
    }

    @Override
    public void increaseCount(CounterUnit unit) {
        this.increaseCount(unit, 1);
    }

    @Override
    public void increaseCount(CounterUnit unit, long increment) {
        Assert.notNull(unit, "CounterUnit required!");
        repo.addCount(unit, increment);
    }

}
