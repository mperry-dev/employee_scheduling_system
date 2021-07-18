package com.roster123.employeescheduler.loaders;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;


public class AvailabilityLoaderTest {
    @Test
    public void testValidityHelperGetType(){
        AvailabilityLoader availabilityLoader = new AvailabilityLoader();
        assertThrows(AssertionError.class, ()->availabilityLoader.getType());
    }

    @Test
    public void testValidityHelperGetEmployeeId(){
        AvailabilityLoader availabilityLoader = new AvailabilityLoader();
        assertThrows(AssertionError.class, ()->availabilityLoader.getEmployeeId());
    }

    // for these tests, system should be checking if they're invalid before getting shift
    @Test
    public void testInvalidGetStartStartAfterEndAvailabilityLoader(){
        AvailabilityLoader loader = new AvailabilityLoader("employee1", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2018, 1, 11, 10, 0));
        assertThrows(AssertionError.class, ()->loader.getStart());
    }

    @Test
    public void testInvalidGetStartStartAtEndAvailabilityLoader(){
        AvailabilityLoader loader = new AvailabilityLoader("employee1", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 9, 9, 0));
        assertThrows(AssertionError.class, ()->loader.getStart());
    }

    @Test
    public void testInvalidGetEndStartAfterEndAvailabilityLoader(){
        AvailabilityLoader loader = new AvailabilityLoader("employee1", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2018, 1, 11, 10, 0));
        assertThrows(AssertionError.class, ()->loader.getEnd());
    }

    @Test
    public void testInvalidGetEndStartAtEndAvailabilityLoader(){
        AvailabilityLoader loader = new AvailabilityLoader("employee1", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 9, 9, 0));
        assertThrows(AssertionError.class, ()->loader.getEnd());
    }

    @Test
    public void testInvalidByEmptyType(){
        assertThrows(AssertionError.class, ()->new AvailabilityLoader("employee1", "", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 10, 9, 0)));
    }

    @Test
    public void testInvalidByEmptyEmployeeId(){
        assertThrows(AssertionError.class, ()->new AvailabilityLoader("", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 10, 9, 0)));
    }

    @Test
    public void testInvalidByNullEmployeeId(){
        assertThrows(AssertionError.class, ()->new AvailabilityLoader(null, "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 10, 9, 0)));
    }

    @Test
    public void testValidWithNullType(){
        AvailabilityLoader loader = new AvailabilityLoader("employee1", null, new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 10, 9, 0));
        assertNull(loader.getType());
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(AvailabilityLoader.class)
            .usingGetClass()
            // can't have final fields since opencsv requires ability to insert fields, whilst also running empty constructor
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(AvailabilityLoader.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
