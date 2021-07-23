package com.roster123.employeescheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.roster123.employeescheduler.exceptions.*;
import com.roster123.employeescheduler.domain.*;

/**
 * check behaviour when something not input for a field in CSV file.
 * 
 * Importantly, only custom checked exceptions should be thrown from system!
 */
public class EmployeeSchedulingSystemNullInputBehaviourTest {
    private EmployeeSchedulingSystem employeeSchedulingSystem;

    @BeforeEach
    public void setup(){
        employeeSchedulingSystem = new EmployeeSchedulingSystem();
    }

    @Test
    public void testNullEmployeeId(){
        assertThrows(CsvInputInvalidException.class,
        ()->{employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            ",John Smith,+61400578922,someemail2@email.com\n");});
    }

    @Test
    public void testLackingNumberOfFields(){
        assertThrows(CsvInputInvalidException.class,
        ()->{employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124\n");});
    }

    @Test
    public void testEmptyEmployeeName() throws DuplicatePlanningIdException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException{
        employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat124,,+61400578922,someemail2@email.com\n");
        Employee employee = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees").get(0);
        assertEquals("", employee.getName());
    }

    @Test
    public void testEmptyEmployeeMobile() throws DuplicatePlanningIdException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException{
        employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat124,Bob Smith,,someemail2@email.com\n");
        Employee employee = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees").get(0);
        assertNull(employee.getMobile());
    }

    @Test
    public void testNoEmployeeMobileColumn() throws DuplicatePlanningIdException, IllegalArgumentException, IllegalAccessException, CsvInputInvalidException{
        employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,email\n"+
            "cat124,Bob Smith,someemail2@email.com\n");
        Employee employee = ClassMemberExposer.<List<Employee>>getFieldValueByName(employeeSchedulingSystem, "employees").get(0);
        assertNull(employee.getMobile());
    }

    @Test
    public void testEmptyEmployeeEmail(){
        assertThrows(CsvInputInvalidException.class,
        ()->{employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "cat124,Bob Smith,+61400578922,\n");});
    }

    @Test
    public void testResultOfInputtingEmptyCsv(){
        assertThrows(CsvInputInvalidException.class,
        ()->{employeeSchedulingSystem.processEmployeeInformationCsv(
            "employeeId,name,mobile,email\n"+
            "\n");});
    }

    @Test
    public void testInvalidCsvInvalidAvailabilityEmployeeId(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            ",,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvInvalidAvailabilityEmployeeIdNotMatchingEmployee(){
        assertThrows(AvailabilityEmployeeIdNotMatchingAnyEmployeeException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat125,,03/07/2021 09:00,03/07/2021 9:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvInvalidAvailabilityStartAfterEnd(){
        assertThrows(StartAfterEndException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvInvalidAvailabilityStartAtEnd(){
        assertThrows(StartAfterEndException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 9:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvInvalidShiftStartAtEnd() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(StartAfterEndException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,03/07/2021 09:00,03/07/2021 9:00,shift123\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftStartAfterEnd() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(StartAfterEndException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,03/07/2021 09:01,03/07/2021 9:00,shift123\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftEmptyType() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                ",03/07/2021 09:00,03/07/2021 10:00,shift123\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftMissingTypeField() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "start,end,shiftId\n"+
                "03/07/2021 09:00,03/07/2021 10:00,shift123\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftDuplicateId() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(DuplicatePlanningIdException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,shift123\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,shift123\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftEmptyShiftId() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,shift124\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftMissingShiftIdField() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftEmptyStart() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,,03/07/2021 10:00,shift123\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,shift124\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftMissingStart() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,end,shiftId\n"+
                "online,03/07/2021 10:00,shift123\n"+
                "online,03/07/2021 10:00,shift124\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftEmptyEnd() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,end,shiftId\n"+
                "online,03/07/2021 9:00,,shift123\n"+
                "online,03/07/2021 09:00,03/07/2021 10:00,shift124\n"
            ));
    }

    @Test
    public void testInvalidCsvInvalidShiftMissingEndField() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(CsvInputInvalidException.class,
        ()->employeeSchedulingSystem.processShiftsCsv(
                "type,start,shiftId\n"+
                "online,03/07/2021 9:00,shift123\n"+
                "online,03/07/2021 09:00,shift124\n"
            ));
    }

    @Test
    public void testInvalidCsvAvailabilityStartAfterEndException(){
        assertThrows(StartAfterEndException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvShiftStartAfterEndException() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(StartAfterEndException.class,
        () -> {employeeSchedulingSystem.processShiftsCsv(
            "type,start,end,shiftId\n"+
            "online,03/07/2021 09:00,03/07/2021 8:55,shift123\n");});
    }

    @Test
    public void testInvalidCsvAvailabilityDuplicatePlanningIdException(){
        assertThrows(DuplicatePlanningIdException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat123,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvShiftDuplicatePlanningIdException() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(DuplicatePlanningIdException.class,
        () -> {employeeSchedulingSystem.processShiftsCsv(
            "type,start,end,shiftId\n"+
            "online,03/07/2021 09:00,03/07/2021 10:00,shift123\n"+
            "online,03/07/2021 09:00,03/07/2021 10:55,shift123\n");});
    }

    @Test
    public void testInvalidCsvEmployeeDuplicatePlanningIdExceptionAvailabilityStartAfterEndException(){
        // verify when have duplicate planning id and start after end when loading employee info/availability out, the planning id problem comes first (as we check employee info first)
        assertThrows(DuplicatePlanningIdException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat123,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvShiftDuplicatePlanningIdExceptionStartAfterEndException() throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        // verify when have duplicate planning id and start after end when loading shift, the start after end problem is revealed first (because we check each shift line by line first)
        employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 10:00\n"+
            "cat124,,30/01/2021 03:00,06/02/2021 21:00\n");
        assertThrows(StartAfterEndException.class,
        () -> {employeeSchedulingSystem.processShiftsCsv(
            "type,start,end,shiftId\n"+
            "online,03/07/2021 09:00,03/07/2021 8:00,shift123\n"+
            "online,03/07/2021 09:00,03/07/2021 10:55,shift123\n");});
    }

    @Test
    public void testInvalidCsvInvalidMobile(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,123,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvEmptyEmployeeId(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            ",John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvEmptyEmail(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile,email\n"+
            "cat123,Bob Smith,+61400578921,someemail@email.com\n"+
            "cat124,John Smith,+61400578922,\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvMissingEmployeeIdField(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "name,mobile,email\n"+
            "Bob Smith,+61400578921,someemail@email.com\n"+
            "John Smith,+61400578922,someemail2@email.com\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }

    @Test
    public void testInvalidCsvMissingEmailField(){
        assertThrows(CsvInputInvalidException.class,
        () -> {employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(
            "employeeId,name,mobile\n"+
            "cat123,Bob Smith,+61400578921\n"+
            "cat124,John Smith,+61400578922\n",

            "employeeId,type,start,end\n"+
            "cat123,,03/07/2021 09:00,03/07/2021 8:55\n"+
            "cat123,,30/01/2021 03:00,06/02/2021 21:00\n");});
    }
}
