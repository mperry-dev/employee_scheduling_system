package com.roster123.employeescheduler.domain;

import java.util.Objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.roster123.employeescheduler.loaders.DateTimeCsvConverter;

/**
 * a class representing a shift which can be done by an employee
 */
@PlanningEntity
public class Shift extends TimePeriod{
    /** 
     * unique business id of the shift.
     * 
     * Cannot be null or ""
     */
    @PlanningId
    private String shiftId;

    /**
     * the shift type, such as online shift, in-person shift.
     * 
     * Null means available for anything, otherwise some string type set by the user.
     * 
     * Cannot be "" or null since the shift type required to be possible to use for preference by employee
     */
    private String type;

    /**
     * the employee currently allocated to the shift.
     * 
     * Accessible and modified by optaplanner, to optimize the allocation
     */
    @PlanningVariable(valueRangeProviderRefs = "employeeRange")
    private Employee employee;

    /**
     * no-arg constructor for optaplanner
     */
    public Shift(){
    }

    /**
     * full constructor setting all fields
     * @param type
     * @param start
     * @param end
     * @param shiftId
     */
    public Shift(String shiftId, String type, DateTime start, DateTime end) {
        super(start, end);
        validityHelper(shiftId, type);
        this.type = type;
        this.shiftId = shiftId;
    }

    /**
     * get the employee for this shift.
     * 
     * Need because the constraint provider class AllocationsConstraintProvider needs the employee object for a shift.
     * 
     * Note - whilst optaplanner changes the employee, it does this by making new objects, so the hashcode of the shift won't change for an object. So it is safe to consider the employee in equals.
     * It is also an easier design to debug with, than excluding employee, and works better for Allocations.equals
     * @return employee for this shift
     */
    public Employee getEmployee() {
        validityHelper(shiftId, type);
        return employee;
    }

    @Override
    public boolean equals(Object obj){
        if (!super.equals(obj)){
            return false;
        }
        Shift other = (Shift)obj;
        return Objects.equals(shiftId, other.shiftId)&&Objects.equals(type, other.type)&&
            Objects.equals(employee, other.employee);
    }

    @Override
    public String toString(){
        return super.toString()+", shiftId="+shiftId+", type="+type+", employee="+employee;
    }

    /**
     * get the type of the shift, e.g. online, or in-person shift
     * @return
     */
    public String getType(){
        validityHelper(shiftId, type);
        return type;
    }

    /**
     * get week of the week year, as a number (1 to 53)
     * @param subject a datetime
     * @return ISA 8601 week of week year
     */
    private static int getWeekOfWeekYear(DateTime subject){
        return Integer.parseInt(subject.weekOfWeekyear().getAsText(Locale.getDefault()));
    }

    /**
     * get week year
     * @param subject a datetime
     * @return ISA 8601 week year
     */
    private static int getWeekYear(DateTime subject){
        return Integer.parseInt(subject.weekyear().getAsText(Locale.getDefault()));
    }

    /**
     * get a table holding the start and end of this week.
     * 
     * If the start and end of the shifts are in different weeks (e.g. start Sunday evening, finish Monday morning), count as being in 2 weeks (1 shift each week).
     * 
     * This follows to say that if across multiple weeks (e.g. a 3-week continuous shift), reflect the shift for all weeks in-between too.
     * 
     * Assumes start <= end
     * @return table holding start and end of this week
     */
    public Table<Integer, Integer, Integer> getStartEndWeekInfoPack(){
        // note that week number not affected by timezone for same timezone - these 2 values below differ by 1
        // Integer.parseInt((new DateTime(2021, 7, 11, 23, 55)).weekOfWeekyear().getAsText(Locale.getDefault()));
        // Integer.parseInt((new DateTime(2021, 7, 12, 0, 05)).weekOfWeekyear().getAsText(Locale.getDefault()));
        validityHelper(shiftId, type);
        int startWeekOfYear = getWeekOfWeekYear(getStart());
        int endWeekOfYear = getWeekOfWeekYear(getEnd());
        int startYear = getWeekYear(getStart());
        int endYear = getWeekYear(getEnd());

        Table<Integer, Integer, Integer> pack = HashBasedTable.create();

        for (int year=startYear; year<=endYear; year++){
            int currentStartWeek, currentEndWeek;
            if (year == startYear){
                currentStartWeek = startWeekOfYear;
            }
            else{
                currentStartWeek = 1;
            }
            if (year == endYear){
                currentEndWeek = endWeekOfYear;
            }
            else{
                // we can get the number of ISA 8601 weeks in the year (52 or 53), by checking the week number of the 28th December.
                // https://en.wikipedia.org/wiki/ISO_week_date - note here the notes under "Last week" that says "It has 28 December in it."
                currentEndWeek = getWeekOfWeekYear(new DateTime(year, 12, 28, 12, 0));
            }

            increaseWeekYearCountTableOverWeekRange(pack, currentStartWeek, currentEndWeek, year);
        }

        return pack;
    }

    /**
     * increase the count by c for the table, or set to c if it isn't in the table (i.e. treating default value of the table as 0)
     * @param <K1> first table key type (row)
     * @param <K2> second table key type (column)
     * @param table the table to have it's values incremented
     * @param key1 first table key (row)
     * @param key2 second table key (column)
     * @param c the number to increase the count for the {key1, key2} combination in the table
     */
    private static <K1, K2> void increaseCountTable(Table<K1, K2, Integer> table, K1 key1, K2 key2, int c){
        if (table.contains(key1, key2)){
            table.put(key1, key2, table.get(key1, key2)+c);
        }
        else{
            table.put(key1, key2, c);
        }
    }

    /**
     * increase the count of each combination of year, with week in the inclusive interval startWeek->endWeek, by 1 each
     * @param table table representing the number of times a week and year combination has been counted
     * @param startWeek start week number of the range
     * @param endWeek end week number of the range
     * @param year year for which we're increasing the count for these weeks
     */
    private static void increaseWeekYearCountTableOverWeekRange(Table<Integer, Integer, Integer> table, int startWeek, int endWeek, int year){
        for (int week=startWeek; week<=endWeek; week++){
            increaseCountTable(table, week, year, 1);
        }
    }

    /**
     * sum the count tables with the same row and column types.
     * 
     * Their sum is defined as having the sum of their count values for each row/column combination they have in common,
     * and for all other row/column combinations the entry in the table which has that row/column combination.
     * 
     * Both tables must have the same row types, and the same column types
     * @param <K1> table row key type
     * @param <K2> table column key type
     * @param table1 first table
     * @param table2 second table
     * @return result table of the sum
     */
    public static <K1, K2> Table<K1, K2, Integer> addCountTables(Table<K1, K2, Integer> table1, Table<K1, K2, Integer> table2){
        Table<K1, K2, Integer> merged = HashBasedTable.create(table1);
        for (Table.Cell<K1, K2, Integer> cell: table2.cellSet()){
            increaseCountTable(merged, cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
        return merged;
    }

    /**
     * subtract table2 from table 1, and return the result.
     * 
     * Subtraction is defined as subtracting the values for all row/column combinations in table2, from that row/column combination in table1
     * where table1 lacks a row/column combination in table2, the default value in table1 will be treated as 0.
     * 
     * This operation can result in negative or 0 values for the count of the row/column combinations.
     * 
     * Both tables must have the same row types, and the same column types
     * @param <K1> table row key type
     * @param <K2> table column key type
     * @param table1 first table
     * @param table2 second table
     * @return result table of the subtraction
     */
    public static <K1, K2> Table<K1, K2, Integer> subtractCountTables(Table<K1, K2, Integer> table1, Table<K1, K2, Integer> table2){
        Table<K1, K2, Integer> difference = HashBasedTable.create(table1);
        for (Table.Cell<K1, K2, Integer> cell: table2.cellSet()){
            // difference implemented by subtracting cell value. Note this means cell value can become negative
            increaseCountTable(difference, cell.getRowKey(), cell.getColumnKey(), -cell.getValue());
        }
        return difference;
    }

    /**
     * get a CSV string array representing the shift to be added as a line into the output allocations file ALLOCATIONS_OUTPUT.csv
     * @return string array representing shift
     */
    public String[] getShiftCsvString(){
        validityHelper(shiftId, type);
        assert employee != null;  // system shouldn't be checking for csv string if haven't run the solver
        return new String[]{shiftId, employee.getEmployeeId(),
            DateTimeCsvConverter.convertDateTimeToString(getStart()),
            DateTimeCsvConverter.convertDateTimeToString(getEnd()),
            type};
    }

    /**
     * get the unique business id of the shift
     * @return shiftId
     */
    public String getShiftId(){
        validityHelper(shiftId, type);
        return shiftId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(shiftId, type, employee, super.hashCode());
    }

    /**
     * check whether the employee currently stored can do the shift by the time, for optaplanner.
     * 
     * Need employee to not be null
     * @return whether the employee can do this shift by the time
     */
    public boolean shiftEmployeeCanDoTime(){
        return employee.canPerformShiftByTime(this);
    }

    /**
     * check whether the employee currently stored can do the shift by the type, for optaplanner.
     * 
     * Need employee to not be null
     * @return whether the employee can do this shift by the type
     */
    public boolean shiftEmployeeCanDoType(){
        return employee.canPerformShiftByType(this);
    }

    /**
     * get the list with start/end pair from within this shift
     * @return list with start/end pair
     */
    public List<Pair<DateTime, DateTime>> getListWithStartEndPair(){
        return new LinkedList<Pair<DateTime, DateTime>>(Arrays.asList(new Pair<DateTime, DateTime>(getStart(), getEnd())));
    }

    /**
     * check whether matches another shift by the shift id
     * @param shift an shift to compare to
     * @return whether shift id's match
     */
    public boolean matchesShiftByShiftId(Shift shift){
        return Objects.equals(shiftId, shift.shiftId);
    } 


    /**
     * helper method to check validity of internal data.
     * 
     * Used because forced to have empty constructor
     * @param shiftId
     * @param type
     */
    private static void validityHelper(String shiftId, String type){
        assert (shiftId != null)&&(!"".equals(shiftId))&&(type != null)&&(!"".equals(type));
    }
}
