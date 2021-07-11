package tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import employeeschedulingsystem.*;

public class AllocationsConstraintProviderTest {
    ConstraintVerifier<AllocationsConstraintProvider, Allocations> constraintVerifier;

    @BeforeEach
    public void before(){
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("employeesSchedulingSolverConfig.xml");
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSecondsSpentLimit(10L);
        solverConfig.setTerminationConfig(terminationConfig);

        constraintVerifier = ConstraintVerifier.create(solverConfig);
    }

    @Test
    public void testEmployeeAvailabilityConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 10, 30)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeAvailabilityConflict)
            .given(shift, employee)
            .penalizesBy(1);
    }

    @Test
    public void testEmployeeAvailabilitySmallLeftConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 10, 30)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 9, 59), new DateTime(2021, 7, 9, 10, 30));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeAvailabilityConflict)
            .given(shift, employee)
            .penalizesBy(1);
    }

    @Test
    public void testEmployeeAvailabilitySmallRightConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 10, 30)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 10, 31));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeAvailabilityConflict)
            .given(shift, employee)
            .penalizesBy(1);
    }

    @Test
    public void testEmployeeAvailabilityNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeAvailabilityConflict)
            .given(shift, employee)
            .penalizesBy(0);
    }

    @Test
    public void testEmployeeShiftsAtSameTimeNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 12, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 11, 0), new DateTime(2021, 7, 9, 12, 0));
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeShiftsAtSameTimeConflict)
            .given(shift, shift2, employee)
            .penalizesBy(0);
    }

    @Test
    public void testEmployeeShiftsAtSameTimeFullConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 12, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeShiftsAtSameTimeConflict)
            .given(shift, shift2, employee)
            .penalizesBy(1);
    }

    @Test
    public void testEmployeeShiftsAtSameTimeSmallConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 12, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 59), new DateTime(2021, 7, 9, 12, 0));
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeShiftsAtSameTimeConflict)
            .given(shift1, shift2, employee)
            .penalizesBy(1);
    }

    @Test
    public void testEmployeeTypesNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::typeConflict)
            .given(shift, employee)
            .penalizesBy(0);
    }

    @Test
    public void testEmployeeTypesNoConflictDueToNull() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", null, new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::typeConflict)
            .given(shift, employee)
            .penalizesBy(0);
    }

    @Test
    public void testEmployeeTypesConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "in-person", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::typeConflict)
            .given(shift, employee)
            .penalizesBy(1);
    }

    @Test
    public void testAboveMaxWeeklyShiftsNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, 2, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::aboveMaxWeeklyShiftsConflict)
            .given(shift, employee)
            .penalizesBy(0);
    }

    @Test
    public void testAboveMaxWeeklyShiftsAlmostConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, 2, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::aboveMaxWeeklyShiftsConflict)
            .given(shift1, shift2, employee)
            .penalizesBy(0);
    }

    @Test
    public void testAboveMaxWeeklyShiftsConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, 2, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift3 = new Shift("shift3", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift3, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::aboveMaxWeeklyShiftsConflict)
            .given(shift1, shift2, shift3, employee)
            .penalizesBy(1);
    }

    @Test
    public void testAboveMaxWeeklyShifts9ShiftToNullNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 0, null, 0);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift3 = new Shift("shift3", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift4 = new Shift("shift4", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift5 = new Shift("shift5", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift6 = new Shift("shift6", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift7 = new Shift("shift7", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift8 = new Shift("shift8", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift9 = new Shift("shift9", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift3, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift4, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift5, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift6, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift7, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift8, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift9, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::aboveMaxWeeklyShiftsConflict)
            .given(shift1, shift2, shift3, shift4, shift5, shift6, shift7, shift8, shift9, employee)
            .penalizesBy(0);
    }

    @Test
    public void testMinWeeklyShiftsConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 1, null, 2);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeMinimumWeeklyShifts)
            .given(shift, employee)
            .penalizesBy(1);
    }

    @Test
    public void testMinWeeklyShiftsConflictTripleImportance() throws IllegalArgumentException, IllegalAccessException{
        // check more importance means bigger penalty
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 3, null, 2);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeMinimumWeeklyShifts)
            .given(shift, employee)
            .penalizesBy(3);
    }

    @Test
    public void testMinWeeklyShiftsAlmostConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 1, null, 2);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeMinimumWeeklyShifts)
            .given(shift1, shift2, employee)
            .penalizesBy(0);
    }

    @Test
    public void testMinWeeklyShiftsNoConflict() throws IllegalArgumentException, IllegalAccessException{
        Employee employee = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 1, null, 2);
        employee.addAvailabilities(new ArrayList<>(Arrays.asList(new AvailabilityLoader("employee1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0)))));
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift3 = new Shift("shift3", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee);
        ClassMemberExposer.setFieldValueByName(shift3, "employee", employee);
        constraintVerifier.verifyThat(AllocationsConstraintProvider::employeeMinimumWeeklyShifts)
            .given(shift1, shift2, shift3, employee)
            .penalizesBy(0);
    }

    @Test
    public void checkGapsBetweenShifts() throws IllegalArgumentException, IllegalAccessException{
        Employee employee1 = new Employee("employee1", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 1, null, 0);
        Employee employee2 = new Employee("employee2", "Bob Smith", "+61400789345", "Bob@noneistantemail21345654326.com", 1, null, 0);
        // penalty of 4 (waited in total 4 hours 35 minutes, rounded down to 4)
        Shift shift1 = new Shift("shift1", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 11, 0));
        Shift shift2 = new Shift("shift2", "online", new DateTime(2021, 7, 9, 11, 0), new DateTime(2021, 7, 9, 11, 30));
        Shift shift3 = new Shift("shift3", "online", new DateTime(2021, 7, 9, 12, 35), new DateTime(2021, 7, 9, 13, 0));
        Shift shift4 = new Shift("shift4", "online", new DateTime(2021, 7, 9, 16, 30), new DateTime(2021, 7, 9, 17, 0));

        // penalty of 1+2=3 (since on separate days, gap between shift6 and shift7 isn't counted)
        Shift shift5 = new Shift("shift5", "online", new DateTime(2021, 7, 9, 9, 0), new DateTime(2021, 7, 9, 10, 0));
        Shift shift6 = new Shift("shift6", "online", new DateTime(2021, 7, 9, 11, 0), new DateTime(2021, 7, 9, 11, 30));
        Shift shift7 = new Shift("shift7", "online", new DateTime(2021, 7, 10, 4, 0), new DateTime(2021, 7, 10, 6, 0));
        Shift shift8 = new Shift("shift8", "online", new DateTime(2021, 7, 10, 8, 0), new DateTime(2021, 7, 10, 10, 0));

        ClassMemberExposer.setFieldValueByName(shift1, "employee", employee1);
        ClassMemberExposer.setFieldValueByName(shift2, "employee", employee1);
        ClassMemberExposer.setFieldValueByName(shift3, "employee", employee1);
        ClassMemberExposer.setFieldValueByName(shift4, "employee", employee1);
        ClassMemberExposer.setFieldValueByName(shift5, "employee", employee2);
        ClassMemberExposer.setFieldValueByName(shift6, "employee", employee2);
        ClassMemberExposer.setFieldValueByName(shift7, "employee", employee2);
        ClassMemberExposer.setFieldValueByName(shift8, "employee", employee2);

        // penalized by 7 since sum penalty for each employee
        constraintVerifier.verifyThat(AllocationsConstraintProvider::smallerGapsBetweenShifts)
            .given(shift1, shift2, shift3, shift4, shift5, shift6, shift7, shift8, employee1, employee2)
            .penalizesBy(7);
    }
}
