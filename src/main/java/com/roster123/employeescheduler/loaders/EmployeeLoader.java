package com.roster123.employeescheduler.loaders;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

import com.roster123.employeescheduler.domain.Employee;
import com.roster123.employeescheduler.validators.*;

/**
 * class for loading in employee.
 * 
 * Distinguished from employee as has empty constructor, so can be made to be invalid. Also, following style of system, as both shift and availability require loaders
 */
public class EmployeeLoader {
    /**
     * employee business ID. Should be unique if user has correctly input data
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required=true)
    private String employeeId;

    /**
     * name of the employee.
     * 
     * Can be null
     */
    @CsvBindByName
    private String name;

    /**
     * mobile number of the employee.
     * 
     * Can be proper phone number, or null only (no empty string or invalid mobile)
     */
    @PreAssignmentProcessor(processor = EmptyStringBecomesNullProcessor.class)
    @PreAssignmentValidator(validator = MobileValidator.class)
    @CsvBindByName
    private String mobile;

    /**
     * email of the employee.
     * 
     * Cannot be null
     */
    @PreAssignmentValidator(validator = EmailValidator.class)
    @CsvBindByName(required=true)
    private String email;

    /**
     * higher importance places more weight on their requirements being satisfied.
     * 
     * Default will be 0
     */
    @CsvBindByName
    private int importance;

    /**
     * the maximum number of shifts an employee is allowed to take (mandatory to abide by).
     * 
     * Null value indicates no upper limit
     */
    @CsvBindByName
    private Integer maxWeeklyShifts;

    /**
     * the minimum number of shifts the employee will be happy with (optional to abide by).
     * 
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
        return new Employee(employeeId, name, mobile, email, importance, maxWeeklyShifts, minWeeklyShifts);
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
     * helper method to check validity of this loader's internal data.
     * 
     * Used because forced to have empty constructor
     * @param employeeId
     * @param mobile
     * @param email
     */
    private static void validityHelper(String employeeId, String mobile, String email){
        assert (employeeId != null) && (!"".equals(employeeId))&&
            ((mobile==null)||MobileValidator.validMobileString(mobile))&&
            (new EmailValidator()).isValid(email);
    }
}
