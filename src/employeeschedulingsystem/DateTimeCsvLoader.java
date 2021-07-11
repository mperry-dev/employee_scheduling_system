package employeeschedulingsystem;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * opencsv Bean to help load a datetime from a CSV file
 * <p>
 * also has helper methods for converting between String and DateTime format, and vice versa
 */
public class DateTimeCsvLoader extends AbstractBeanField<DateTime, String> {
    // note formats here https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("d/MM/yyyy H:mm");

    @Override
    public DateTime convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        try{
            return convertStringToDateTime(s);
        }
        catch (IllegalArgumentException e){
            throw new CsvDataTypeMismatchException();
        }
    }

    /**
     * convert a String representing a DateTime to a DateTime
     * @param s String representing a DateTime
     * @return DateTime objects
     */
    private static DateTime convertStringToDateTime(String s) {
        return DateTime.parse(s, formatter);
    }

    /**
     * convert a DateTime into a String representing it
     * <p>
     * note if extra information such as second information in the datetime, that is simply discarded
     * <p>
     * throws IllegalArgumentException if get null (need to do so since Joda Time treats null as current time)
     * @param dt a DateTime
     * @return a String representing it
     */
    public static String convertDateTimeToString(DateTime dt){
        if (dt == null){
            throw new IllegalArgumentException();
        }
        return formatter.print(dt);
    }
}
