package com.izj.knowledge.service.base.i18n;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author iz-j
 *
 */
public final class MLStrings {

    private MLStrings() {
    }

    /**
     * @param ms
     * @return true if MLString is null or empty
     */
    public static boolean isEmpty(MLString ms) {
        return ms == null ? true : ms.isEmpty();
    }

    /**
     * @param ms
     * @return true if MLString is not null and has some values
     */
    public static boolean isNotEmpty(MLString ms) {
        return ms != null && !ms.isEmpty();
    }

    /**
     * @param ms
     *            the MLString to check
     * @param s
     *            the String to find
     * @return true if MLString contains 's'
     */
    public static boolean contains(MLString ms, String s) {
        return ms.entrySet().stream().anyMatch(e -> {
            return StringUtils.contains(e.getValue(), s);
        });
    }

    /**
     * @param ms
     * @param src
     * @param dst
     * @return replaced
     */
    public static MLString replace(MLString ms, String src, String dst) {
        Set<Map.Entry<Locale, String>> replcaed = ms.entrySet().stream().map(e -> {
            return new SimpleEntry<Locale, String>(e.getKey(), StringUtils.replace(e.getValue(), src, dst));
        }).collect(Collectors.toSet());
        return new MLString(replcaed);
    }

    /**
     * @param locale
     * @param strings
     * @return collection
     */
    public static List<MLString> newList(Locale locale, Collection<String> strings) {
        return strings.stream().map(s -> new MLString(locale, s)).collect(Collectors.toList());
    }

    /**
     * @param mlString
     * @param locale
     * @return
     */
    public static String get(@Nullable MLString mlString, Locale locale) {
        return mlString != null ? mlString.get(locale) : null;
    }

    /**
     * @param mlString
     * @return
     */
    public static String getDefault(@Nullable MLString mlString) {
        return mlString != null ? mlString.get() : null;
    }

    /**
     * Return MLString instance if succeeded to convert given value.<br>
     * Otherwise, return null
     *
     * @param src
     * @return MLString or null
     */
    public static MLString tryConvert(Object src) {
        if (src == null) {
            return null;
        }
        if (src instanceof MLString) {
            return (MLString)src;
        }
        if (Map.class.isAssignableFrom(src.getClass()) && isMLStringLike((Map<?, ?>)src)) {
            MLString ms = new MLString();
            ((Map<?, ?>)src).forEach((k, v) -> {
                ms.set(LocaleUtils.toLocale((String)k), (String)v);
            });
            return ms;
        }

        return null;
    }

    /**
     * @param map
     * @return true if given map is convertible into MLString
     */
    private static boolean isMLStringLike(Map<?, ?> map) {
        return map.entrySet().stream().allMatch(kv -> {
            if (SupportedLocale.isSupported(kv.getKey().toString())) {
                return kv.getValue() instanceof String;
            } else {
                return false;
            }
        });
    }

    /**
     * Return comparator that compares MLString using the current locale.
     *
     * @param keyExtractor
     * @return comparator
     */
    public static <T> Comparator<T> comparing(Function<? super T, MLString> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T>)(c1, c2) -> {
            String s1 = keyExtractor.apply(c1).get();
            String s2 = keyExtractor.apply(c2).get();
            return StringUtils.compare(s1, s2);
        };
    }

}
