package com.roster123.employeescheduler.solver;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import java.util.function.Predicate;

import java.util.LinkedList;
import java.util.List;

import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;

import org.apache.commons.collections4.ListUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.roster123.employeescheduler.domain.*;

/**
 * optaplanner constraints to ensure allocations are valid/optimized
 */
public class AllocationsConstraintProvider implements ConstraintProvider {
    private static final int MILLIS_IN_HOUR = 3600000;

    // TODO = make these constraints more efficient - e.g. smallerGapsBetweenShifts keeps reallocating and copying linked lists
    // that being said, if dealing with 30 employees/50 shifts or so, the time spent for each instance won't be overwhelming... 
    // this could be premature optimization - especially if optaplanner algorithm take longer each step than calculating constraints
    // could use something similar to StreamBuilder perhaps (StreamBuilder lacks subtract method)
    // could refactor smallerGapsBetweenShifts to use a map from element to the number of times it appears (similar to table).
    //     Then have the methods which perform the operation mutate t1 and return a reference to t1 (will need to inspect optaplanner code and test in case optaplanner reuses t1 expecting it to be unmodified)
    
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        // get constraints applied in order attached
        return new Constraint[]{
            // hard constraints
            employeeAvailabilityConflict(constraintFactory),
            employeeShiftsAtSameTimeConflict(constraintFactory),
            typeConflict(constraintFactory),
            aboveMaxWeeklyShiftsConflict(constraintFactory),
            // medium constraints
            employeeMinimumWeeklyShifts(constraintFactory),
            // soft constraints
            smallerGapsBetweenShifts(constraintFactory)
        };
    }

    /**
     * get constraint to ensure employees cannot do shifts outside their availability, taking into account type of shift
     * @param constraintFactory
     * @return constraint
     */
    public Constraint employeeAvailabilityConflict(ConstraintFactory constraintFactory){
        // select all shifts...
        return constraintFactory.from(Shift.class)
            // where the employee cannot do the shift
            .filter(Predicate.not(Shift::shiftEmployeeCanDoTime))
            .penalize("Employee availability conflict", HardMediumSoftScore.ONE_HARD);
    }

    /**
     * get constraint to ensure employees cannot do multiple shifts at the same time
     * @param constraintFactory
     * @return constraint
     */
    public Constraint employeeShiftsAtSameTimeConflict(ConstraintFactory constraintFactory){
        // select each pair of 2 different shifts...
        return constraintFactory.fromUniquePair(Shift.class,
            //  with the same employee...
            Joiners.equal(Shift::getEmployee),
            // with overlapping times... (use filtering over overlapping because already have intervals providing overlapping functionality)
            Joiners.filtering((s1, s2) -> s1.overlapsWithTimePeriod(s2)))
            .penalize("Employee shifts occurring at the same time", HardMediumSoftScore.ONE_HARD);
    }

    /**
     * get constraint to ensure employees can only do shifts satisfying their shift type requirements (null means anything is OK) for the relevant availabilities
     * @param constraintFactory
     * @return constraint
     */
    public Constraint typeConflict(ConstraintFactory constraintFactory){
        // select all shifts
        return constraintFactory.from(Shift.class)
            // where the employee cannot do the shift type during the times
            .filter(Predicate.not(Shift::shiftEmployeeCanDoType))
            // penalize a point for every instance of the rule being broken
            .penalize("Employee availability (type) conflict", HardMediumSoftScore.ONE_HARD);
    }

    /**
     * get constraint to ensure employees cannot go above their maximum number of shifts per week in any week
     * @param constraintFactory
     * @return constraint
     */
    public Constraint aboveMaxWeeklyShiftsConflict(ConstraintFactory constraintFactory){
        // steps:
        // 1. expand from having shift (with employee included) to also adding the shift year number/week number pair (need this so can group by the employee and week info pair along a sum)
        // 2. group by employee and week info - removing individual shifts from the picture. Collect sum of shifts for each week in this operation
        // https://youtu.be/XCyIdmUDMtI?t=1745
        // 3. filter so retain only the employees who are going above shift limits
        // 4. penalize where max shift sum is too large, by the number of extra shifts across all shifts going over

        // select the shift class (not the only relevant employee is the one which comes with this shift)
        return constraintFactory.from(Shift.class)
            // group by the same employee. Need to pair this with weekly for that employee using the collector, and pair with the Pair of week number and week year
            // sum function builds a list of pairs of week num, week year and the number of shifts for that week information
            // https://github.com/kiegroup/optaplanner/blob/master/optaplanner-core/src/main/java/org/optaplanner/core/api/score/stream/bi/BiConstraintStream.java
            // https://github.com/kiegroup/optaplanner/blob/master/optaplanner-core/src/main/java/org/optaplanner/core/api/score/stream/ConstraintCollectors.java
            // https://docs.optaplanner.org/7.28.0.Final/optaplanner-javadoc/org/optaplanner/core/api/score/stream/bi/BiConstraintStream.html
            // https://docs.optaplanner.org/7.33.0.Final/optaplanner-javadoc/org/optaplanner/core/api/score/stream/ConstraintCollectors.html
            // https://stackoverflow.com/questions/62515805/optaplanner-constraint-streams-groupby
            .groupBy(Shift::getEmployee,
                // getStartEndWeekInfoPack maps shift to the corresponding information about start and end week week numbers/week years
                ConstraintCollectors.sum((s) -> s.getStartEndWeekInfoPack(),
                    // "zero" is an empty table that will be built up
                    getEmptyTable(),
                    // adder adds the tables (increasing count where overlap)
                    (t1, t2) -> Shift.addCountTables(t1, t2),
                    // subtractor subtracts the tables, decreasing count where overlap, and going to negative possibly
                    (t1, t2) -> Shift.subtractCountTables(t1, t2)))
            
            // go from the employee and table mapping week number in year, year number for week to the number of shifts for that week, to the employee and maximum number of shifts
            
            // filter out employees who are OK
            .filter((e, t) -> !e.numberOfShiftsUnderMaximum(Collections.max(t.values())))
            // penalize by the number of shifts over summed across all shifts. Note avoiding null pointer exception here since filtered out those who are abiding by rules
            .penalize("Employee above maximum shifts", HardMediumSoftScore.ONE_HARD,
                (e, t) -> t.values().stream().filter(n -> !e.numberOfShiftsUnderMaximum(n)).map(n -> n-e.maxWeeklyShifts()).collect(Collectors.summingInt(Integer::intValue)));
    }
    
    /**
     * get constraint to prioritize employees minimum preferred number of weekly shifts being satisfied for every week.
     * 
     * Satisfying minimum shifts to satisfy employees is of medium importance - not critical, but more important than having shorter gaps between shifts
     * @param constraintFactory
     * @return constraint
     */
    public Constraint employeeMinimumWeeklyShifts(ConstraintFactory constraintFactory){
        // select the allocations class
        return constraintFactory.from(Shift.class)
            .groupBy(Shift::getEmployee,
                // getStartEndWeekInfoPack maps shift to the corresponding information about start and end week week numbers/week years
                ConstraintCollectors.sum(Shift::getStartEndWeekInfoPack,
                    // "zero" is an empty table that will be built up
                    getEmptyTable(),
                    // adder adds the tables (increasing count where overlap)
                    (t1, t2) -> Shift.addCountTables(t1, t2),
                    // subtractor subtracts the tables, decreasing count where overlap, and going to negative possibly
                    (t1, t2) -> Shift.subtractCountTables(t1, t2)))
            .filter((e, t) -> !e.numberOfShiftsAboveMinimum(Collections.min(t.values())))
            .penalize("Employee below minimum shifts", HardMediumSoftScore.ONE_MEDIUM,
            // lose a point every time employee allocation breaks rule*importance
            (e, t) -> t.values().stream()
                .filter(n -> !e.numberOfShiftsAboveMinimum(n))
                .map(n -> e.minWeeklyShifts()-n)
                .collect(Collectors.summingInt(Integer::intValue))*e.getImportance());
    }

    /**
     * get constraint to prioritize having smaller gaps between shifts on the same day for all employees.
     * 
     * Prioritize more important employees.
     * 
     * This should also work fine if a shift is across 3 days (noting overlapping shifts not causing penalty isn't an issue as will fail hard constraint)
     * @param constraintFactory
     * @return constraint
     */
    public Constraint smallerGapsBetweenShifts(ConstraintFactory constraintFactory){
        // employees prefer to do sequential shifts when on the same day, to avoid waiting around
        return constraintFactory.from(Shift.class)
            // group by the employees (can't group yet by day because start and end could be different)
            // pair the employees with their list of datetimes
            // use pair of datetime rather than interval since intervals are equal based on duration for jodatime
            .groupBy(Shift::getEmployee,
                ConstraintCollectors.sum(
                    Shift::getListWithStartEndPair,
                    new LinkedList<Pair<DateTime, DateTime>>(),
                    // take the lists and add them
                    (l1, l2) -> new LinkedList<>(ListUtils.union(l1, l2)),
                    // note ListUtils.subtract respects cardinality:
                    // https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/ListUtils.html#subtract-java.util.List-java.util.List-
                    (l1, l2) -> new LinkedList<>(ListUtils.subtract(l1, l2))))
            .penalize("Employees prefer to avoid gaps", HardMediumSoftScore.ONE_SOFT, (e, l)->getHoursSumGapsSharedDates(l)*e.getImportance());
    }
    

    /**
     * helper method to get empty table so typing correct
     * @return empty table
     */
    private static Table<Integer, Integer, Integer> getEmptyTable(){
        Table<Integer, Integer, Integer> table = HashBasedTable.create();
        return table;
    }

    /**
     * go from pairs of start/end times employee has worked in, to the sum of the hours of gaps on the same date.
     * 
     * This should also work fine if a shift is across 3 days (noting overlapping shifts not causing penalty isn't an issue as will fail hard constraint)
     * @param times
     * @return
     */
    private static int getHoursSumGapsSharedDates(List<Pair<DateTime, DateTime>> times){
        ArrayList<Pair<DateTime, DateTime>> timesCopy = new ArrayList<>(times);
        // we just sort... don't worry apart from avoiding errors for overlapping shifts, because if hard constraints are satisfied there wont be overlapping shifts here
        Collections.sort(timesCopy);
        int millisWaited = 0;
        for (int i=0; i<timesCopy.size()-1; i++){
            DateTime gapStart = timesCopy.get(i).getValue1();
            DateTime gapEnd = timesCopy.get(i+1).getValue0();
            // correctness relies on neither being null... joda time considers null to be current time
            // if they are on the same day and start is before end, add the gap
            // compare using LocalDate because java.util.Date equals isn't working (from .toDate)
            if (Objects.equals(gapStart.toLocalDate(), gapEnd.toLocalDate())&&(gapStart.compareTo(gapEnd)<0)){
                millisWaited += (new Interval(gapStart, gapEnd)).toDurationMillis();
            }
        }
        // hours rounded down
        return millisWaited/MILLIS_IN_HOUR;
    }
}
