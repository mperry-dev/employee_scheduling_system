package tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashSet;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.junit.jupiter.api.Test;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import employeeschedulingsystem.*;

public class AvailabilityTest {
    @Test
    public void testTimePeriodStartGetter() {
        DateTime start = new DateTime(2021, 7, 9, 10, 30);
        DateTime end = new DateTime(2021, 7, 9, 11, 30);
        Availability availability = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), start, end);
        assertEquals(start, availability.getStart());
    }

    @Test
    public void testTimePeriodEndGetter() {
        DateTime start = new DateTime(2021, 7, 9, 10, 30);
        DateTime end = new DateTime(2021, 7, 9, 11, 30);
        Availability availability = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), start, end);
        assertEquals(end, availability.getEnd());
    }

    @Test
    public void testTimePeriodInterval(){
        DateTime start = new DateTime(2021, 7, 9, 10, 30);
        DateTime end = new DateTime(2021, 7, 9, 11, 30);
        Availability availability = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), start, end);
        // https://www.epochconverter.com/
        assertEquals(new Interval(1625790600000L, 1625794200000L), availability.getInterval());
    }

    @Test
    public void testTimePeriodGetTypes(){
        DateTime start = new DateTime(2021, 7, 9, 10, 30);
        DateTime end = new DateTime(2021, 7, 9, 11, 30);
        Availability availability = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), start, end);
        assertEquals(new HashSet<String>(Arrays.asList("online", "in-person")), availability.getTypes());
    }

    @Test
    public void testTimePeriodGetTypesIncludingNull(){
       DateTime start = new DateTime(2021, 7, 9, 10, 30);
       DateTime end = new DateTime(2021, 7, 9, 11, 30);
       Availability availability = new Availability(new HashSet<>(Arrays.asList("online", "in-person", null)), start, end);
       assertEquals(new HashSet<String>(Arrays.asList("online", "in-person", null)), availability.getTypes());
    }

    @Test
    public void testTimePeriodNullTypesInput(){
       DateTime start = new DateTime(2021, 7, 9, 10, 30);
       DateTime end = new DateTime(2021, 7, 9, 11, 30);
       assertThrows(AssertionError.class, ()->new Availability(null, start, end));
    }

    @Test
    public void testReflexivityOverlappingTimePeriods(){
        // TODO = do property based test?
        DateTime startAvailability1 = new DateTime(2021, 7, 9, 10, 30);
        DateTime endAvailability1 = new DateTime(2021, 7, 9, 11, 30);
        DateTime startAvailability2 = new DateTime(2021, 7, 9, 11, 0);
        DateTime endAvailability2 = new DateTime(2021, 7, 9, 12, 0);
        Availability availability1 = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), startAvailability1, endAvailability1);
        Availability availability2 = new Availability(new HashSet<>(Arrays.asList("online", "in-person")), startAvailability2, endAvailability2);
        assertTrue(availability1.overlapsWithTimePeriod(availability2));
        assertTrue(availability2.overlapsWithTimePeriod(availability1));
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(Availability.class)
            .usingGetClass()
            // don't have final fields since inheriting from TimePeriod, and TimePeriod forced to have empty constructor since shift modified by optaplanner (in employee field)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(Availability.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
