package com.knowledge.hoge.connect.service.system.search.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.knowledge.hoge.connect.service.base.i18n.MLString;
import com.knowledge.hoge.connect.service.base.i18n.MLStrings;
import com.knowledge.hoge.connect.service.system.search.internal.AmazonCloudSearchFormatter;

import lombok.Getter;

public final class HighlightUtils {
    public static final String PRE_TAG = "<em>";
    public static final String POST_TAG = "</em>";
    public static final String ELLIPSIS_WORD = "…";
    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("(" + PRE_TAG + ").*(" + POST_TAG + ")");
    private static final Pattern TAGS_PATTERN = Pattern.compile("(" + PRE_TAG + "|" + POST_TAG + ")");

    private HighlightUtils() {
    }

    public static boolean isHighlighted(MLString value) {
        if (value == null) {
            return false;
        }

        for (Entry<Locale, String> e : value.entrySet()) {
            if (isHighlighted(e.getValue())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHighlighted(String value) {
        if (value == null) {
            return false;
        }
        Matcher m = HIGHLIGHT_PATTERN.matcher(value);
        return m.find();
    }

    public static List<int[]> tagIndexesOf(String src) {
        List<int[]> result = Lists.newArrayList();

        if (StringUtils.isEmpty(src)) {
            return result;
        }

        int preTag = 0;
        int postTag = 0;
        int from = 0;
        while (true) {
            preTag = src.indexOf(PRE_TAG, from);
            postTag = src.indexOf(POST_TAG, from);
            from = postTag + POST_TAG.length();

            if (preTag < 0 || postTag < 0) {
                break;
            } else {
                result.add(new int[] { preTag, postTag });
            }
        }

        return result;
    }

    @Getter
    public static class HighlightRange {
        private final int start;
        private final int end;

        public HighlightRange(int start, int end) {
            super();
            this.start = start;
            this.end = end;
        }
    }

    public static MLString restoreFullOrigin(MLString searchResult, MLString origin) {
        if (MLStrings.isEmpty(searchResult)) {
            return null;
        }

        return new MLString(searchResult, (locale, resultStr) -> {
            String originStr = MLStrings.get(origin, locale);
            String restored = restoreFullOrigin(resultStr, originStr);
            return restored;
        });
    }

    public static String restoreFullOrigin(String searchResult, String origin) {
        return restoreOrigin(searchResult, origin, true);
    }

    public static MLString restoreOrigin(MLString searchResult, MLString origin) {
        if (MLStrings.isEmpty(searchResult)) {
            return null;
        }

        return new MLString(searchResult, (locale, resultStr) -> {
            String originStr = MLStrings.get(origin, locale);
            String restored = restoreOrigin(resultStr, originStr);
            return restored;
        });
    }

    public static String restoreOrigin(String searchResult, String origin) {
        return restoreOrigin(searchResult, origin, false);
    }

    /**
     * restore searchText to highlighted original text
     * 
     * @param searchResult
     * @param origin
     * @param restoreFulltext
     *            true -> return full original text<br>
     *            false -> return searchResult ranged original text<br>
     * 
     * @return
     */
    private static String restoreOrigin(String searchResult, String origin, boolean restoreFulltext) {
        if (StringUtils.isEmpty(searchResult)) {
            return searchResult;
        }

        String notagHL = HighlightUtils.removeTag(searchResult);

        String normaledOrigin = AmazonCloudSearchFormatter.normalize(origin);
        String normaledNotagHL = AmazonCloudSearchFormatter.reverseNormalize(notagHL);
        String normaledHL = AmazonCloudSearchFormatter.reverseNormalize(searchResult);

        int hlIndex = normaledOrigin.indexOf(normaledNotagHL);
        List<int[]> hlTagIndexes = HighlightUtils.tagIndexesOf(normaledHL);

        StringBuilder sb;
        int correctIndex = 0;

        if (restoreFulltext) {
            sb = new StringBuilder(origin);
            correctIndex = hlIndex;
        } else {
            String hlPickupOrigin = origin.substring(hlIndex, hlIndex + normaledNotagHL.length());
            sb = new StringBuilder(hlPickupOrigin);
        }

        for (int[] indexes : hlTagIndexes) {
            sb.insert(indexes[0] + correctIndex, HighlightUtils.PRE_TAG);
            sb.insert(indexes[1] + correctIndex, HighlightUtils.POST_TAG);
        }
        String restored = sb.toString();

        return restored;
    }

    /**
     * remove tags from highlighted string
     * 
     * @param highlighted
     * @return
     */
    public static MLString removeTag(MLString highlighted) {
        return new MLString(highlighted, (locale, string) -> removeTag(string));
    }

    /**
     * remove tags from highlighted string
     * 
     * @param highlighted
     * @return
     */
    public static String removeTag(String highlighted) {
        return highlighted.replaceAll(TAGS_PATTERN.pattern(), "");
    }

    /**
     * @param mlString
     * @return Highlighted locale string
     */
    public static String getHighlighted(MLString nullable) {
        if (Objects.isNull(nullable)) {
            return null;
        }

        for (Entry<Locale, String> e : nullable.entrySet()) {
            if (isHighlighted(e.getValue())) {
                return e.getValue();
            }
        }

        return null;
    }

    /**
     * @param mlString
     * @return Highlighted locale string or default string
     */
    public static String getHighlightedOrDefault(MLString nullable) {
        String highlighted = getHighlighted(nullable);
        if (Objects.nonNull(highlighted)) {
            return highlighted;
        }

        return MLStrings.getDefault(nullable);
    }

    /**
     * <pre>
     * Find last separator before {@link HighlightUtils#PRE_TAG}
     * and pickup substring after the last separator as shorter than maxLength.
     * 
     * When not found {@link HighlightUtils#PRE_TAG}, return substring(0, maxLength). 
     * 
     * When text shorted, insert {@link HighlightUtils#ELLIPSIS_WORD} to before or after of text.
     * </pre>
     * 
     * @param separator
     *            like " "
     * @param maxLength
     *            result's max length, 0 if unlimited.
     * @param highlighted
     *            target highlighted String
     * @return
     */
    public static String pickHighlightContext(String separator, int maxLength, String highlighted) {
        if (StringUtils.isEmpty(highlighted)) {
            return highlighted;
        }

        int firstTag = highlighted.indexOf(HighlightUtils.PRE_TAG);
        int lastSeparator = highlighted.lastIndexOf(separator, firstTag);

        if (0 <= lastSeparator) {
            highlighted = ELLIPSIS_WORD + highlighted.substring(lastSeparator + separator.length());
        }

        if ((0 < maxLength) && (maxLength < highlighted.length())) {
            highlighted = highlighted.substring(0, maxLength) + ELLIPSIS_WORD;
        }

        return highlighted;
    }

}
