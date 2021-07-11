package employeeschedulingsystem;

import java.util.Objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Locale;

import org.joda.time.DateTime;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * a class representing a shift which can be done by an employee
 */
@PlanningEntity
public class Shift extends TimePeriod{
    /** 
     * unique business id of the shift
     * <p>
     * cannot be null or ""
     */
    @PlanningId
    private String shiftId;

    /**
     * the shift type, such as online shift, in-person shift
     * <p>
     * null means available for anything, otherwise some string type set by the user
     * <p>
     * cannot be "" or null since the shift type required to be possible to use for preference by employee
     */
    private String type;

    /**
     * the employee currently allocated to the shift
     * <p>
     * accessible and modified by optaplanner, to optimize the allocation
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
     * get the employee for this shift
     * <p>
     * need because the constraint provider class {@link AllocationsConstraintProvider} needs the employee object for a shift
     * <p>
     * note - whilst optaplanner changes the employee, it does this by making new objects, so the hashcode of the shift won't change for an object. So it is safe to consider the employee in equals.
     * it is also an easier design to debug with, than excluding employee, and works better for Allocations.equals
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
     * get a table holding the start and end of this week
     * <p>
     * if the start and end of the shifts are in different weeks (e.g. start Sunday evening, finish Monday morning), count as being in 2 weeks (1 shift each week)
     * <p>
     * this follows to say that if across multiple weeks (e.g. a 3-week continuous shift), reflect the shift for all weeks in-between too
     * @return table holding start and end of this week
     */
    public Table<Integer, Integer, Integer> getStartEndWeekInfoPack(){
        // note that week number not affected by timezone for same timezone - these 2 values below differ by 1
        // Integer.parseInt((new DateTime(2021, 7, 11, 23, 55)).weekOfWeekyear().getAsText(Locale.getDefault()));
        // Integer.parseInt((new DateTime(2021, 7, 12, 0, 05)).weekOfWeekyear().getAsText(Locale.getDefault()));
        validityHelper(shiftId, type);
        int startWeekOfYear = Integer.parseInt(getStart().weekOfWeekyear().getAsText(Locale.getDefault()));
        int endWeekOfYear = Integer.parseInt(getEnd().weekOfWeekyear().getAsText(Locale.getDefault()));
        int startYear = Integer.parseInt(getStart().weekyear().getAsText(Locale.getDefault()));
        int endYear = Integer.parseInt(getEnd().weekyear().getAsText(Locale.getDefault()));

        Table<Integer, Integer, Integer> pack = HashBasedTable.create();
        
        if (startYear == endYear){
            // in same year. Assume end week after start week, since time period assumption applies that start before end.
            for (int week=startWeekOfYear; week<=endWeekOfYear; week++){
                increaseCountTable(pack, week, startYear, 1);
            }
            return pack;
        }
        else{
            // loop over years - the start year is before the end year
            for (int year=startYear; year<=endYear; year++){
                // weeks span from 1 to 52 or 53. For example, (new DateTime(2021, 01, 3, 12, 0)).weekyear().getAsText() gives 2020,
                // (new DateTime(2021, 01, 3, 12, 0)).weekOfWeekyear().getAsText() gives 53,
                // (new DateTime(2021, 01, 4, 12, 0)).weekyear().getAsText() gives 2021,
                // (new DateTime(2021, 01, 4, 12, 0)).weekOfWeekyear().getAsText() gives 1
                
                // we can get the number of ISA 8601 weeks in the year, by checking the week number of the 28th December. Can't use 29th, 30th, 31st December as that might be part of the subsequent year.
                // https://en.wikipedia.org/wiki/ISO_week_date - note here the notes under "Last week" that says "It has 28 December in it."
                int numberWeeksInYear = Integer.parseInt((new DateTime(year, 12, 28, 12, 0)).weekOfWeekyear().getAsText(Locale.getDefault()));
                
                // loop over weeks, keeping in mind it could be in the first or last year
                // across multiple years, so have to have special cases for first year, last year, and in between years
                // start by checking if in start year
                if (year == startYear){
                    for (int week=startWeekOfYear; week<=numberWeeksInYear; week++){
                        increaseCountTable(pack, week, year, 1);
                    }
                }
                else if (year == endYear){
                    for (int week=1; week<=endWeekOfYear; week++){
                        increaseCountTable(pack, week, year, 1);
                    }
                }
                else{
                    for (int week=1; week<=numberWeeksInYear; week++){
                        increaseCountTable(pack, week, year, 1);
                    }
                }
            }
            return pack;
        }
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
     * sum the count tables with the same row and column types
     * <p>
     * their sum is defined as having the sum of their count values for each row/column combination they have in common,
     * and for all other row/column combinations the entry in the table which has that row/column combination
     * <p>
     * both tables must have the same row types, and the same column types
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
     * subtract table2 from table 1, and return the result
     * <p>
     * subtraction is defined as subtracting the values for all row/column combinations in table2, from that row/column combination in table1
     * where table1 lacks a row/column combination in table2, the default value in table1 will be treated as 0
     * <p>
     * this operation can result in negative or 0 values for the count of the row/column combinations
     * <p>
     * both tables must have the same row types, and the same column types
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
            DateTimeCsvLoader.convertDateTimeToString(getStart()),
            DateTimeCsvLoader.convertDateTimeToString(getEnd()),
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
     * helper method to check validity of internal data
     * <p>
     * used because forced to have empty constructor
     * @param shiftId
     * @param type
     */
    private static void validityHelper(String shiftId, String type){
        assert (shiftId != null)&&(!"".equals(shiftId))&&(type != null)&&(!"".equals(type));
    }
}
