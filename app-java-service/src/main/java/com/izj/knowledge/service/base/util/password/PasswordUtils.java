package com.izj.knowledge.service.base.util.password;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.izj.knowledge.service.base.time.SystemClock;

/**
 * Handles all requirements about password.
 *
 * @author iz-j
 *
 */
public final class PasswordUtils {

    private static final long PASSWORD_EFFECTIVE_DAYS = 180;

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(-1, secureRandom());

    private static final Pattern VALID_PATTERN = Pattern
        .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!-/:-@\\[-`{-~])[!-~]{8,128}$");

    private PasswordUtils() {
    }

    /**
     * @return password for initial
     */
    public static String createInitial() {
        return RandomStringUtils.random(16, 0, 0, true, true, null, secureRandom());
    }

    /**
     * @param rawPassword
     * @return encodedPassword
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * @param rawPassword
     * @param encodedPassword
     * @return true if both are matched
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * Test the validity of password.<br>
     * Password must fill following conditions.
     * <ul>
     * <li>over 8 characters</li>
     * <li>consisted of numeric or upper alphabet or lower alphabet or hyphen</li>
     * <li>contains 3 type characters of above</li>
     * </ul>
     *
     * @param rawPassword
     * @return true if fill the requirement
     */
    public static boolean isValid(String rawPassword) {
        Matcher m = VALID_PATTERN.matcher(rawPassword);
        return m.find();
    }

    /**
     * @return password expiration date
     */
    public static ZonedDateTime nextExpirationDate() {
        return SystemClock
            .now()
            .plusDays(PASSWORD_EFFECTIVE_DAYS)
            .truncatedTo(ChronoUnit.HOURS);
    }

    private static SecureRandom secureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
