package com.knowledge.hoge.connect.service.system.search.internal;

import com.knowledge.hoge.connect.service.system.search.model.DataId;
import com.knowledge.hoge.connect.service.system.search.model.OwnerId;
import com.knowledge.hoge.connect.service.system.search.model.SearchDataType;
import com.knowledge.hoge.connect.service.system.search.model.SearchModel;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstraction of searchable data that structured for this application.
 *
 * @author iz-j, ~~~~
 *
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public final class SearchData {
    private final SearchDataType dataType;
    private final OwnerId ownerId;
    private final DataId dataId;

    private final SearchSource fields;
    private final SearchModel original;

    private final boolean synced;

    public <T> T get(FieldType field) {
        return fields.get(field);
    }
}
