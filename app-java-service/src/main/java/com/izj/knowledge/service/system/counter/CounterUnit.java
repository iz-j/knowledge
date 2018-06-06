package com.izj.knowledge.service.system.counter;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * The unit value to hold a counter.<br>
 * If you want to count a number per date, create a unit as below.
 *
 * <pre>
 * LocalDate someDate = LocalDate.of(1970, 1, 1);
 * CounterUnit unit = new CounterUnit(CounterDefinition.SOME_TABLE).add(someDate);
 * </pre>
 *
 * Also, if you want to count a number per company & date, create a unit as below.
 *
 * <pre>
 * UUID companyId = UUID.randomUUID();
 * LocalDate someDate = LocalDate.of(1970, 1, 1);
 * CounterUnit unit = new CounterUnit(CounterDefinition.SOME_TABLE).add(companyId).add(someDate);
 * </pre>
 *
 * @author iz-j
 *
 */
public final class CounterUnit {
    private final CounterDefinition definition;
    private final List<String> parts = new ArrayList<>();

    public CounterUnit(CounterDefinition definition) {
        this.definition = definition;
    }

    public CounterDefinition getDefinition() {
        return this.definition;
    }

    public CounterUnit add(String part) {
        this.parts.add(part);
        return this;
    }

    public CounterUnit add(Year part) {
        // yyyy
        this.parts.add(StringUtils.leftPad(String.valueOf(part.getValue()), 4, '0'));
        return this;
    }

    public CounterUnit add(YearMonth part) {
        // yyyyMM
        this.parts.add(StringUtils.join(
                StringUtils.leftPad(String.valueOf(part.getYear()), 4, '0'),
                StringUtils.leftPad(String.valueOf(part.getMonthValue()), 2, '0')
            ));
        return this;
    }

    public CounterUnit add(LocalDate part) {
        this.parts.add(part.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        return this;
    }

    public CounterUnit add(UUID part) {
        this.parts.add(part.toString());
        return this;
    }

    public String toKey() {
        return parts.isEmpty() ? "#" : StringUtils.join(parts, "#");
    }

    @Override
    public String toString() {
        return toKey();
    }

}
