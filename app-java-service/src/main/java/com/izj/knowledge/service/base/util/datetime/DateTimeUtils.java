package com.izj.knowledge.service.base.util.datetime;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.izj.knowledge.service.base.i18n.Messages;
import com.izj.knowledge.service.base.time.SystemClock;

/**
 *
 * @author ~~~~
 *
 */
public final class DateTimeUtils {
    private static final String MESSAGE_CODE_THIS_MONTH = "datetime.thisMonth";
    private static final String MESSAGE_CODE_YESTERDAY = "datetime.yesterday";
    private static final String MESSAGE_CODE_TODAY = "datetime.today";
    private static final String MESSAGE_CODE_TOMORROW = "datetime.tomorrow";
    private static final String MESSAGE_CODE_THIS_WEEK = "datetime.thisWeek";
    private static final String MESSAGE_CODE_NEXT_WEEK = "datetime.nextWeek";
    private static final String MESSAGE_CODE_LATER = "datetime.later";

    private DateTimeUtils() {
    }

    /**
     * Returns formatted current year string. <br>
     * If now is 2017/01/01 then, returns '2017';
     *
     * @return
     */
    public static String currentYYYY() {
        return simpleFormat(SystemClock.now(), SimpleFormat.YYYY);
    }

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isSameDay(ZonedDateTime d1, ZonedDateTime d2) {
        return d1.getYear() == d2.getYear() && d1.getDayOfYear() == d2.getDayOfYear();
    }

    public static String simpleFormat(ZonedDateTime datetime, SimpleFormat format) {
        switch (format) {
        case YYYY:
            return datetime.format(SimpleFormat.YYYY_FORMATTER);
        case YYYYMM:
            return datetime.format(SimpleFormat.YYYYMM_FORMATTER);
        case YYYYMMW:
            int weekOfMonth = GregorianCalendar.from(datetime).get(Calendar.WEEK_OF_MONTH);
            return new StringBuilder()
                .append(datetime.format(SimpleFormat.YYYYMM_FORMATTER))
                .append(String.format("%02d", weekOfMonth))
                .toString();
        default:
            break;
        }
        return null;
    }

    public static enum SimpleFormat {
        YYYY, YYYYMM, YYYYMMW;
        private static final DateTimeFormatter YYYY_FORMATTER = DateTimeFormatter.ofPattern("YYYY");
        private static final DateTimeFormatter YYYYMM_FORMATTER = DateTimeFormatter.ofPattern("YYYYMM");
    }

    /**
     *
     * @param datetime
     * @return
     */
    public static String overview(ZonedDateTime datetime, ZoneId zone) {
        // TODO リファクタ
        ZonedDateTime zoned = datetime.withZoneSameInstant(zone);
        ZonedDateTime now = SystemClock.now().withZoneSameInstant(zone);
        if (DateTimeUtils.isSameDay(zoned, now)) {
            return StringUtils.capitalize(Messages.get(MESSAGE_CODE_TODAY));
        } else if (zoned.isBefore(now)) {
            if (DateTimeUtils.isSameDay(zoned, now.minusDays(1))) {
                return StringUtils.capitalize(Messages.get(MESSAGE_CODE_YESTERDAY));
            } else if (zoned.getYear() == now.getYear() && zoned.getMonth() == now.getMonth()) {
                return StringUtils.capitalize(Messages.get(MESSAGE_CODE_THIS_MONTH));
            } else {
                return DateTimeTemplate.FLEX_YM.format(zoned);
            }
        } else {
            if (DateTimeUtils.isSameDay(zoned, now.plusDays(1))) {
                return StringUtils.capitalize(Messages.get(MESSAGE_CODE_TOMORROW));
            } else if (now.getYear() == zoned.getYear()) {
                int targetWeek = GregorianCalendar.from(zoned).get(Calendar.WEEK_OF_MONTH);
                int week = GregorianCalendar.from(now).get(Calendar.WEEK_OF_MONTH);
                boolean sameWeek = now.getMonth() == zoned.getMonth() && targetWeek == week;
                if (sameWeek) {
                    return StringUtils.capitalize(Messages.get(MESSAGE_CODE_THIS_WEEK));
                } else {
                    long diff = ChronoUnit.DAYS.between(now, zoned);
                    return diff < 14 && (targetWeek == 1 || targetWeek - week == 1)// 翌週、もしくは月またいで1週目
                            ? StringUtils.capitalize(Messages.get(MESSAGE_CODE_NEXT_WEEK))
                            : StringUtils.capitalize(Messages.get(MESSAGE_CODE_LATER));
                }
            }
            return StringUtils.capitalize(Messages.get(MESSAGE_CODE_LATER));
        }
    }

    /**
     * convert LocalDate to ZonedDateTime with localZone,<br>
     * and return ZonedDateTime with UTC offset.
     *
     * @param date
     *            target
     * @param localZone
     *            date's zone
     * @return converted ZonedDateTime (UTC offset)
     */
    public static ZonedDateTime toZoned(LocalDate date, ZoneId localZone) {
        if (Objects.isNull(date)) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.atStartOfDay(localZone).toInstant(), SystemClock.getSystemZone());
    }

    /**
     * convert ZonedDateTime to LocalDate with localZone<br>
     *
     * @param zoned
     *            target
     * @param localZone
     *            return LocalDate's zone
     * @return converted LocalDate with localZone
     */
    public static LocalDate toLocalDate(ZonedDateTime zoned, ZoneId localZone) {
        if (Objects.isNull(zoned)) {
            return null;
        }
        return zoned.withZoneSameInstant(localZone).toLocalDate();
    }

    public static boolean isBeforeOrEquals(YearMonth basedMonth, YearMonth other) {
        return basedMonth.compareTo(other) <= 0;
    }

    public static boolean isAfterOrEquals(YearMonth basedMonth, YearMonth other) {
        return basedMonth.compareTo(other) >= 0;

    }

    private static final DateTimeFormatter RFC3339_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneId.of("UTC"));

    /**
     * <pre>
     * Format ZonedDateTime to RFC3339: yyyy-mm-ddTHH:mm:ss.SSSZ 
     * ex) "2001-12-25T00:00:00Z"
     * 
     * This format used by typically AmazonCloudSearch Date field.
     * </pre>
     * 
     * @return
     */
    public static String toRFC3339(ZonedDateTime zoned) {
        return RFC3339_FORMATTER.format(zoned);
    }
}
