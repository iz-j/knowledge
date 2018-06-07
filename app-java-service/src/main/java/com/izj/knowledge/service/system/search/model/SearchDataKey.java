package com.knowledge.hoge.connect.service.system.search.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class SearchDataKey {
    private final SearchDataType dataType;
    private final OwnerId ownerId;
    private final DataId dataId;

    public SearchDataKey(SearchDataType dataType, OwnerId ownerId, DataId dataId) {
        this.dataType = dataType;
        this.ownerId = ownerId;
        this.dataId = dataId;
    }
}
