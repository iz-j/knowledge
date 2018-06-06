package com.izj.knowledge.service.system.counter.repo;

import com.izj.dynamodb.entity.annotation.Attribute;
import com.izj.dynamodb.entity.annotation.HashKey;
import com.izj.dynamodb.entity.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("ItemCounter")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCounterEntity {

    @HashKey
    private String counterName;

    @HashKey
    private String unitKey;

    @Attribute("cnt")
    private long count;

}
