package com.knowledge.hoge.connect.service.system.search;

import java.lang.reflect.ParameterizedType;

import com.knowledge.hoge.connect.service.system.search.internal.SearchData;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource;

public interface SearchModelConverter<M extends SearchModel> {
    SearchSource toSearchSource(M model);

    M mergeHighlights(SearchSource src, M model);

    @SuppressWarnings("unchecked")
    default Class<M> getModelClass() {
        ParameterizedType type = (ParameterizedType)getClass().getGenericInterfaces()[0];
        Class<M> clazz = (Class<M>)type.getActualTypeArguments()[0];
        return clazz;
    }

    @SuppressWarnings("unchecked")
    default M mergeHighlights(SearchData data) {
        return mergeHighlights(data.getFields(), (M)data.getOriginal());
    }

}
