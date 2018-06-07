package com.knowledge.hoge.connect.service.system.search.repo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.knowledge.dynamodb.DynamodbClient;
import com.knowledge.dynamodb.clause.condition.Filter;
import com.knowledge.dynamodb.clause.key.HashKey;
import com.knowledge.hoge.connect.service.system.TenantHolder;
import com.knowledge.hoge.connect.service.system.search.SearchModelConverter;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchFieldConverter;
import com.knowledge.hoge.connect.service.system.search.internal.SearchData;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataKey;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;

public class TemporalSearchDataRepositoryDynamo implements TemporalSearchDataRepository {

    @Autowired
    private DynamodbClient db;

    @Autowired
    private TenantHolder tenant;

    @Override
    public void put(Collection<SearchData> data) {
        db
            .put(TemporalSearchDataEntity.class)
            .items(data
                .stream()
                .map(e -> TemporalSearchDataEntity.forAdd(tenant.get().getId(), e))
                .collect(Collectors.toList()));
    }

    @Override
    public void putDeletion(Collection<SearchDataKey> keys) {
        Collection<TemporalSearchDataEntity> entities = keys
            .stream()
            .map(key -> TemporalSearchDataEntity.forRemove(tenant.get().getId(), key))
            .collect(Collectors.toList());

        db.put(TemporalSearchDataEntity.class).items(entities);
    }

    @Override
    public List<SearchData> find(SearchDataType dataType, OwnerId ownerId) {
        SearchModelConverter<?> converter = dataType.instanthiateConverter();
        return db
            .query(TemporalSearchDataEntity.class)
            .filter(Filter.eq("syncType", "add"), Filter.eq("dataType", dataType))
            .items(new HashKey(ownerId))
            .getItems()
            .stream()
            .map(e -> AmazonCloudSearchFieldConverter.toSearchData(e.getContentJSON(), converter))
            .collect(Collectors.toList());
    }
}
