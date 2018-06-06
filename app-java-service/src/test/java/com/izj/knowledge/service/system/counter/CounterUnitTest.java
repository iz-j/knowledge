package com.izj.knowledge.service.system.counter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.UUID;

import org.junit.Test;

import com.izj.knowledge.service.system.counter.CounterDefinition;
import com.izj.knowledge.service.system.counter.CounterUnit;

public class CounterUnitTest {

    @Test
    public void test() {
        assertThat(new CounterUnit(CounterDefinition.TEST)
            .add(UUID.fromString("0-0-0-0-0"))
            .add(Year.of(1970))
            .toKey()
                , is("00000000-0000-0000-0000-000000000000#1970"));

        assertThat(new CounterUnit(CounterDefinition.TEST)
            .add(UUID.fromString("0-0-0-0-0"))
            .add(YearMonth.of(1970, 1))
            .toKey()
                , is("00000000-0000-0000-0000-000000000000#197001"));

        assertThat(new CounterUnit(CounterDefinition.TEST)
            .add(UUID.fromString("0-0-0-0-0"))
            .add(LocalDate.of(1970, 1, 1))
            .toKey()
                , is("00000000-0000-0000-0000-000000000000#19700101"));
    }
}
