package com.roster123.employeescheduler.loaders;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

import org.joda.time.DateTime;

/**
 * class to load availability data from a CSV file.
 * 
 * This represents a single availability row, whilst an Availability object may represent multiple rows.
 * 
 * Objects of this class must not be changed, since needs to go in a set
 */
public class AvailabilityLoader extends TimePeriodLoader {
    /**
     * the business id of the employee.
     * 
     * This should reference the employeeId of an employee, if the user input a valid file.
     * 
     * Has to be at least 1 character
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required = true)
    private String employeeId;

    /**
     * the type loaded from the csv file, e.g. one of online, in-person.
     * 
     * If "", converted to null
     */
    @PreAssignmentProcessor(processor = EmptyStringBecomesNullProcessor.class)
    @CsvBindByName
    private String type;

    /**
     * empty constructor (for use by opencsv)
     */
    public AvailabilityLoader(){
    }

    /**
     * construct all fields for AvailabilityLoader
     * @param type
     * @param start
     * @param end
     * @param employeeId
     */
    public AvailabilityLoader(String employeeId, String type, DateTime start, DateTime end){
        super(start, end);
        validityHelper(employeeId, type);
        this.type = type;
        this.employeeId = employeeId;
    }

    /**
     * get employeeId
     * @return employeeId
     */
    public String getEmployeeId(){
        validityHelper(employeeId, type);
        return employeeId;
    }

    /**
     * get type
     * @return type
     */
    public String getType(){
        validityHelper(employeeId, type);
        return type;
    }

    @Override
    public String toString(){
        return super.toString()+", employeeId="+employeeId+" type="+type;
    }

    @Override
    public boolean equals(Object obj){
        if (!super.equals(obj)){
            return false;
        }
        AvailabilityLoader other = (AvailabilityLoader)obj;
        return Objects.equals(employeeId, other.employeeId)&&Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, type, super.hashCode());
    }

    /**
     * helper method to check validity of this loader's internal data.
     * 
     * Used because forced to have empty constructor
     * @param employeeId
     */
    private static void validityHelper(String employeeId, String type){
        assert (employeeId != null)&&(!"".equals(employeeId))&&(!"".equals(type));
    }
}
