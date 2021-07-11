package employeeschedulingsystem;

import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * represents a time period, such as a shift or availability
 * <p>
 * has convenience method for checking overlap with other time periods
 */
public abstract class TimePeriod {
    /**
     * start time
     * <p>
     * cannot be null
     */
    private DateTime start;

    /**
     * end time
     * <p>
     * cannot be null
     */
    private DateTime end;

    /**
     * no-arg constructor for optaplanner
     * <p>
     * unfortunately this means it's possible for start and end to be null due to programmer error, so need to add asserts that start and end are not null
     */
    public TimePeriod(){
    }

    public TimePeriod(DateTime start, DateTime end){
        validityHelper(start, end);
        this.start = start;
        this.end = end;
    }

    /**
     * get the start time of the time period
     * <p>
     * must not return null! Otherwise we get current time
     * @return start time
     */
    public DateTime getStart(){
        validityHelper(start, end);
        return start;
    }

    /**
     * get the end time of the time period
     * <p>
     * must not return null! Otherwise we get current time
     * @return end time
     */
    public DateTime getEnd(){
        validityHelper(start, end);
        return end;
    }

    /**
     * get the interval of the time period
     * @return interval
     */
    public Interval getInterval(){
        validityHelper(start, end);
        return new Interval(start, end);
    }

    /**
     * check if this time period overlaps with another time period
     * @param other another time period
     * @return whether overlaps
     */
    public boolean overlapsWithTimePeriod(TimePeriod other){
        validityHelper(start, end);
        // https://www.joda.org/joda-time/apidocs/org/joda/time/Interval.html#overlap-org.joda.time.ReadableInterval-
        return getInterval().overlaps(other.getInterval());
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        TimePeriod other = (TimePeriod)obj;
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
     * helper method to check validity of start and end
     * <p>
     * used because forced to have empty constructor
     * @param start start time
     * @param end end time
     */
    private static void validityHelper(DateTime start, DateTime end){
        assert (start != null) && (end != null) && start.isBefore(end);
    }    
}
