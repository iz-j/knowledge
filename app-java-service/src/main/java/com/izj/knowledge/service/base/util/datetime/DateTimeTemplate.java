package com.izj.knowledge.service.base.util.datetime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.izj.knowledge.service.base.i18n.Messages;
import com.izj.knowledge.service.base.time.SystemClock;

public enum DateTimeTemplate implements DateTimeTemplateFormatter {

    FROM_NOW {
        @Override
        public String format(ZonedDateTime source) {
            if (source == null) {
                return StringUtils.EMPTY;
            }
            ZonedDateTime now = SystemClock.now();
            Optional<Pair<ChronoUnit, Long>> chronoUnitAndDiff = EnumSet
                .of(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS, ChronoUnit.WEEKS,
                        ChronoUnit.MONTHS, ChronoUnit.YEARS)
                .stream()
                .sorted(Collections.reverseOrder())
                .map(c -> Pair.of(c, c.between(source, now)))
                .filter(pair -> Math.abs(pair.getRight()) >= 1)
                .findFirst();
            if (chronoUnitAndDiff.isPresent()) {
                ChronoUnit chronoUnit = chronoUnitAndDiff.get().getLeft();
                long diff = Math.abs(chronoUnitAndDiff.get().getRight());
                Pair<String, String> singleAndMultiple = CHRONO_UNITS.get(chronoUnit);
                String diffLabel = diff
                        + (Messages.get(diff == 1 ? singleAndMultiple.getLeft() : singleAndMultiple.getRight()));
                boolean isAfter = chronoUnitAndDiff.get().getRight() < 0;
                return Messages.get(isAfter ? FORMAT_AFTER : FORMAT_BEFORE,
                        diffLabel);
            } else {
                return Messages.get(JUSTNOW);
            }
        }
    },

    YMD {
        @Override
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source, FORMAT_LOCALE_YMD);
        }

    },

    HM {
        @Override
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source, FORMAT_LOCALE_HM);
        }
    },

    YMDHM {
        @Override
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source, FORMAT_LOCALE_YMDHM);
        }
    },

    FLEX_YM {
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source,
                    source.getYear() == SystemClock.now().getYear() ? FORMAT_LOCALE_MD : FORMAT_LOCALE_YMD);
        }
    },

    FLEX_YMD {
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source,
                    source.getYear() == SystemClock.now().getYear() ? FORMAT_LOCALE_MD : FORMAT_LOCALE_YMD);
        }
    },

    FLEX_YMDHM {
        public String format(ZonedDateTime source) {
            return source == null ? StringUtils.EMPTY : formatBy(source,
                    source.getYear() == SystemClock.now().getYear() ? FORMAT_LOCALE_MDHM : FORMAT_LOCALE_YMDHM);
        }
    };

    private static String formatBy(ZonedDateTime source, String format) {
        return source.format(DateTimeFormatter.ofPattern(Messages.get(format), Messages.getLocale()));
    }

}
