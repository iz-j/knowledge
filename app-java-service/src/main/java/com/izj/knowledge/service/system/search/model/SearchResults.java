package com.knowledge.hoge.connect.service.system.search.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import com.amazonaws.services.cloudsearchdomain.model.Bucket;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.google.common.collect.Sets;
import com.knowledge.hoge.connect.service.system.search.SearchModelConverter;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchFieldConverter;
import com.knowledge.hoge.connect.service.system.search.model.SearchFacetParam.SearchBucketParam;
import com.knowledge.hoge.connect.service.system.search.model.SearchSource.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j;

/**
 * 
 * @author ~~~~
 *
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@Log4j
public class SearchResults {
    private final Set<SearchDataType> dataTypes;
    private final long found;
    private final String cursor;
    private final List<SearchModel> hits;
    private final Map<String, Map<String, SearchBucket>> facets;

    public List<? extends SearchModel> getHits(SearchDataType dataType) {
        return hits
            .stream()
            .filter(e -> dataType == e.getDataType())
            .collect(Collectors.toList());
    }

    public void completeHits(Function<SearchModel, SearchModel> mapper) {
        IntStream
            .range(0, hits.size())
            .forEach(i -> {
                SearchModel model = hits.get(i);
                model = mapper.apply(model);
                hits.set(i, model);
            });
    }

    public void completeHits(SearchDataType dataType, Function<SearchModel, SearchModel> mapper) {
        IntStream
            .range(0, hits.size())
            .forEach(i -> {
                SearchModel model = hits.get(i);
                if (model.getDataType() == dataType) {
                    model = mapper.apply(model);
                    hits.set(i, model);
                }
            });
    }

    public void completeBuckets(String facetName, Function<SearchBucket, SearchBucket> mapper) {
        Assert.isTrue(facets.containsKey(facetName), "Facet result [" + facetName + "] Not found.");

        Map<String, SearchBucket> completed = facets
            .get(facetName)
            .values()
            .stream()
            .map(mapper)
            .collect(Collectors.toMap(SearchBucket::getName, e -> e));

        facets.put(facetName, completed);
    }

    public static SearchResults of(SearchParam param, SearchResult res) {
        Map<SearchDataType, SearchModelConverter<?>> converters = SearchDataType
            .instanthiateConverters(param.getDataTypes());

        List<SearchModel> hits = readHits(res, converters);

        Map<String, Map<String, SearchBucket>> facets = readFacets(res, param);

        return SearchResults
            .builder()
            .dataTypes(Sets.newHashSet(param.getDataTypes()))
            .found(res.getHits().getFound())
            .cursor(res.getHits().getCursor())
            .hits(hits)
            .facets(facets)
            .build();
    }

    private static List<SearchModel> readHits(SearchResult res,
            Map<SearchDataType, SearchModelConverter<?>> converters) {
        List<SearchModel> hits = res
            .getHits()
            .getHit()
            .stream()
            .map(hit -> {
                SearchDataType dataType = SearchDataType.valueOf(hit.getFields().get("data_type").get(0));
                SearchModelConverter<?> converter = converters.get(dataType);
                return AmazonCloudSearchFieldConverter
                    .toSearchData(hit.getFields(), hit.getHighlights(), converter);
            })
            .map(data -> {
                SearchModelConverter<?> converter = converters.get(data.getDataType());
                try {
                    return converter.mergeHighlights(data);
                } catch (Throwable t) {
                    log.error("Can't convert SearchData to SearchModel: " + data.getDataType(), t);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return hits;
    }

    private static Map<String, Map<String, SearchBucket>> readFacets(SearchResult res, SearchParam param) {
        Map<String, Map<String, SearchBucket>> facets = res
            .getFacets()
            .entrySet()
            .stream()
            .map(facet -> {
                String facetFieldName = facet.getKey().toUpperCase();
                FieldType field = EnumUtils.getEnum(FieldType.class, facetFieldName);

                // repair facet name
                String facetName = Optional
                    .ofNullable(param.findFacetOf(field))
                    .map(SearchFacetParam::getName)
                    .orElse(field.name());

                // bucket list
                List<SearchBucket> bucketList = facet
                    .getValue()
                    .getBuckets()
                    .stream()
                    .map(SearchBucket::of)
                    .collect(Collectors.toList());

                // repair bucket name
                bucketList = bucketList.stream().map(bucket -> {
                    String name = java.util.Optional
                        .ofNullable(param.findBucketOf(field, bucket.getValue()))
                        .map(SearchBucketParam::getName)
                        .orElse(bucket.getValue());
                    return new SearchBucket(name, bucket.value, bucket.count);
                }).collect(Collectors.toList());

                // bucket map
                Map<String, SearchBucket> bucketMap = bucketList
                    .stream()
                    .collect(Collectors.toMap(SearchBucket::getName, e -> e));

                return Pair.of(facetName, bucketMap);
            })
            .collect(Collectors.toMap(e -> e.getLeft(), e -> e.getRight()));

        return facets;
    }

    @Data
    @Builder(toBuilder = true)
    public static class SearchBucket {
        private final String name;
        private final String value;
        private final long count;

        private static SearchBucket of(Bucket bucket) {
            return new SearchBucket(bucket.getValue(), bucket.getValue(), bucket.getCount());
        }
    }

}
