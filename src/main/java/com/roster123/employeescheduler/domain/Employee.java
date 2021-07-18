package com.roster123.employeescheduler.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.ReadableInstant;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import com.roster123.employeescheduler.loaders.AvailabilityLoader;
import com.roster123.employeescheduler.validators.*;


/**
 * class representing an Employee in the system.
 * 
 * Distinguished from employee loader by not having empty constructor - so impossible to violate requirements
 */
public class Employee {
    /**
     * employee business ID. Should be unique if user has correctly input data.
     * 
     * Cannot be null, cannot be ""
     */
    @PlanningId
    private final String employeeId;

    /**
     * name of the employee
     */
    private final String name;

    /**
     * mobile number of the employee.
     * 
     * Can be proper phone number, or null (field wasn't set for this employee of the field not in the CSV).
     * 
     * Cannot be empty string
     */
    private final String mobile;

    /**
     * email of the employee.
     * 
     * Cannot be null or empty
     */
    private final String email;

    /**
     * higher importance places more weight on their requirements being satisfied.
     * 
     * Default will be 0
     */
    private final int importance;

    // we include the following fields as Integer rather than as int, so can detect if not set in csv as will be null
    /**
     * the maximum number of shifts an employee is allowed to take (mandatory to abide by)
     */
    private final Integer maxWeeklyShifts;

    /**
     * the minimum number of shifts the employee will be happy with (optional to abide by).
     * 
     * Default will be 0
     */
    private final int minWeeklyShifts;

    /**
     * this availabilities list should have no overlaps (note they can meet end-to-end) - CLASS INVARIANT.
     * 
     * Additionally, guarantees that abutting availabilities with the same types have been merged (CLASS INVARIANT)
     */
    private List<Availability> availabilities;

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
    public Employee(String employeeId, String name, String mobile, String email, int importance, Integer maxWeeklyShifts, int minWeeklyShifts){
        assert (employeeId != null)&&(!"".equals(employeeId))&&
            ((mobile==null)||MobileValidator.validMobileString(mobile))&&
            (new EmailValidator()).isValid(email);

        availabilities = new ArrayList<>();
        this.employeeId = employeeId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.importance = importance;
        this.maxWeeklyShifts = maxWeeklyShifts;
        this.minWeeklyShifts = minWeeklyShifts;
    }

    /**
     * get sorted, broken-up list of availability and availability loader times for availability adding algorithm
     * @param loadedAvailabilities new availability loaders we intend to add availabilities based on
     * @return list of all start/end datetimes in availability/availability loader lists, with availability type, and whether is at start
     */
    private List<Triplet<DateTime, String, Boolean>> getSortedAvailabilityAndAvailabilityLoaderTimesBrokenUp(List<AvailabilityLoader> loadedAvailabilities){
        // TODO = unit test this method
        // this array will contain datetime, type of availability, and whether start/end (note false is sorted before true in Java by default)
        List<Triplet<DateTime, String, Boolean>> intervalsBrokenUp = new ArrayList<>();
        for (AvailabilityLoader loadedAvailability: loadedAvailabilities){
            intervalsBrokenUp.add(new Triplet<>(loadedAvailability.getStart(), loadedAvailability.getType(), false));
            intervalsBrokenUp.add(new Triplet<>(loadedAvailability.getEnd(), loadedAvailability.getType(), true));
        }

        // now take into account there are existing availabilities, we should take them into account and not remove them
        for (Availability availability: availabilities){
            for (String type: availability.getTypes()){
                intervalsBrokenUp.add(new Triplet<>(availability.getStart(), type, false));
                intervalsBrokenUp.add(new Triplet<>(availability.getEnd(), type, true));
            }
        }

        // perform sort that shifts nulls to the beginning
        Collections.sort(intervalsBrokenUp, new TripletWithNullsComparator<DateTime, String, Boolean, ReadableInstant, String, Boolean>());

        return intervalsBrokenUp;
    }

    /**
     * get all intervals spanning across the range of all times from earliest start to latest end in intervalBrokenUp, with the possibly empty set of types associated with it
     * @param intervalsBrokenUp list of all start/end datetimes in availability/availability loader lists, with availability type, and whether is at start
     * @return list of all intervals with possibly empty types
     */
    private List<Triplet<DateTime, DateTime, HashSet<String>>> getAllIntervalsWithTypes(List<Triplet<DateTime, String, Boolean>> intervalsBrokenUp){
        // TODO = unit test this method
        // use a list of current since can have duplicates of current types (unlike the problem on Github)
        List<String> currentTypesList = new LinkedList<>();
        // contains start time, end time, set of types. Used linkedlist as just adding to the end.
        // using hashset since can contain null
        // use pair of datetime rather than interval since intervals are ordered based on duration for jodatime
        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilities = new LinkedList<>();
        for (int i=0; i<intervalsBrokenUp.size()-1; i++) {
            // looping over pairs of adjacent items in intervalsBrokenUp
            Triplet<DateTime, String, Boolean> firstTime = intervalsBrokenUp.get(i);
            Triplet<DateTime, String, Boolean> secondTime = intervalsBrokenUp.get(i+1);

            DateTime firstTimestamp = firstTime.getValue0();
            String firstType = firstTime.getValue1();
            Boolean firstIsStart = firstTime.getValue2();
            DateTime secondTimestamp = secondTime.getValue0();
            if (!firstIsStart){
                currentTypesList.add(firstType);
            }
            else{
                currentTypesList.remove(firstType);
            }

            // note that checking here that firstTimestamp <= secondTimestamp would be redundant - it is guaranteed here, since the datetimes will have been sorted first
            // we use != because we don't want to bother with availabilities of 0 width
            if (firstTimestamp.compareTo(secondTimestamp) != 0){
                // use a hashset since can contain null, which we require
                processedAvailabilities.add(new Triplet<>(firstTimestamp, secondTimestamp, new HashSet<String>(currentTypesList)));
            }
        }

        return processedAvailabilities;
    }

    /**
     * get processed availabilities where if abutting availabilities have same types, merge them
     * @param processedAvailabilitiesNoEmptyTypes list of all intervals with no empty types
     * @return intervals which are merged if abutting and same types
     */
    private List<Triplet<DateTime, DateTime, HashSet<String>>> minimizeProcessedAvailabilities(List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilitiesNoEmptyTypes){
        // TODO = unit test this method
        // set to size of processed availabilities list to avoid wastage due to copying when large
        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilitiesMinimal = new ArrayList<>(processedAvailabilitiesNoEmptyTypes.size());
        for (Triplet<DateTime, DateTime, HashSet<String>> availabilityInfo: processedAvailabilitiesNoEmptyTypes){
            // potentially modify existing availability if already put one or more into this list
            if (processedAvailabilitiesMinimal.size()>0){
                int lastIndex = processedAvailabilitiesMinimal.size()-1;
                DateTime currentStart = availabilityInfo.getValue0();
                DateTime currentEnd = availabilityInfo.getValue1();
                HashSet<String> currentTypes = availabilityInfo.getValue2();
                DateTime previousStart = processedAvailabilitiesMinimal.get(lastIndex).getValue0();
                DateTime previousEnd = processedAvailabilitiesMinimal.get(lastIndex).getValue1();
                HashSet<String> previousTypes = processedAvailabilitiesMinimal.get(lastIndex).getValue2();
                // if the current start abuts with the previous end (i.e. they're equal) and the types match, merge (note due to above algorithm wont have overlap)
                if (currentStart.equals(previousEnd)&&currentTypes.equals(previousTypes)){
                    // set the last last value to have the previous start and current end, and current types
                    processedAvailabilitiesMinimal.set(lastIndex, new Triplet<>(previousStart, currentEnd, currentTypes));
                }
                else{
                    // no abutting, thus add to the end (keeping in mind no overlapping)
                    processedAvailabilitiesMinimal.add(availabilityInfo);
                }
            }
            else{
                // no availabilities have been added yet, so just add on
                processedAvailabilitiesMinimal.add(availabilityInfo);
            }
        }

        return processedAvailabilitiesMinimal;
    }

    /**
     * add availabilities using a list of availability loaders.
     * 
     * The availabilities stored in this class should have no overlaps in the availability objects (merging types together under a single list).
     * 
     * NOTE - it is possible here to get adjacent availabilities of the same type. Could perhaps write a method to merge them, but not necessary
     * @return list of availabilities
     */
    public void addAvailabilities(List<AvailabilityLoader> loadedAvailabilities){
        // algorithm matches the below link (except mine is inclusive for start/end whilst the example is exclusive)
        // https://softwareengineering.stackexchange.com/questions/363091/split-overlapping-ranges-into-all-unique-ranges?newreg=93383e379afe4dd3a595480528ee1541
        assert loadedAvailabilities != null;

        List<Triplet<DateTime, String, Boolean>> intervalsBrokenUp = getSortedAvailabilityAndAvailabilityLoaderTimesBrokenUp(loadedAvailabilities);
        
        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilities = getAllIntervalsWithTypes(intervalsBrokenUp);

        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilitiesNoEmptyTypes = processedAvailabilities.stream().filter(t->t.getValue2().size()>0).collect(Collectors.toList());

        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilitiesNoEmptyTypesMinimal = minimizeProcessedAvailabilities(processedAvailabilitiesNoEmptyTypes);

        // now have the processed availabilities, produce list of Availability (now guaranteed do not overlap, no empty type sets, and abutting availabilities with same types are merged)
        // set initial size, so a little faster (perhaps premature optimization unless many employees)
        availabilities = new ArrayList<Availability>(processedAvailabilitiesNoEmptyTypesMinimal.size()); // overwrite existing availabilities and replace it
        for (Triplet<DateTime, DateTime, HashSet<String>> availabilityInfo: processedAvailabilitiesNoEmptyTypesMinimal){
            availabilities.add(new Availability(availabilityInfo.getValue2(), availabilityInfo.getValue0(), availabilityInfo.getValue1()));
        }
    }

    /**
     * add a single availability from an availability loader. Just a wrapper of addAvailabilities
     * @param availability
     */
    public void addAvailability(AvailabilityLoader availability){
        assert availability != null;
        addAvailabilities(new ArrayList<>(Arrays.asList(availability)));
    }

    /**
     * get the availabilities stored which overlap in time with the shift
     * @param shift shift checking for overlaps with
     * @return list of availabilities which overlap in time with the shift
     */
    private List<Availability> getAvailabilitiesOverlappingShift(Shift shift){
        // TODO = unit test this method
        List<Availability> availabilitiesOverlappingShift = new LinkedList<>();
        for (Availability availability: availabilities){
            if (availability.overlapsWithTimePeriod(shift)){
                availabilitiesOverlappingShift.add(availability);
            }
        }
        return availabilitiesOverlappingShift;
    }

    /**
     * get list of start/end times, sorted by the start time then the end time.
     * 
     * Using this instead of joda time intervals because joda time intervals are sorted based on the length of duration
     * @param availabilitiesOverlappingShift
     * @return
     */
    private List<Pair<DateTime, DateTime>> getSortedIntervalInfo(List<Availability> availabilitiesOverlappingShift){
        // TODO = unit test this method
        List<Pair<DateTime, DateTime>> intervalInfo = new ArrayList<>();
        // add intervals for availabilities overlapping shift, because only they are relevant in determining whether shift can be satisfied
        for (Availability availability: availabilitiesOverlappingShift){
            intervalInfo.add(new Pair<>(availability.getStart(), availability.getEnd()));
        }
        Collections.sort(intervalInfo); // sort by start then end

        return intervalInfo;
    }

    /**
     * get list of availabilities covering shift by time if cover it. Note we DO NOT consider whether the employee has a clash or whether the types work.
     * 
     * Implementation relies on the assumption that there's no overlaps between availabilities (CLASS INVARIANT)
     * @param shift a shift we're considering taking
     * @return null if availabilities do not cover it, or the list of availabilities if they do
     */
    private List<Availability> availabilitiesFullyCoveringShiftTime(Shift shift){
        // TODO = test this has no overlaps in the returned availabilities (property based test)
        List<Availability> availabilitiesOverlappingShift = getAvailabilitiesOverlappingShift(shift);

        // no availabilities to offer
        if (availabilitiesOverlappingShift.size() == 0){
            return null;
        }

        // applying the efficient algorithm here for merging overlapping intervals
        // https://www.geeksforgeeks.org/merging-intervals/
        // use pair of datetime rather than interval since intervals are ordered based on duration for jodatime
        List<Pair<DateTime, DateTime>> intervalInfo = getSortedIntervalInfo(availabilitiesOverlappingShift);
        for (int i=0; i<intervalInfo.size()-1; i++){
            Interval interval1 = new Interval(intervalInfo.get(0).getValue0(), intervalInfo.get(0).getValue1());
            Interval interval2 = new Interval(intervalInfo.get(1).getValue0(), intervalInfo.get(1).getValue1());
            // we know here interval1.overlaps(interval2) will give false, and rely upon it
            assert !interval1.overlaps(interval2);
            if (!interval1.abuts(interval2)){
                // if they don't touch, then there's a gap in the availabilities offered, so immediately return null
                return null;
            }
        }
        // if first start time in overlapping intervals is after the start of the shift, won't work
        if (intervalInfo.get(0).getValue0().compareTo(shift.getStart()) > 0){
            return null;
        }
        // if end of the overlapping intervals is less than the end of the shift, won't work
        if (intervalInfo.get(availabilitiesOverlappingShift.size()-1).getValue1().compareTo(shift.getEnd()) < 0){
            return null;
        }
        return availabilitiesOverlappingShift;
    }

    /**
     * confirm whether shift can be performed by time (not considering the type of shift or whether there is a conflict)
     * @param shift
     * @return whether the shift can be performed by time
     */
    public boolean canPerformShiftByTime(Shift shift){
        assert shift != null;
        return (availabilitiesFullyCoveringShiftTime(shift) != null);
    }

    /**
     * confirm whether shift can be performed by the type of the shift and the time
     * @param shift
     * @return whether the shift can be performed by type of the shift
     */
    public boolean canPerformShiftByType(Shift shift){
        assert shift != null;
        // this relies on the availabilities list having no overlaps between themselves - since assume correct, then just check all availabilities types
        List<Availability> availabilitiesOverlappingShift = availabilitiesFullyCoveringShiftTime(shift);
        if (availabilitiesOverlappingShift == null){
            return false; // cannot even cover the times
        }
        for (Availability availability: availabilitiesOverlappingShift){
            // if the availability includes null, then anything is fine (shouldn't have empty string)
            if ((!availability.getTypes().contains(null))&&(!availability.getTypes().contains(shift.getType()))){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+" employeeId="+employeeId+", name="+name+", mobile="+mobile+", email="+email+
            ", importance="+importance+", maxWeeklyShifts="+maxWeeklyShifts+", minWeeklyShifts="+minWeeklyShifts+
            ", availabilities="+availabilities;
    }

    @Override
    public boolean equals(Object obj){
        // note this equals method is used for both the main system and the constraint checking (in Joiners.equal)
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Employee other = (Employee)obj;
        return Objects.equals(employeeId, other.employeeId)&&
            Objects.equals(name, other.name)&&
            Objects.equals(mobile, other.mobile)&&
            Objects.equals(email, other.email)&&
            Objects.equals(importance, other.importance)&&
            Objects.equals(maxWeeklyShifts, other.maxWeeklyShifts)&&
            Objects.equals(minWeeklyShifts, other.minWeeklyShifts)&&
            Objects.equals(availabilities, other.availabilities);
    }

    /**
     * check whether matches another employee by the employee id
     * @param employee an employee to compare to
     * @return whether employee id's match
     */
    public boolean matchesEmployeeEmployeeId(Employee employee){
        return Objects.equals(employeeId, employee.employeeId);
    }

    /**
     * check whether matches an availability loader by the employee id
     * @param loader an availability loader which have an availability for this employee
     * @return whether employee id's match
     */
    public boolean availabilityLoaderForThisEmployee(AvailabilityLoader loader){
        return Objects.equals(employeeId, loader.getEmployeeId());
    }

    /**
     * get the employee id
     * @return employee id
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * confirm whether the suggested weekly number of shifts fits under the maximum allowable number of shifts for this employee.
     * 
     * This is separate from checking minimum because this will be a hard heuristic
     */
    public boolean numberOfShiftsUnderMaximum(int weeklyNumberOfShifts){
        return ((maxWeeklyShifts == null) || (weeklyNumberOfShifts <= maxWeeklyShifts));
    }

    /**
     * confirm whether the suggested weekly number of shifts above the minimum preferred number of shifts for this employee.
     * 
     * This is separate from checking maximum as it will be a soft heuristic
     */
    public boolean numberOfShiftsAboveMinimum(int weeklyNumberOfShifts){
        return weeklyNumberOfShifts >= minWeeklyShifts;
    }

    /**
     * get the importance of the employee - higher importance value means we prioritize the employee for shifts
     * @return importance of the employee
     */
    public int getImportance(){
        return importance;
    }

    /**
     * get the name of the employee
     * @return name of the employee
     */
    public String getName(){
        return name;
    }

    /**
     * get the mobile of the employee
     * @return mobile of the employee
     */
    public String getMobile(){
        return mobile;
    }

    /**
     * get the maximum number of weekly shifts allowed for this employee.
     * 
     * Null if no maximum for the employee
     * @return maximum weekly shifts
     */
    public Integer maxWeeklyShifts(){
        return maxWeeklyShifts;
    }

    /**
     * get the minimum number of weekly shifts preferred by this employee
     * @return minimum weekly shifts
     */
    public int minWeeklyShifts() {
        return minWeeklyShifts;
    }


    @Override
    public int hashCode() {
        return Objects.hash(employeeId, name, mobile, email, importance, maxWeeklyShifts, minWeeklyShifts, availabilities);
    }
}
