package com.knowledge.hoge.connect.service.system.search.repo;

import com.knowledge.dynamodb.entity.annotation.Attribute;
import com.knowledge.dynamodb.entity.annotation.HashKey;
import com.knowledge.dynamodb.entity.annotation.RangeKey;
import com.knowledge.dynamodb.entity.annotation.Table;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchFieldConverter;
import com.knowledge.hoge.connect.service.system.search.internal.SearchData;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataKey;

import lombok.Data;

@Table("TemporalSearchData")
@Data
public class TemporalSearchDataEntity {

    @HashKey
    private String ownerId;

    @RangeKey
    private String dataId;

    @Attribute
    private String dataType;

    @Attribute
    private String syncType;

    @Attribute
    private String contentJSON;

    public static TemporalSearchDataEntity forAdd(String tenantId, SearchData data) {
        TemporalSearchDataEntity e = new TemporalSearchDataEntity();
        e.setDataType(data.getDataType().toString());
        e.setOwnerId(data.getOwnerId().toString());
        e.setDataId(data.getDataId().toString());
        e.setSyncType("add");
        e.setContentJSON(AmazonCloudSearchFieldConverter.toCloudSearchJSON(tenantId, data));
        return e;
    }

    public static TemporalSearchDataEntity forRemove(String tenantId, SearchDataKey key) {
        TemporalSearchDataEntity e = new TemporalSearchDataEntity();
        e.setDataType(key.getDataType().toString());
        e.setOwnerId(key.getOwnerId().toString());
        e.setDataId(key.getDataId().toString());
        e.setSyncType("delete");
        return e;
    }
}
