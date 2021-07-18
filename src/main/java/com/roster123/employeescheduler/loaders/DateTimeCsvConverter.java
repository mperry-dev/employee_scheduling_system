package com.roster123.employeescheduler.loaders;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * opencsv Bean to help load a datetime from a CSV file.
 * 
 * Also has helper methods for converting between String and DateTime format, and vice versa
 */
public class DateTimeCsvConverter extends AbstractBeanField<DateTime, String> {
    // note formats here https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("d/MM/yyyy H:mm");

    @Override
    public DateTime convert(String subject) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        try{
            return convertStringToDateTime(subject);
        }
        catch (IllegalArgumentException e){
            throw new CsvDataTypeMismatchException();
        }
    }

    /**
     * convert a String representing a DateTime to a DateTime
     * @param subject String representing a DateTime
     * @return corresponding DateTime object
     */
    private static DateTime convertStringToDateTime(String subject) {
        return DateTime.parse(subject, formatter);
    }

    /**
     * convert a DateTime into a String representing it.
     * 
     * Note if extra information such as second information in the datetime, that is simply discarded.
     * 
     * Throws IllegalArgumentException if get null (need to do so since Joda Time treats null as current time)
     * @param subject a DateTime
     * @return a String representing it
     */
    public static String convertDateTimeToString(DateTime subject){
        if (subject == null){
            throw new IllegalArgumentException();
        }
        return formatter.print(subject);
    }
}
