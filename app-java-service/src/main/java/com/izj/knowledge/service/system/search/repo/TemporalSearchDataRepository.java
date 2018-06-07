package com.knowledge.hoge.connect.service.system.search.repo;

import java.util.Collection;
import java.util.List;

import com.knowledge.hoge.connect.service.system.search.internal.SearchData;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataKey;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;

public interface TemporalSearchDataRepository {

    void put(Collection<SearchData> data);

    void putDeletion(Collection<SearchDataKey> keys);

    List<SearchData> find(SearchDataType dataType, OwnerId ownerId);

}
