package com.knowledge.hoge.connect.service.system.search;

import java.util.Collection;

import com.knowledge.hoge.connect.service.system.search.model.DataId;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataKey;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchResults;

public interface CloudSearchService {
    <M extends SearchModel> void index(M model);

    <M extends SearchModel> void index(Collection<M> models);

    /**
     * This method for indexing master data or application tools.<br>
     * Don't call this method for data that can be frequently update like transaction data.<br>
     * 
     * @param models
     */
    <M extends SearchModel> void indexPromptly(M model);

    /**
     * This method for indexing master data or application tools.<br>
     * Don't call this method for data that can be frequently update like transaction data.<br>
     * 
     * @param models
     */
    <M extends SearchModel> void indexPromptly(Collection<M> models);

    void remove(SearchDataType dataType, OwnerId ownerId, DataId dataId);

    void remove(SearchDataKey searchDataKey);

    void remove(Collection<SearchDataKey> searchDataKeys);

    /**
     * Don't call this method from ordinary process.<br>
     * Only application tools can use this.
     * 
     * @param searchDataType
     */
    void removeAllOf(SearchDataType searchDataType);

    SearchResults search(SearchParam searchParam);

}
