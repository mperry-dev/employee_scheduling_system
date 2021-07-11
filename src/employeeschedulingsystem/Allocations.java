package employeeschedulingsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

/**
 * optaplanner solution class representing all allocations
 */
@PlanningSolution
public class Allocations {
    /**
     * the shifts which have to be allocated
     */
    @PlanningEntityCollectionProperty
    private List<Shift> shifts;

    /**
     * the employees for whom shifts will be assigned
     */
    @ValueRangeProvider(id = "employeeRange")
    @ProblemFactCollectionProperty
    private List<Employee> employees;

    /**
     * the score from the current allocation
     */
    @PlanningScore
    private HardMediumSoftScore score;

    /**
     * empty constructor (for use by optaplanner)
     */
    public Allocations(){
    }

    /**
     * create allocations, without shifts having employees allocated to them
     * @param shifts shifts without employees
     * @param employees all employees for the system
     */
    public Allocations(List<Shift> shifts, List<Employee> employees){
        validityHelper(shifts, employees);
        this.shifts = new ArrayList<>(shifts);
        this.employees = new ArrayList<>(employees);
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+" shifts="+shifts+", employees="+employees+", score="+score;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Allocations other = (Allocations)obj;
        return (Objects.equals(shifts, other.shifts)&&Objects.equals(employees, other.employees)&&Objects.equals(score, other.score));
    }

    /**
     * get the immutable score object
     * @return score
     */
    public HardMediumSoftScore getScore(){
        validityHelper(shifts, employees);
        return score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shifts, employees, score);
    }

    /**
     * get the shifts
     * <p>
     * This is necessary because the shifts list here has different identity to that in the EmployeeSchedulingSystem
     * @return the shifts
     */
    public List<Shift> getShifts(){
        validityHelper(shifts, employees);
        return Collections.unmodifiableList(shifts);
    }

    /**
     * helper method to check validity of internal data
     * <p>
     * used because forced to have empty constructor
     * @param shifts
     * @param employees
     */
    private void validityHelper(List<Shift> shifts, List<Employee> employees){
        assert (shifts != null) && (employees != null);
    }
}
