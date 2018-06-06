package com.izj.knowledge.service.system.counter.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.izj.dynamodb.DynamodbClient;
import com.izj.dynamodb.clause.condition.UpdateValues;
import com.izj.dynamodb.clause.key.HashKey;
import com.izj.knowledge.service.system.counter.CounterUnit;

public class ItemCounterRepositoryDynamo implements ItemCounterRepository {

    @Autowired
    private DynamodbClient db;

    @Override
    public void addCount(CounterUnit unit, long increment) {
        Assert.isTrue(increment != 0, "increment must not be zero!");
        // Update or Put(if not exists)
        db
            .update(ItemCounterEntity.class)
            .ifExists()
            .item(
                    new HashKey(unit.getDefinition().toString(), unit.toKey()),
                    new UpdateValues().addNumeric("cnt", increment));
        db
            .put(ItemCounterEntity.class)
            .ifNotExists()
            .item(new ItemCounterEntity(unit.getDefinition().toString(), unit.toKey(), increment));
    }

    @Override
    public long getCount(CounterUnit unit) {
        ItemCounterEntity entity = db
            .get(ItemCounterEntity.class)
            .item(new HashKey(unit.getDefinition().toString(), unit.toKey()));
        return entity != null ? entity.getCount() : 0;
    }
}
