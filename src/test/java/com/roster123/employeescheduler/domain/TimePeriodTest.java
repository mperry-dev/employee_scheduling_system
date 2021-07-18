package com.roster123.employeescheduler.domain;

import java.lang.AssertionError;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * test scenarios of invalid implementation of time period (valid implementations tested with AvailabilityTest and ShiftTest)
 */
public class TimePeriodTest {
    private class NullStartEndTimePeriodImplementation extends TimePeriod{
        public NullStartEndTimePeriodImplementation(){
            super();
        }
    }

    private class EndBeforeStartTimePeriodImplementation extends TimePeriod{
        public EndBeforeStartTimePeriodImplementation(){
            super(new DateTime(2021, 9, 7, 0, 0), new DateTime(2021, 9, 7, 0, 0).minusDays(1));
        }
    }

    private class EndMatchesStartTimePeriodImplementation extends TimePeriod{
        public EndMatchesStartTimePeriodImplementation(){
            super(new DateTime(2021, 9, 7, 0, 0), new DateTime(2021, 9, 7, 0, 0));
        }
    }

    @Test
    public void testNullAssertCheckingOverlap(){
        assertThrows(AssertionError.class, ()->{(new NullStartEndTimePeriodImplementation()).overlapsWithTimePeriod(new NullStartEndTimePeriodImplementation());});
    }

    @Test
    public void testAssertCreatingIntervalFromNullDateTimes(){
        assertThrows(AssertionError.class, ()->{new NullStartEndTimePeriodImplementation().getInterval();});
    }

    @Test
    public void testEndBeforeStartAssert(){
        assertThrows(AssertionError.class, ()->{new EndBeforeStartTimePeriodImplementation();});
    }

    @Test
    public void testEndMatchesStartAssert(){
        assertThrows(AssertionError.class, ()->{new EndMatchesStartTimePeriodImplementation();});
    }
}
