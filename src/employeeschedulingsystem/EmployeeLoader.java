package employeeschedulingsystem;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

public class EmployeeLoader implements CsvEntityLoader {
    /**
     * employee business ID. Should be unique if user has correctly input data
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required=true)
    private String employeeId;

    /**
     * name of the employee
     * <p>
     * can be null
     */
    @CsvBindByName
    private String name;

    /**
     * mobile number of the employee
     * <p>
     * can be proper phone number, or null (empty field or field not in the CSV)
     */
    @PreAssignmentValidator(validator = MobileValidator.class)
    @CsvBindByName
    private String mobile;

    /**
     * email of the employee
     * <p>
     * cannot be null
     */
    @PreAssignmentValidator(validator = EmailValidator.class)
    @CsvBindByName(required=true)
    private String email;

    /**
     * higher importance places more weight on their requirements being satisfied
     * <p>
     * default will be 0
     */
    @CsvBindByName
    private int importance;

    /**
     * the maximum number of shifts an employee is allowed to take (mandatory to abide by)
     * <p>
     * null value indicates no upper limit
     */
    @CsvBindByName
    private Integer maxWeeklyShifts;

    /**
     * the minimum number of shifts the employee will be happy with (optional to abide by)
     * <p>
     * 0 by default
     */
    @CsvBindByName
    private int minWeeklyShifts;

    /**
     * no-arg constructor (for use by opencsv and optaplanner)
     */
    public EmployeeLoader(){
    }

    /**
     * constructor with non-defined importance, maxWeeklyShifts, minWeeklyShifts
     * @param employeeId
     * @param name
     * @param mobile
     * @param email
     */
    public EmployeeLoader(String employeeId, String name, String mobile, String email){
        validityHelper(employeeId, mobile, email);
        this.employeeId = employeeId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        importance = 0;
        maxWeeklyShifts = null;
        minWeeklyShifts = 0;
    }

    /**
     * full constructor for employee with all fields
     * @param employeeId
     * @param name
     * @param mobile
     * @param email
     * @param importance
     * @param maxWeeklyShifts
     * @param minWeeklyShifts
     */
    public EmployeeLoader(String employeeId, String name, String mobile, String email, int importance, Integer maxWeeklyShifts, int minWeeklyShifts){
        this(employeeId, name, mobile, email);
        this.importance = importance;
        this.maxWeeklyShifts = maxWeeklyShifts;
        this.minWeeklyShifts = minWeeklyShifts;
    }

    /**
     * get the employee
     * @return employee
     */
    public Employee getEmployee(){
        validityHelper(employeeId, mobile, email);
        // since in this class it is allowed for mobile to be empty string, we have to convert to null if so
        return new Employee(employeeId, name, identityButConvertEmptyStringToNull(mobile), email, importance, maxWeeklyShifts, minWeeklyShifts);
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+" employeeId="+employeeId+", name="+name+", mobile="+mobile+", email="+email+
            ", importance="+importance+", maxWeeklyShifts="+maxWeeklyShifts+", minWeeklyShifts="+minWeeklyShifts;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        EmployeeLoader other = (EmployeeLoader)obj;
        return Objects.equals(employeeId, other.employeeId)&&
            Objects.equals(name, other.name)&&
            Objects.equals(mobile, other.mobile)&&
            Objects.equals(email, other.email)&&
            Objects.equals(importance, other.importance)&&
            Objects.equals(maxWeeklyShifts, other.maxWeeklyShifts)&&
            Objects.equals(minWeeklyShifts, other.minWeeklyShifts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, name, mobile, email, importance, maxWeeklyShifts, minWeeklyShifts);
    }

    /**
     * helper method to check validity of this loader's internal data
     * <p>
     * used because forced to have empty constructor
     * @param employeeId
     * @param mobile
     * @param email
     */
    private static void validityHelper(String employeeId, String mobile, String email){
        assert (employeeId != null) && (!"".equals(employeeId))&&
            // note it is allowed for the mobile to be empty string in EmployeeLoader. But it has to be converted to null in employee getter
            ((mobile==null)||(new MobileValidator()).isValid(mobile))&&
            (new EmailValidator()).isValid(email);
    }
}
