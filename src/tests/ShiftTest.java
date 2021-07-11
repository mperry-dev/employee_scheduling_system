package tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import employeeschedulingsystem.*;

public class ShiftTest {
    @Test
    public void testShiftNullDateTime(){
        assertThrows(AssertionError.class, ()->new Shift("shift123", "online", null, null));
    }

    @Test
    public void testShiftStartAfterEnd(){
        assertThrows(AssertionError.class, ()->new Shift("shift123", "online", new DateTime(2021, 7, 9, 11, 0), new DateTime(2021, 7, 9, 10, 0)));
    }

    @Test
    public void testShiftStartAtEnd(){
        assertThrows(AssertionError.class, ()->new Shift("shift123", "online", new DateTime(2021, 7, 9, 10, 0), new DateTime(2021, 7, 9, 10, 0)));
    }

    @Test
    public void testShiftNullShiftId(){
        assertThrows(AssertionError.class, ()->new Shift(null, "online", new DateTime(2021, 7, 9, 9, 0), new DateTime(2021, 7, 9, 10, 0)));
    }

    @Test
    public void testShiftNullShiftType(){
        assertThrows(AssertionError.class, ()->new Shift("shift123", null, new DateTime(2021, 7, 9, 9, 0), new DateTime(2021, 7, 9, 10, 0)));
    }

    @Test
    public void testEquals(){
        EqualsVerifier.forClass(Shift.class)
            .usingGetClass()
            // don't have final fields since want to be able to change availabilities list when add more (also optaplanner requires empty constructor)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void checkShiftAcross2WeeksReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2021, 7, 10, 10, 0), new DateTime(2021, 7, 14, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(27, 2021, 1);
        expectedResult.put(28, 2021, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void checkShiftAcross3WeeksReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2021, 7, 10, 10, 0), new DateTime(2021, 7, 21, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(27, 2021, 1);
        expectedResult.put(28, 2021, 1);
        expectedResult.put(29, 2021, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void checkShiftIn1WeekReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2021, 7, 10, 10, 0), new DateTime(2021, 7, 11, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(27, 2021, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void shiftAcross2YearsWith2WeeksReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2020, 12, 31, 10, 0), new DateTime(2021, 1, 4, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(53, 2020, 1);
        expectedResult.put(1, 2021, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void shiftAcross2YearsWith4WeeksReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2020, 12, 24, 10, 0), new DateTime(2021, 1, 11, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(52, 2020, 1);
        expectedResult.put(53, 2020, 1);
        expectedResult.put(1, 2021, 1);
        expectedResult.put(2, 2021, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void shiftAcross3YearsReflected(){
        Shift shift = new Shift("shift123", "online", new DateTime(2020, 12, 24, 10, 0), new DateTime(2022, 1, 11, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(52, 2020, 1);
        expectedResult.put(53, 2020, 1);
        for (int w=1; w<=52; w++){
            expectedResult.put(w, 2021, 1);
        }
        expectedResult.put(1, 2022, 1);
        expectedResult.put(2, 2022, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void shiftAcross4YearsReflected(){
        // useful tool for checking tests https://www.timeanddate.com/date/weeknumber.html
        Shift shift = new Shift("shift123", "online", new DateTime(2019, 12, 21, 10, 0), new DateTime(2022, 1, 11, 10, 0));
        Table<Integer, Integer, Integer> expectedResult = HashBasedTable.create();
        expectedResult.put(51, 2019, 1);
        expectedResult.put(52, 2019, 1);
        for (int w=1; w<=53; w++){
            expectedResult.put(w, 2020, 1);
        }
        for (int w=1; w<=52; w++){
            expectedResult.put(w, 2021, 1);
        }
        expectedResult.put(1, 2022, 1);
        expectedResult.put(2, 2022, 1);
        assertEquals(expectedResult, shift.getStartEndWeekInfoPack());
    }

    @Test
    public void testShiftCsv() throws IllegalArgumentException, IllegalAccessException{
        Shift shift = new Shift("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0));
        Employee employee = new Employee("employee1", "Bob", "+61400578921", "someemail@email.com", 0, null, 0);
        ClassMemberExposer.setFieldValueByName(shift, "employee", employee);
        assertArrayEquals(new String[]{"shift123", "employee1", "9/12/2019 9:00", "11/01/2022 10:00", "online"}, shift.getShiftCsvString());
    }

    @Test
    public void testShiftCsvAssertWhenNoEmployee(){
        Shift shift = new Shift("shift123", "online", new DateTime(2019, 12, 9, 9, 0), new DateTime(2022, 1, 11, 10, 0));
        assertThrows(AssertionError.class, ()->shift.getShiftCsvString());
    }

    @Test
    public void testToString(){
        ToStringVerifier.forClass(Shift.class)
                    .withClassName(NameStyle.SIMPLE_NAME)
                    .verify();
    }
}
