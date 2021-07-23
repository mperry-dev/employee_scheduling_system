package com.roster123.employeescheduler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.javatuples.Triplet;
import org.joda.time.DateTime;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import com.roster123.employeescheduler.exceptions.*;
import com.roster123.employeescheduler.domain.*;
import com.roster123.employeescheduler.loaders.*;

// importantly, only custom checked exceptions should be thrown from system!
@RunWith(JUnitQuickcheck.class)
public class EmployeeSchedulingSystemTest {
    private EmployeeSchedulingSystem employeeSchedulingSystem;

    @BeforeEach
    public void before(){
        employeeSchedulingSystem = new EmployeeSchedulingSystem();
    }

    @Test
    public void testReadCsvEmployeeInfo() throws DuplicatePlanningIdException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException{
        employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n");
        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");

        assertArrayEquals(new Employee[]{
            new Employee("cat123", "Bob Smith", "+61400578921", "someemail@email.com", 0, null, 0),
            new Employee("cat124", "John Smith", "+61400578922", "someemail2@email.com", 0, null, 0)},
            employees.stream().toArray());
    }

    @Test
    public void testReadCsvEmployeeAvailabilities() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",
            
            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");
        
        Employee employee1 = new Employee("cat123", "Bob Smith", "+61400578921", "someemail@email.com", 0, null, 0);
        Employee employee2 = new Employee("cat124", "John Smith", "+61400578922", "someemail2@email.com", 0, null, 0);
        employee1.addAvailability(new AvailabilityLoader("cat123", null, new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0)));
        employee2.addAvailability(new AvailabilityLoader("cat124", null, new DateTime(2021, 1, 30, 3, 0), new DateTime(2021, 2, 6, 21, 0)));
        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        assertArrayEquals(new Employee[]{employee1, employee2}, employees.toArray());
    }

    @Test
    public void testAvailabilitiesDividedProperly() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,in-person\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        List<Availability> availabilities = ClassMemberExposer.<List<Availability>>getFieldValueByName(employee, "availabilities");

        Availability[] expectedAvailabilities = new Availability[]{
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 11, 0)),
            new Availability(new HashSet<>(Arrays.asList("in-person", "online")), new DateTime(2021, 7, 3, 11, 0), new DateTime(2021, 7, 3, 20, 0)),
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 3, 20, 0), new DateTime(2021, 7, 3, 21, 0))};
        assertArrayEquals(expectedAvailabilities, availabilities.toArray());
    }

    @Test
    public void testAvailabilitiesMergedProperlySameType() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        // check merging done
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,online\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        List<Availability> availabilities = ClassMemberExposer.<List<Availability>>getFieldValueByName(employee, "availabilities");

        Availability[] expectedAvailabilities = new Availability[]{
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0))};
        assertArrayEquals(expectedAvailabilities, availabilities.toArray());
    }

    @Test
    public void testAvailabilitiesCoveringShiftTime() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,online\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        Shift suggestedShift = new Shift("shift123", "online", new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0));
        List<Availability> availabilitiesCoveringShiftTime = ClassMemberExposer.<List<Availability>>genericInvokeMethod(employee, "availabilitiesFullyCoveringShiftTime", suggestedShift);
        Availability[] expectedAvailabilities = new Availability[]{
            new Availability(new HashSet<>(Arrays.asList("online")), new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0))};
        assertArrayEquals(expectedAvailabilities, availabilitiesCoveringShiftTime.toArray());
    }

    @Test
    public void testCannotCoverShiftTime() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,online\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        Shift suggestedShift = new Shift("shift123", "online", new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 22, 0));
        List<Availability> availabilitiesCoveringShiftTime = ClassMemberExposer.<List<Availability>>genericInvokeMethod(employee, "availabilitiesFullyCoveringShiftTime", suggestedShift);

        assertNull(availabilitiesCoveringShiftTime);
    }

    @Test
    public void testCannotCoverShiftType() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,online\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        Shift suggestedShift = new Shift("shift123", "in-person", new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0));
        assertFalse(employee.canPerformShiftByType(suggestedShift));
    }

    @Test
    public void testCanCoverShiftType() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n",

            "employeeId,type,start,end,type\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00,online\n"+
            "cat123,,03/07/2021 11:00,03/07/2021 20:00,online\n");

        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");
        Employee employee = employees.get(0);
        Shift suggestedShift = new Shift("shift123", "online", new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0));
        assertTrue(employee.canPerformShiftByType(suggestedShift));
    }

    @Test
    public void testMobileMissingIsOK() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, IllegalArgumentException, IllegalAccessException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,email\n"+
            "cat123,Bob Smith,someemail@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");

        AvailabilityLoader loader = new AvailabilityLoader("cat123", null, new DateTime(2021, 1, 30, 3, 0), new DateTime(2021, 2, 6, 21, 0));
        Employee employee = new Employee("cat123", "Bob Smith", null, "someemail@email.com", 0, null, 0);
        employee.addAvailability(loader);
        
        assertEquals(employee, employees.get(0));
    }

    @Test
    public void testMobileEmptyIsOK() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, IllegalArgumentException, IllegalAccessException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,,someemail@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        List<Employee> employees = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees");

        AvailabilityLoader loader = new AvailabilityLoader("cat123", null, new DateTime(2021, 1, 30, 3, 0), new DateTime(2021, 2, 6, 21, 0));
        Employee employee = new Employee("cat123", "Bob Smith", null, "someemail@email.com", 0, null, 0);
        employee.addAvailability(loader);
        
        assertEquals(employee, employees.get(0));
    }
    
    @Test
    public void testRunForCsvShifts() throws DuplicatePlanningIdException, StartAfterEndException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",
            
            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 21:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");
        employeeSchedulingSystem.processShiftsCsv(
            "type,start,end,shiftId\n"+
            "online,03/07/2021 09:00,03/07/2021 12:00,shift123\n"
        );
        Employee employee =  new Employee("cat123", "Bob Smith", "+61400578921", "someemail@email.com", 0, null, 0);
        employee.addAvailability(new AvailabilityLoader("cat123", null, new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 21, 0)));
        Shift newShift = new Shift("shift123", "online", new DateTime(2021, 7, 3, 9, 0), new DateTime(2021, 7, 3, 12, 0));
        ClassMemberExposer.setFieldValueByName(newShift, "employee", employee);
        employeeSchedulingSystem.allocateShifts(10L);
        List<Shift> shifts = ClassMemberExposer.<List<Shift>>getFieldValueByName(employeeSchedulingSystem, "allocatedShifts");
        assertArrayEquals(new Shift[]{newShift}, shifts.toArray());
    }

    @ParameterizedTest
    @ValueSource(strings={"example1_significant_working_example", "example3_trivial_working_example", "example4_simple_working_example",
                          "example5_simpler_working_example", "example6_even_simpler_working_example", "example7_complex_example"})
    public void testWorkingExamples(String inputFolderName) throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        Path commandLineCsvFolder = EmployeeSchedulingSystem.getPathOfCsvFolder("CSVs_command_line").resolve(inputFolderName);
        
        Triplet<String, String, String> csvData = EmployeeSchedulingSystem.getDataFromFiles(commandLineCsvFolder);
        String employeeAvailabilityCsvString = csvData.getValue0();
        String employeeInformationCsvString = csvData.getValue1();
        String shiftInformationCsvString = csvData.getValue2();

        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(employeeInformationCsvString, employeeAvailabilityCsvString);
        employeeSchedulingSystem.processShiftsCsv(shiftInformationCsvString);
        HardMediumSoftScore score = employeeSchedulingSystem.allocateShifts(null);
        assertTrue(score.isFeasible());
    }

    @ParameterizedTest
    @ValueSource(strings={"example2_impossible_example"})
    public void testImpossibleExamples(String inputFolderName) throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        Path commandLineCsvFolder = EmployeeSchedulingSystem.getPathOfCsvFolder("CSVs_command_line").resolve(inputFolderName);
        
        Triplet<String, String, String> csvData = EmployeeSchedulingSystem.getDataFromFiles(commandLineCsvFolder);
        String employeeAvailabilityCsvString = csvData.getValue0();
        String employeeInformationCsvString = csvData.getValue1();
        String shiftInformationCsvString = csvData.getValue2();

        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(employeeInformationCsvString, employeeAvailabilityCsvString);
        employeeSchedulingSystem.processShiftsCsv(shiftInformationCsvString);
        HardMediumSoftScore score = employeeSchedulingSystem.allocateShifts(null);
        assertFalse(score.isFeasible());
    }
    

    @Property(trials=1000)
    public void propertyQuickcheckTest(@InRange(min="0", max="1000") int someNumber){
        assertEquals(someNumber, someNumber);
    }
}
