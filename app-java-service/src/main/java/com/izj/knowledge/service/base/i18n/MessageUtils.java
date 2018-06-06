package com.izj.knowledge.service.base.i18n;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ~~~~
 *
 */
public final class MessageUtils {

    private MessageUtils() {
    }

    /**
     * Build fullName with given firstName & lastName, and return it.<br>
     * e.g. firstName = Taro, lastName = Yamada<br>
     * When in Japanese, return 'Yamada Taro', when in English, return 'Taro Yamada'.
     *
     * @param firstName
     * @param lastName
     * @return fullName
     */
    public static String getFullName(String firstName, String lastName) {
        if (StringUtils.isEmpty(firstName)) {
            return lastName;
        }
        if (StringUtils.isEmpty(lastName)) {
            return firstName;
        }
        return Messages.get("fullName", firstName, lastName);
    }

    /**
     * Build fullName with given firstName & lastName, and return it.<br>
     * e.g. firstName = Taro, lastName = Yamada<br>
     * When in Japanese, return 'Yamada Taro', when in English, return 'Taro Yamada'.
     *
     * @param firstName
     * @param lastName
     * @return fullName
     */
    private static String getFullNameByLocale(Locale locale, MLString firstName, MLString lastName) {
        if (StringUtils.isEmpty(firstName.get(locale))) {
            return lastName.get(locale);
        }
        if (StringUtils.isEmpty(lastName.get(locale))) {
            return firstName.get(locale);
        }
        return Messages.get("fullName", locale, firstName.get(locale), lastName.get(locale));
    }

    /**
     * Build fullName with given firstName & lastName, and returns it with honorific.<br>
     * e.g. firstName = Taro, lastName = Yamada<br>
     * When in Japanese, return 'Yamada Taro sama', when in English, return 'Mr.(Ms.) Taro Yamada'.
     *
     * @param firstName
     * @param lastName
     * @return fullName
     */
    public static String getFullNameWithHonorific(String firstName, String lastName) {
        String fullName = getFullName(firstName, lastName);
        return Messages.get("honorific", fullName);
    }

    /**
     * Build fullName with given firstName & lastName, and returns it with honorific.<br>
     * e.g. firstName = Taro, lastName = Yamada<br>
     * When in Japanese, return 'Yamada Taro sama', when in English, return 'Mr.(Ms.) Taro Yamada'.
     *
     * @param firstName
     * @param lastName
     * @return fullName
     */
    public static String withHonorific(String fullName) {
        return Messages.get("honorific", fullName);
    }

    /**
     * @see #getFullName(String, String)
     */
    public static MLString getFullName(MLString firstName, MLString lastName) {
        MLString s = new MLString();
        Arrays
            .stream(SupportedLocale.values())
            .forEach(l -> {
                Locale locale = l.get();
                String fullName = getFullNameByLocale(locale, firstName, lastName);
                if (StringUtils.isNotEmpty(fullName)) {
                    s.set(locale, fullName);
                }
            });
        return s;
    }

    /**
     * @see #getFullName(String, String)
     */
    public static String toDisplayFullName(MLString firstName, MLString lastName) {
        return getFullName(firstName.get(), lastName.get());
    }

}
