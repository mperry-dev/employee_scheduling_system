package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import employeeschedulingsystem.*;

public class ShiftLoaderTest {
    @Test
    public void testValidShiftLoader(){
        ShiftLoader loader = new ShiftLoader("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0));
        Shift shift = loader.getShift();
        assertEquals((new Shift("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0))), shift);
    }

    @Test
    public void testInvalidShiftLoaderVariablesNotInitialized(){
        ShiftLoader loader = new ShiftLoader();
        assertThrows(AssertionError.class, ()->loader.getShift());
    }

    @Test
    public void testInvalidInputShiftIdShiftLoader(){
        assertThrows(AssertionError.class, ()->new ShiftLoader(null, "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0)));
    }

    @Test
    public void testInvalidInputShiftTypeShiftLoader(){
        assertThrows(AssertionError.class, ()->new ShiftLoader("shift123", null, new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0)));
    }

    @Test
    public void testInvalidInputShiftStartShiftLoader(){
        assertThrows(AssertionError.class, ()->new ShiftLoader("shift123", "online", null, new DateTime(2022, 1, 11, 10, 0)));
    }

    @Test
    public void testInvalidInputShiftEndShiftLoader(){
        assertThrows(AssertionError.class, ()->new ShiftLoader("shift123", "online", new DateTime(2019, 12, 9, 9, 0), null));
    }

    // for these 2 tests, system should be checking if they're invalid before getting shift
    @Test
    public void testInvalidShiftCreationStartAfterEndShiftLoader(){
        ShiftLoader loader = new ShiftLoader("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2018, 1, 11, 10, 0));
        assertThrows(AssertionError.class, ()->loader.getShift());
    }

    @Test
    public void testInvalidShiftCreationStartAtEndShiftLoader(){
        ShiftLoader loader = new ShiftLoader("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2019, 12, 9, 9, 0));
        assertThrows(AssertionError.class, ()->loader.getShift());
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(ShiftLoader.class)
            .usingGetClass()
            // can't have final fields since opencsv requires ability to insert fields, whilst also running empty constructor
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(ShiftLoader.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
