package com.izj.knowledge.service.base.i18n;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Supported locale with priority orders.<br>
 * Priority means what locale to use when no value with the contextual locale.
 *
 * @author iz-j
 *
 */
public enum SupportedLocale {
    EN(Locale.ENGLISH),
    JA(Locale.JAPANESE), ;

    private final Locale locale;

    private SupportedLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale get() {
        return locale;
    }

    public static Locale[] getCandidates() {
        return new Locale[] {
                LocaleContextHolder.getLocale(),
                EN.get(),
                JA.get()
        };
    }

    public static boolean isSupported(Locale locale) {
        return Arrays.stream(SupportedLocale.values()).anyMatch(sl -> {
            return sl.get().equals(locale);
        });
    }

    public static boolean isSupported(String locale) {
        return Arrays.stream(SupportedLocale.values()).anyMatch(sl -> {
            return StringUtils.equals(sl.get().toString(), locale);
        });
    }
}
