package com.roster123.employeescheduler.loaders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;


public class TimePeriodLoaderTest {
    private class NullStartEndTimePeriodLoaderImplementation extends TimePeriodLoader{
        public NullStartEndTimePeriodLoaderImplementation(){
            super();
        }
    }

    private class EndBeforeStartTimePeriodLoaderImplementation extends TimePeriodLoader{
        public EndBeforeStartTimePeriodLoaderImplementation(){
            super(new DateTime(2021, 9, 7, 0, 0), new DateTime(2021, 9, 7, 0, 0).minusDays(1));
        }
    }

    private class EndMatchesStartTimePeriodLoaderImplementation extends TimePeriodLoader{
        public EndMatchesStartTimePeriodLoaderImplementation(){
            super(new DateTime(2021, 9, 7, 0, 0), new DateTime(2021, 9, 7, 0, 0));
        }
    }

    @Test
    public void testNullAssertCheckingOverlap(){
        assertThrows(AssertionError.class,()->{(new NullStartEndTimePeriodLoaderImplementation()).checkValidTimePeriod();});
    }

    @Test
    public void testGetStartNull(){
        assertThrows(AssertionError.class,()->{(new NullStartEndTimePeriodLoaderImplementation()).getStart();});
    }

    @Test
    public void testGetEndNull(){
        assertThrows(AssertionError.class,()->{(new NullStartEndTimePeriodLoaderImplementation()).getEnd();});
    }

    @Test
    public void testEndBeforeStartAssert(){
        assertFalse((new EndBeforeStartTimePeriodLoaderImplementation()).checkValidTimePeriod());
    }

    @Test
    public void testEndMatchesStartAssert(){
        assertFalse((new EndMatchesStartTimePeriodLoaderImplementation()).checkValidTimePeriod());
    }
}
