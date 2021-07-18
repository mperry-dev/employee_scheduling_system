package com.roster123.employeescheduler.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.apache.commons.collections4.ListUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import com.roster123.employeescheduler.ClassMemberExposer;
import com.roster123.employeescheduler.loaders.AvailabilityLoader;


public class EmployeeTest {
    @Test
    public void testInvalidConstructorNullEmployeeId(){
        assertThrows(AssertionError.class, ()->{new Employee(null, "Bob", "+61400578921", "someemail@email.com", 0, null, 0);});
    }

    @Test
    public void testInvalidConstructorEmptyEmployeeId(){
        assertThrows(AssertionError.class, ()->{new Employee("", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);});
    }

    @Test
    public void testInvalidConstructorEmptyMobile(){
        assertThrows(AssertionError.class, ()->{new Employee("employee1", "Bob", "", "someemail@email.com", 0, null, 0);});
    }

    @Test
    public void testInvalidConstructorInvalidMobile(){
        assertThrows(AssertionError.class, ()->{new Employee("employee1", "Bob", "+abc", "someemail@email.com", 0, null, 0);});
    }

    @Test
    public void testInvalidConstructorNullEmail(){
        assertThrows(AssertionError.class, ()->{new Employee("employee1", "Bob", "+61400578921", null, 0, null, 0);});
    }

    @Test
    public void testInvalidConstructorInvalidEmail(){
        assertThrows(AssertionError.class, ()->{new Employee("employee1", "Bob", "+61400578921", "abc", 0, null, 0);});
    }

    @Test
    public void testEmployeeAddNullAvailabilities(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        assertThrows(AssertionError.class, ()->{employee.addAvailabilities(null);});
    }

    @Test
    public void testEmployeeAddNullAvailability(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        assertThrows(AssertionError.class, ()->{employee.addAvailability(null);});
    }

    @Test
    public void testEmployeeCheckNullShiftByType(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        assertThrows(AssertionError.class, ()->{employee.canPerformShiftByType(null);});
    }

    @Test
    public void testEmployeeCheckNullShiftByTime(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        assertThrows(AssertionError.class, ()->{employee.canPerformShiftByTime(null);});
    }

    @Test
    public void testEmployeeAddAvailabilitiesWithExistingAvailabilitiesWithTypes() throws IllegalArgumentException, IllegalAccessException{
        AvailabilityLoader av1 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av2 = new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av3 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 10, 30), new DateTime(2021, 7, 11, 10, 40));
        AvailabilityLoader av4 = new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 11, 10, 30), new DateTime(2021, 7, 11, 10, 40));
        AvailabilityLoader av5 = new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 11, 10, 50), new DateTime(2021, 7, 11, 11, 0));
        AvailabilityLoader av6 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 10, 50), new DateTime(2021, 7, 11, 10, 55));
        AvailabilityLoader av7 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 10, 35), new DateTime(2021, 7, 11, 10, 55));

        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(av1, av2, av3, av4)));
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(av5, av6, av7)));
        
        List<Availability> expectedAvailabilities = new ArrayList<>(Arrays.asList(
            new Availability(new HashSet<>(Arrays.asList("online", "in-person")), new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12)),
            new Availability(new HashSet<>(Arrays.asList("online", "in-person")), new DateTime(2021, 7, 11, 10, 30), new DateTime(2021, 7, 11, 10, 35)),
            new Availability(new HashSet<>(Arrays.asList("online", "in-person", null)), new DateTime(2021, 7, 11, 10, 35), new DateTime(2021, 7, 11, 10, 40)),
            new Availability(new HashSet<>(Arrays.asList((String)null)), new DateTime(2021, 7, 11, 10, 40), new DateTime(2021, 7, 11, 10, 50)),
            new Availability(new HashSet<>(Arrays.asList("in-person", "online", null)), new DateTime(2021, 7, 11, 10, 50), new DateTime(2021, 7, 11, 10, 55)),
            new Availability(new HashSet<>(Arrays.asList("in-person")), new DateTime(2021, 7, 11, 10, 55), new DateTime(2021, 7, 11, 11, 0))
        ));

        List<Availability> availabilitiesInEmployee = (List<Availability>)(ClassMemberExposer.getFieldValueByName(employee, "availabilities"));
        // check all elements appear at same frequency for both (order irrelevant)
        for (Availability av: ListUtils.union(expectedAvailabilities, availabilitiesInEmployee)) {
            assertEquals(Collections.frequency(expectedAvailabilities, av), Collections.frequency(availabilitiesInEmployee, av));
        }
    }

    @Test
    public void testEmployeeAddAvailabilitiesIncludingNulls() throws IllegalArgumentException, IllegalAccessException{
        AvailabilityLoader av0 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 11), new DateTime(2021, 7, 9, 10, 12));
        AvailabilityLoader av1 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av2 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av3 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av4 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av5 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12));
        AvailabilityLoader av6 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 11), new DateTime(2021, 7, 9, 10, 12));
        
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(av0, av1, av2, av3, av4, av5, av6)));

        List<Availability> expectedAvailabilities = new ArrayList<>(Arrays.asList(
            new Availability(new HashSet<>(Arrays.asList("online", null)), new DateTime(2021, 7, 11, 10, 11), new DateTime(2021, 7, 11, 10, 12)),
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 9, 10, 11), new DateTime(2021, 7, 9, 10, 12))
        ));

        List<Availability> availabilitiesInEmployee = (List<Availability>)(ClassMemberExposer.getFieldValueByName(employee, "availabilities"));
        // check all elements appear at same frequency for both (order irrelevant)
        for (Availability av: ListUtils.union(expectedAvailabilities, availabilitiesInEmployee)) {
            assertEquals(Collections.frequency(expectedAvailabilities, av), Collections.frequency(availabilitiesInEmployee, av));
        }
    }

    @Test
    public void testEmployeeCheckCanDoShiftAbuttingAvailabilities(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 11, 10, 0), new DateTime(2021, 7, 11, 13, 0));
        AvailabilityLoader av0 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 9, 0), new DateTime(2021, 7, 11, 12, 0));
        AvailabilityLoader av1 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 12, 0), new DateTime(2021, 7, 11, 13, 0));
        employee.addAvailability(av0);
        employee.addAvailability(av1);
        assertTrue(employee.canPerformShiftByType(shift));
    }

    @Test
    public void testEmployeeCheckCannotDoShiftNonAbuttingAvailabilities(){
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 11, 10, 0), new DateTime(2021, 7, 11, 13, 0));
        AvailabilityLoader av0 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 9, 0), new DateTime(2021, 7, 11, 12, 0));
        AvailabilityLoader av1 = new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 11, 12, 1), new DateTime(2021, 7, 11, 13, 0));
        employee.addAvailability(av0);
        employee.addAvailability(av1);
        assertFalse(employee.canPerformShiftByType(shift));
    }

    @Test
    public void checkMergingOfAbuttingAvailabilitiesWithSameTypes() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        AvailabilityLoader av0 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 9, 0), new DateTime(2021, 7, 11, 12, 0));
        AvailabilityLoader av1 = new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 11, 12, 0), new DateTime(2021, 7, 11, 13, 0));
        employee.addAvailability(av0);
        employee.addAvailability(av1);
        List<Availability> availabilitiesInEmployee = (List<Availability>)(ClassMemberExposer.getFieldValueByName(employee, "availabilities"));
        List<Availability> expectedAvailabilities = new ArrayList<>(Arrays.asList(
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 11, 9, 0), new DateTime(2021, 7, 11, 13, 0))
        ));
        assertArrayEquals(expectedAvailabilities.toArray(), availabilitiesInEmployee.toArray());
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(Employee.class)
            .usingGetClass()
            // don't have final fields since want to be able to change availabilities list when add more
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(Employee.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
