package com.izj.knowledge.service.base.time;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 *
 * @author ~~~~
 *
 */
public final class SystemClock {
    private static Clock baseClock = Clock.systemDefaultZone();
    private static final ThreadLocal<Clock> CLOCKS = new ThreadLocal<Clock>();

    private SystemClock() {
    }

    public static void setSystemTimeZone(TimeZone timezone) {
        baseClock = Clock.system(timezone.toZoneId());
        TimeZone.setDefault(timezone);
    }

    public static ZoneId getSystemZone() {
        return TimeZone.getDefault().toZoneId();
    }

    public static void setSystemOffsetByMillis(long millis) {
        CLOCKS.set(Clock.offset(baseClock, Duration.ofMillis(millis)));
    }

    public static void setSystemOffsetBySeconds(long seconds) {
        CLOCKS.set(Clock.offset(baseClock, Duration.ofSeconds(seconds)));
    }

    public static void setSystemOffsetByMinutes(long minutes) {
        CLOCKS.set(Clock.offset(baseClock, Duration.ofMinutes(minutes)));
    }

    public static void setSystemOffsetByHours(long hours) {
        CLOCKS.set(Clock.offset(baseClock, Duration.ofHours(hours)));
    }

    public static void setSystemOffsetByDays(long days) {
        CLOCKS.set(Clock.offset(baseClock, Duration.ofDays(days)));
    }

    public static ZonedDateTime now() {
        Clock clock = CLOCKS.get();
        return ZonedDateTime.now(clock == null ? baseClock : clock);
    }

}
