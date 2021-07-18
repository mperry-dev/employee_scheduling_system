package com.roster123.employeescheduler.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import com.roster123.employeescheduler.domain.Employee;


public class EmployeeLoaderTest {
    @Test
    public void testCreateValidEmployeeLoader(){
        EmployeeLoader loader = new EmployeeLoader("employee1", "Bob Smith", "+61400578921", "abc@email.com", 0, null, 0);
        assertEquals(new Employee("employee1", "Bob Smith", "+61400578921", "abc@email.com", 0, null, 0), loader.getEmployee());
    }

    @Test
    public void testCreateValidEmployeeLoaderEmptyStringMobile(){
        EmployeeLoader loader = new EmployeeLoader("employee1", "Bob Smith", null, "abc@email.com", 0, null, 0);
        assertEquals(new Employee("employee1", "Bob Smith", null, "abc@email.com", 0, null, 0), loader.getEmployee());
    }

    @Test
    public void testNullFieldEmployeeLoader(){
        EmployeeLoader loader = new EmployeeLoader();
        assertThrows(AssertionError.class, ()->loader.getEmployee());
    }

    @Test
    public void testInvalidByNullEmployeeId(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader(null, "Bob Smith", "+61400578921", "abc@email.com", 0, null, 0));
    }

    @Test
    public void testInvalidByEmptyEmployeeId(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader("", "Bob Smith", "+61400578921", "abc@email.com", 0, null, 0));
    }

    @Test
    public void testInvalidByNullEmail(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader("employee1", "Bob Smith", "+61400578921", null, 0, null, 0));
    }

    @Test
    public void testInvalidByEmptyEmail(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader("employee1", "Bob Smith", "+61400578921", "", 0, null, 0));
    }

    @Test
    public void testInvalidByInvalidEmail(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader("employee1", "Bob Smith", "+61400578921", "abc", 0, null, 0));
    }

    @Test
    public void testInvalidByEmptyMobile(){
        assertThrows(AssertionError.class, ()->new EmployeeLoader("employee1", "Bob Smith", "", "abc@email.com", 0, null, 0));
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(EmployeeLoader.class)
            .usingGetClass()
            // can't have final fields since opencsv requires ability to insert fields, whilst also running empty constructor
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(EmployeeLoader.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
