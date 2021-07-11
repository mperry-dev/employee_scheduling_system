package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import employeeschedulingsystem.*;

public class AllocationsTest {
    @Test
    public void testValidityHelperGetScore(){
        Allocations allocations = new Allocations();
        assertThrows(AssertionError.class, ()->allocations.getScore());
    }

    @Test
    public void testValidityHelperGetShifts(){
        Allocations allocations = new Allocations();
        assertThrows(AssertionError.class, ()->allocations.getShifts());
    }

    @Test
    public void testConstructorInvalidShifts(){
        assertThrows(AssertionError.class, ()->new Allocations(null, new ArrayList<>()));
    }

    @Test
    public void testConstructorInvalidEmployees(){
        assertThrows(AssertionError.class, ()->new Allocations(new ArrayList<>(), null));
    }

    @Test
    public void testConstructorInvalidShiftsAndEmployees(){
        assertThrows(AssertionError.class, ()->new Allocations(null, null));
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(Allocations.class)
            .usingGetClass()
            // // can't have final fields since optaplanner requires ability to insert fields, whilst also running constructor
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(Allocations.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
