package com.roster123.employeescheduler.loaders;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

import org.joda.time.DateTime;

import com.roster123.employeescheduler.domain.Shift;

/**
 * class to load shift.
 * 
 * Distinguished from shift by being lenient towards start being after the end in TimePeriodLoader
 */
public class ShiftLoader extends TimePeriodLoader {
    /** 
     * unique business id of the shift.
     * 
     * Cannot be null or ""
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required=true)
    private String shiftId;

    /**
     * the shift type, such as online shift, in-person shift.
     * 
     * Cannot be "" or null
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required=true)
    private String type;

    /**
     * no-arg constructor (for use by opencsv)
     */
    public ShiftLoader(){
        super();
    }

    /**
     * full constructor setting all fields
     * @param type
     * @param start
     * @param end
     * @param shiftId
     */
    public ShiftLoader(String shiftId, String type, DateTime start, DateTime end) {
        super(start, end);
        validityHelper(shiftId, type);
        this.type = type;
        this.shiftId = shiftId;
    }

    @Override
    public boolean equals(Object obj){
        if (!super.equals(obj)){
            return false;
        }
        ShiftLoader other = (ShiftLoader)obj;
        return Objects.equals(shiftId, other.shiftId)&&Objects.equals(type, other.type);
    }

    @Override
    public String toString(){
        return super.toString()+", shiftId="+shiftId+", type="+type;
    }

    @Override
    public int hashCode() {
        // don't compare using employee - as with equals, we consider 2 shift loaders which are equal apart from the employee to be equal
        return Objects.hash(shiftId, type, super.hashCode());
    }

    public Shift getShift(){
        validityHelper(shiftId, type);
        // note this may raise an assert if time period start < end
        return new Shift(shiftId, type, getStart(), getEnd());
    }

    /**
     * helper method to check validity of this loader's internal data.
     * 
     * Used because forced to have empty constructor
     * @param shiftId
     * @param type
     */
    private static void validityHelper(String shiftId, String type){
        assert (shiftId != null)&&(!"".equals(shiftId))&&(type != null)&&(!"".equals(type));
    }
}
