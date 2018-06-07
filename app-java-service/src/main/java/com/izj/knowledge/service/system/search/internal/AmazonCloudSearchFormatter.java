package com.knowledge.hoge.connect.service.system.search.internal;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator.OfInt;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.knowledge.hoge.connect.service.base.i18n.MLString;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the formatter to allow a search using the symbol in AmazonCloudSearch.
 *
 * @author ~~~~
 *
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmazonCloudSearchFormatter {
    private static final Map<Character, Character> SYMBOL_GROUPS = new HashMap<>();
    static {
        SYMBOL_GROUPS.put('［', '(');
        SYMBOL_GROUPS.put('「', '(');
        SYMBOL_GROUPS.put('{', '(');
        SYMBOL_GROUPS.put('〈', '(');
        SYMBOL_GROUPS.put('[', '(');
        SYMBOL_GROUPS.put('〔', '(');
        SYMBOL_GROUPS.put('《', '(');
        SYMBOL_GROUPS.put('【', '(');
        SYMBOL_GROUPS.put('『', '(');
        SYMBOL_GROUPS.put('｛', '(');
        SYMBOL_GROUPS.put('（', '(');
        SYMBOL_GROUPS.put('｢', '(');
        SYMBOL_GROUPS.put('≪', '(');
        SYMBOL_GROUPS.put('<', '(');

        SYMBOL_GROUPS.put('］', ')');
        SYMBOL_GROUPS.put('〉', ')');
        SYMBOL_GROUPS.put('｝', ')');
        SYMBOL_GROUPS.put('》', ')');
        SYMBOL_GROUPS.put('』', ')');
        SYMBOL_GROUPS.put('】', ')');
        SYMBOL_GROUPS.put('>', ')');
        SYMBOL_GROUPS.put('≫', ')');
        SYMBOL_GROUPS.put('〕', ')');
        SYMBOL_GROUPS.put('}', ')');
        SYMBOL_GROUPS.put('」', ')');
        SYMBOL_GROUPS.put('｣', ')');
        SYMBOL_GROUPS.put('）', ')');
        SYMBOL_GROUPS.put(']', ')');

        SYMBOL_GROUPS.put('⇒', '→');
        SYMBOL_GROUPS.put('⇐', '←');
        SYMBOL_GROUPS.put('＝', '=');
        SYMBOL_GROUPS.put('⇑', '↑');
        SYMBOL_GROUPS.put('⇓', '↓');
        SYMBOL_GROUPS.put('￥', '\\');

        SYMBOL_GROUPS.put('―', '-');
        SYMBOL_GROUPS.put('－', '-');

        SYMBOL_GROUPS.put('〃', '々');
        SYMBOL_GROUPS.put('仝', '々');
        SYMBOL_GROUPS.put('ゞ', '々');
        SYMBOL_GROUPS.put('ゝ', '々');
        SYMBOL_GROUPS.put('ヾ', '々');
        SYMBOL_GROUPS.put('ヽ', '々');

        SYMBOL_GROUPS.put('：', ':');
        SYMBOL_GROUPS.put('＊', '*');
        SYMBOL_GROUPS.put('㏍', '㈱');
        SYMBOL_GROUPS.put('～', '~');
        SYMBOL_GROUPS.put('、', ',');
        SYMBOL_GROUPS.put('＆', '&');
        SYMBOL_GROUPS.put('＾', '^');
        SYMBOL_GROUPS.put(';', ';');
        SYMBOL_GROUPS.put('！', '!');
        SYMBOL_GROUPS.put('？', '?');
        SYMBOL_GROUPS.put('＋', '+');
        SYMBOL_GROUPS.put('＿', '_');
        SYMBOL_GROUPS.put('／', '/');
    }

    private static final BiMap<Character, Character> SYMBOL_MAP = HashBiMap.create();
    static {
        SYMBOL_MAP.put('…', 'あ');
        SYMBOL_MAP.put('(', 'い');
        SYMBOL_MAP.put(')', 'う');
        SYMBOL_MAP.put('→', 'え');
        SYMBOL_MAP.put('←', 'お');
        SYMBOL_MAP.put('□', 'か');
        SYMBOL_MAP.put('■', 'き');
        SYMBOL_MAP.put('℃', 'く');
        SYMBOL_MAP.put('=', 'け');
        SYMBOL_MAP.put('㈹', 'こ');
        SYMBOL_MAP.put('↑', 'さ');
        SYMBOL_MAP.put('↓', 'し');
        SYMBOL_MAP.put('\\', 'す');
        SYMBOL_MAP.put('-', 'せ');
        SYMBOL_MAP.put('♂', 'そ');
        SYMBOL_MAP.put('♀', 'た');
        SYMBOL_MAP.put('々', 'ち');
        SYMBOL_MAP.put('♪', 'つ');
        SYMBOL_MAP.put(':', 'て');
        SYMBOL_MAP.put('×', 'と');
        SYMBOL_MAP.put('*', 'な');
        SYMBOL_MAP.put('㈱', 'に');
        SYMBOL_MAP.put('~', 'ぬ');
        SYMBOL_MAP.put('㈲', 'ね');
        SYMBOL_MAP.put(',', 'の');
        SYMBOL_MAP.put('㌔', 'は');
        SYMBOL_MAP.put('㎏', 'ひ');
        SYMBOL_MAP.put('㎞', 'ふ');
        SYMBOL_MAP.put('≒', 'へ');
        SYMBOL_MAP.put('㌘', 'ほ');
        SYMBOL_MAP.put('★', 'ま');
        SYMBOL_MAP.put('☆', 'み');
        SYMBOL_MAP.put('&', 'む');
        SYMBOL_MAP.put('^', 'め');
        SYMBOL_MAP.put(';', 'も');
        SYMBOL_MAP.put('●', 'や');
        SYMBOL_MAP.put('○', 'ゆ');
        SYMBOL_MAP.put('№', 'よ');
        SYMBOL_MAP.put('℡', 'ら');
        SYMBOL_MAP.put('◆', 'り');
        SYMBOL_MAP.put('◇', 'る');
        SYMBOL_MAP.put('!', 'れ');
        SYMBOL_MAP.put('?', 'ろ');
        SYMBOL_MAP.put('+', 'わ');
        SYMBOL_MAP.put('÷', 'を');
        SYMBOL_MAP.put('￠', 'ん');
        SYMBOL_MAP.put('￡', 'ぁ');
        SYMBOL_MAP.put('_', 'ぃ');
        SYMBOL_MAP.put('/', 'ぅ');

        SYMBOL_MAP.put('Ⅰ', '1');
        SYMBOL_MAP.put('Ⅱ', '2');
        SYMBOL_MAP.put('Ⅲ', '3');
        SYMBOL_MAP.put('Ⅳ', '4');
        SYMBOL_MAP.put('Ⅴ', '5');
        SYMBOL_MAP.put('Ⅵ', '6');
        SYMBOL_MAP.put('Ⅶ', '7');
        SYMBOL_MAP.put('Ⅷ', '8');
        SYMBOL_MAP.put('Ⅸ', '9');
    };

    private static char charNormalize(char c) {
        // Hiragana to Katakana
        if (c >= 'ぁ' && c <= 'ん') {
            return (char)(c - 'ぁ' + 'ァ');
        }
        // Half-width Katakana => Full-width Katakana
        if (c >= 'ｧ' && c <= 'ﾝ' || c >= '①' && c <= '⑨') {
            return Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFKC).charAt(0);
        }

        // Full-width alphanumeric characters => Half-width alphanumeric characters
        if ((c >= 0xFF10 && c <= 0xFF19) || (c >= 0xFF21 && c <= 0xFF3A) || (c >= 0xFF41 && c <= 0xFF5A)) {
            return (char)(c - 0xFEE0);
        }

        // Grouping specify symbols
        if (SYMBOL_GROUPS.containsKey(c)) {
            return SYMBOL_GROUPS.get(c);
        }

        return c;
    };

    private static char charSymbolMapping(char c) {
        // Symbol to Hiragana
        if (SYMBOL_MAP.containsKey(c)) {
            return SYMBOL_MAP.get(c);
        }

        return c;
    };

    private static char charSymbolReverseMapping(char c) {
        // Mapped Hiragana to original Symbol
        if (SYMBOL_MAP.inverse().containsKey(c)) {
            return SYMBOL_MAP.inverse().get(c);
        }

        log.warn("Cant reverse to Symbol from Hiragana \"{}\"", c);
        return '?';
    };

    /**
     * Format according to the following rules.
     *
     * <pre>
     * Hiragana => Full-width Katakana
     * Half-width Katakana => Full-width Katakana
     * Full-width alphanumeric characters => Half-width alphanumeric characters(lowercase letters)
     * Symbol => Hiragana
     * </pre>
     *
     * @see AmazonCloudSearchFormatter#SYMBOL_MAP
     * @param src
     * @return
     */
    public static String format(String src) {
        if (src == null) {
            return null;
        }

        StringBuilder dest = new StringBuilder();

        for (OfInt it = src.chars().iterator(); it.hasNext();) {
            char c = (char)it.nextInt();
            c = charNormalize(c);
            c = charSymbolMapping(c);
            dest.append(c);
        }

        return dest.toString().toLowerCase();
    }

    public static MLString format(MLString src) {
        if (Objects.isNull(src)) {
            return null;
        }

        return new MLString(src, (locale, srcString) -> format(srcString));
    }

    /**
     * Only normalize (intermediate state)
     * 
     * @param src
     * @return
     */
    public static String normalize(String src) {
        if (src == null) {
            return null;
        }

        StringBuilder dest = new StringBuilder();

        for (OfInt it = src.chars().iterator(); it.hasNext();) {
            char c = (char)it.nextInt();
            c = charNormalize(c);
            dest.append(c);
        }

        return dest.toString().toLowerCase();
    }

    public static MLString normalize(MLString src) {
        if (Objects.isNull(src)) {
            return null;
        }

        return new MLString(src, (locale, srcString) -> normalize(srcString));
    }

    /**
     * Reverse to normalized string from formatted string
     * 
     * @param src
     * @return
     */
    public static String reverseNormalize(String src) {
        if (src == null) {
            return null;
        }

        StringBuilder dest = new StringBuilder();
        for (OfInt it = src.chars().iterator(); it.hasNext();) {
            char c = (char)it.nextInt();

            if (c >= 'ぁ' && c <= 'ん') {
                c = charSymbolReverseMapping(c);
            }

            dest.append(c);
        }

        return dest.toString().toLowerCase();
    }

    public static MLString reverseNormalize(MLString src) {
        if (Objects.isNull(src)) {
            return null;
        }

        return new MLString(src, (locale, srcString) -> reverseNormalize(srcString));
    }

}
