package employeeschedulingsystem;

import java.util.Objects;

import com.opencsv.bean.CsvCustomBindByName;

import org.joda.time.DateTime;

/**
 * parent class for classes loading in a time from opencsv
 * <p>
 * this differs from TimePeriod because we don't raise an assert if invalid data loaded in, such as the start being after the end
 */
public abstract class TimePeriodLoader {
    /**
     * start time of the availability
     */
    @CsvCustomBindByName(converter = DateTimeCsvLoader.class, required = true)
    private DateTime start;

    /**
     * end time of the availability
     */
    @CsvCustomBindByName(converter = DateTimeCsvLoader.class, required = true)
    private DateTime end;

    /**
     * empty constructor - for use by opencsv
     */
    public TimePeriodLoader(){
    }

    public TimePeriodLoader(DateTime start, DateTime end){
        validityHelper(start, end);
        this.start = start;
        this.end = end;
    }


    /**
     * get the start time of the time period loader
     * <p>
     * must not return null! Otherwise we get current time
     * <p>
     * to extract the data has to be a valid time period
     * @return start time
     */
    public DateTime getStart(){
        assert checkValidTimePeriod();
        return start;
    }

    /**
     * get the end time of the time period loader
     * <p>
     * must not return null! Otherwise we get current time
     * <p>
     * to extract the data has to be a valid time period
     * @return end time
     */
    public DateTime getEnd(){
        assert checkValidTimePeriod();
        return end;
    }

    /**
     * confirm whether the time period is valid
     * @return whether valid
     */
    public boolean checkValidTimePeriod(){
        validityHelper(start, end);
        // no need to check for null as assert not null above
        return start.isBefore(end);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        TimePeriodLoader other = (TimePeriodLoader)obj;
        return Objects.equals(start, other.start)&&Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" start="+start+", end="+end;
    }

    /**
     * helper method to check validity of this loader's internal data
     * <p>
     * used because forced to have empty constructor
     * @param start
     * @param end
     */
    private static void validityHelper(DateTime start, DateTime end){
        assert (start != null)&&(end != null);
    }
}
