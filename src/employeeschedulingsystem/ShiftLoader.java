package employeeschedulingsystem;

import java.util.Objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;

import org.joda.time.DateTime;

public class ShiftLoader extends TimePeriodLoader implements CsvEntityLoader {
    /** 
     * unique business id of the shift
     * <p>
     * cannot be null or ""
     */
    @PreAssignmentValidator(validator=MustMatchRegexExpression.class, paramString=".+")
    @CsvBindByName(required=true)
    private String shiftId;

    /**
     * the shift type, such as online shift, in-person shift
     * <p>
     * cannot be "" or null
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
        return new Shift(shiftId, type, getStart(), getEnd());
    }

    /**
     * helper method to check validity of this loader's internal data
     * <p>
     * used because forced to have empty constructor
     * @param shiftId
     * @param type
     */
    private static void validityHelper(String shiftId, String type){
        assert (shiftId != null)&&(!"".equals(shiftId))&&(type != null)&&(!"".equals(type));
    }
}
