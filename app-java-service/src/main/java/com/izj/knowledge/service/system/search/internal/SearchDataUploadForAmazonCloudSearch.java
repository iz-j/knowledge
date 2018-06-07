package com.knowledge.hoge.connect.service.system.search.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.knowledge.hoge.connect.service.base.util.json.ObjectMapperFactory;
import com.knowledge.hoge.connect.service.system.search.model.DataId;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchDataUploadForAmazonCloudSearch {
    private String type;
    private String id;
    private Map<String, Object> fields;

    public static SearchDataUploadForAmazonCloudSearch forAdd(String tenantId, SearchData src) {
        SearchDataUploadForAmazonCloudSearch dst = new SearchDataUploadForAmazonCloudSearch();
        dst.type = "add";
        dst.id = StringUtils.joinWith(";", tenantId, src.getOwnerId(), src.getDataId());

        try {
            String json = AmazonCloudSearchFieldConverter.toCloudSearchJSON(tenantId, src);
            dst.fields = ObjectMapperFactory.getDefault().readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Can't convert JSON to Map", e);
        }
        return dst;
    }

    public static SearchDataUploadForAmazonCloudSearch forDelete(String tenantId, OwnerId ownerId, DataId dataId) {
        SearchDataUploadForAmazonCloudSearch dst = new SearchDataUploadForAmazonCloudSearch();
        dst.type = "delete";
        dst.id = StringUtils.joinWith(";", tenantId, ownerId, dataId);
        return dst;
    }
}
