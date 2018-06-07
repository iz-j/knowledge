package com.knowledge.hoge.connect.service.system.search.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Objects;
import com.knowledge.hoge.connect.service.system.search.internal.SearchData;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author iz-j
 *
 */
@ToString
@EqualsAndHashCode
public class SearchOutcome {
    private final long found;
    private final String cursor;
    private final List<SearchData> hits;

    public SearchOutcome(long found, String cursor, List<SearchData> hits) {
        this.found = found;
        this.cursor = cursor;
        this.hits = hits;
    }

    /**
     * Return the number of found.
     *
     * @return found
     */
    public long getFound() {
        return this.found;
    }

    /**
     * Return the key to retrieve next page of search results.
     *
     * @return cursor
     */
    public String getCursor() {
        return this.cursor;
    }

    /**
     * Return the list of hit data.<br>
     * Note that all searchable field values may contain highlighting expression ('em' tag).
     *
     * @return hits
     */
    public List<SearchData> getHits() {
        return this.hits;
    }

    public List<SearchData> getHits(SearchDataType dataType) {
        return this.hits
            .stream()
            .filter(e -> Objects.equal(e.getDataType(), dataType))
            .collect(Collectors.toList());
    }

    /**
     * @return true if there are no search results
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(hits);
    }

    public boolean isEmpty(SearchDataType dataType) {
        return CollectionUtils.isEmpty(getHits(dataType));
    }

    /**
     * @return true if there are some search results
     */
    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(hits);
    }
}
