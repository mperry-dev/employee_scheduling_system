package employeeschedulingsystem;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

import org.joda.time.DateTime;

/**
 * class to load availability data from a CSV file
 */
public class AvailabilityLoader extends TimePeriodLoader implements CsvEntityLoader{
    /**
     * the business id of the employee
     * <p>
     * this should reference the employeeId of an employee, if the user input a valid file
     * <p>
     * has to be at least 1 character
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required = true)
    private String employeeId;

    /**
     * the type loaded from the csv file, e.g. one of online, in-person
     * <p>
     * if "", must be converted to null later. null or "" represents no preference. We do this in identityButConvertEmptyStringToNull
     */
    @CsvBindByName
    private String type;

    /**
     * empty constructor (for use by opencsv)
     */
    public AvailabilityLoader(){
    }

    /**
     * construct all fields for AvailabilityLoader
     * <p>
     * note we allow type to be "" here but we change it when returning it
     * @param type
     * @param start
     * @param end
     * @param employeeId
     */
    public AvailabilityLoader(String employeeId, String type, DateTime start, DateTime end){
        super(start, end);
        validityHelper(employeeId);
        this.type = type;
        this.employeeId = employeeId;
    }

    /**
     * get employeeId
     * @return employeeId
     */
    public String getEmployeeId(){
        validityHelper(employeeId);
        return employeeId;
    }

    /**
     * get type
     * <p>
     * converted to null if empty string
     * @return type
     */
    public String getType(){
        validityHelper(employeeId);
        return identityButConvertEmptyStringToNull(type);
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
     * helper method to check validity of this loader's internal data
     * <p>
     * used because forced to have empty constructor
     * @param employeeId
     */
    private static void validityHelper(String employeeId){
        assert (employeeId != null)&&(!"".equals(employeeId));
    }
}
