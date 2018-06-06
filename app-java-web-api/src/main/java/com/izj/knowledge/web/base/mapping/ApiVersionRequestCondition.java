package com.izj.knowledge.web.base.mapping;

import java.net.URL;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {
    private static final int FIRST = 1;
    private static final int NEWEST = 1;
    private static final String SLASH = "/";
    private final String prefix;
    private final Set<Range<Integer>> supported;

    public enum VersionRange {
        FROM, TO;
    }

    public ApiVersionRequestCondition(String prefix, Set<Range<Integer>> supported) {
        super();
        this.prefix = prefix;
        this.supported = supported;
    }

    public ApiVersionRequestCondition(String prefix, int version) {
        this(prefix, ImmutableSet.<Range<Integer>> builder().add(Range.between(version, version)).build());
    }

    public ApiVersionRequestCondition(String prefix, int from, int to) {
        this(prefix, ImmutableSet.<Range<Integer>> builder().add(Range.between(from, to)).build());
    }

    public ApiVersionRequestCondition(String prefix, VersionRange range, int value) {
        this(prefix, ImmutableSet
                .<Range<Integer>> builder()
                .add(Range.between(range == VersionRange.FROM ? value : FIRST,
                        range == VersionRange.TO ? NEWEST : value))
                .build());
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        try {
            URL uri = new URL(request.getRequestURL().toString());
            String[] paths = uri.getPath().split(SLASH);// path -> /api/external/v*
            if (paths.length > 3 && paths[2].startsWith(this.prefix)) {
                String version = paths[2];
                int current = NumberUtils.toInt(StringUtils.removeStart(version, this.prefix), 0);
                if (this.supported.stream().anyMatch(range -> {
                    return range.contains(current);
                })) {
                    return this;
                }
            }
        } catch (Exception e) {
            log.warn("Caught exception, but ignored. Returned null.", e);
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        return other.supported.size() - this.supported.size();
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        // 結合する場合はどっちのバージョンもサポートする
        return new ApiVersionRequestCondition(this.prefix, ImmutableSet
                .<Range<Integer>> builder()
                .addAll(this.supported)
                .addAll(other.supported)
                .build());
    }

}
