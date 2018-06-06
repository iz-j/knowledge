package com.izj.knowledge.service.base.i18n;

import java.util.List;
import java.util.Locale;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 *
 * @author ~~~~
 *
 */
public final class Messages {

    private Messages() {
    }

    /**
     * @param code
     * @return localized text
     */
    public static String get(String code) {
        return get(code, getLocale());
    }

    /**
     * @param code
     * @param locale
     * @return localized text
     */
    public static String get(String code, Locale locale) {
        try {
            return MessageSourceHolder.get().getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            return "Message not found! code = " + code + ", locale = " + locale;
        }
    }

    /**
     * @param code
     * @param args
     * @return localized text
     */
    public static String get(String code, Object... args) {
        return get(code, getLocale(), args);
    }

    /**
     * @param code
     * @param locale
     * @param args
     * @return localized text
     */
    public static String get(String code, Locale locale, Object... args) {
        try {
            return MessageSourceHolder.get().getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return "Message not found! code = " + code + ", locale = " + locale;
        }
    }

    /**
     * @see #get(String, Object...)
     */
    public static String get(String code, List<Object> args) {
        return get(code, args.toArray());
    }

    /**
     * @see #get(String, Locale, Object...)
     */
    public static String get(String code, Locale locale, List<Object> args) {
        return get(code, locale, args.toArray());
    }

    /**
     * @return locale associated with the current thread
     */
    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

}
