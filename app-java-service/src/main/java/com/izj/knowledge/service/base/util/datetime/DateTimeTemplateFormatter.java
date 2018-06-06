package com.izj.knowledge.service.base.util.datetime;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;

import org.apache.commons.lang3.tuple.Pair;

interface DateTimeTemplateFormatter {
    static final String SECOND = "datetime.second";
    static final String SECONDS = "datetime.seconds";
    static final String MINUTE = "datetime.minute";
    static final String MINUTES = "datetime.minutes";
    static final String HOUR = "datetime.hour";
    static final String HOURS = "datetime.hours";
    static final String DAY = "datetime.day";
    static final String DAYS = "datetime.days";
    static final String WEEK = "datetime.week";
    static final String WEEKS = "datetime.weeks";
    static final String MONTH = "datetime.month";
    static final String MONTHS = "datetime.months";
    static final String YEAR = "datetime.year";
    static final String YEARS = "datetime.years";
    static final String JUSTNOW = "datetime.justnow";

    static final String FORMAT_BEFORE = "datetime.format.before";
    static final String FORMAT_AFTER = "datetime.format.after";

    static final String FORMAT_LOCALE_YM = "datetime.format.ym";
    static final String FORMAT_LOCALE_YMD = "datetime.format.ymd";
    static final String FORMAT_LOCALE_YMDHM = "datetime.format.ymdhm";
    static final String FORMAT_LOCALE_M = "datetime.format.m";
    static final String FORMAT_LOCALE_MD = "datetime.format.md";
    static final String FORMAT_LOCALE_MDHM = "datetime.format.mdhm";
    static final String FORMAT_LOCALE_HM = "datetime.format.hm";

    static final EnumMap<ChronoUnit, Pair<String, String>> CHRONO_UNITS = new EnumMap<ChronoUnit, Pair<String, String>>(
            ChronoUnit.class) {
        private static final long serialVersionUID = 1L;
        {
            put(ChronoUnit.SECONDS, Pair.of(SECOND, SECONDS));
            put(ChronoUnit.MINUTES, Pair.of(MINUTE, MINUTES));
            put(ChronoUnit.HOURS, Pair.of(HOUR, HOURS));
            put(ChronoUnit.DAYS, Pair.of(DAY, DAYS));
            put(ChronoUnit.WEEKS, Pair.of(WEEK, WEEKS));
            put(ChronoUnit.MONTHS, Pair.of(MONTH, MONTHS));
            put(ChronoUnit.YEARS, Pair.of(YEAR, YEARS));
        }
    };

    String format(ZonedDateTime source);

}
