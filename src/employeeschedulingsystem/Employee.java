package employeeschedulingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.ReadableInstant;
import org.optaplanner.core.api.domain.lookup.PlanningId;

/**
 * class representing an Employee in the system
 * <p>
 * also used to load Employee data directly from the CSV file
 */
public class Employee {
    /**
     * employee business ID. Should be unique if user has correctly input data
     * <p>
     * cannot be null, cannot be ""
     */
    @PlanningId
    private final String employeeId;

    /**
     * name of the employee
     */
    private final String name;

    /**
     * mobile number of the employee
     * <p>
     * can be proper phone number, or null (field wasn't set for this employee of the field not in the CSV)
     * <p>
     * Cannot be empty string
     */
    private final String mobile;

    /**
     * email of the employee
     * <p>
     * cannot be null or empty
     */
    private final String email;

    /**
     * higher importance places more weight on their requirements being satisfied
     * <p>
     * default will be 0
     */
    private final int importance;

    // we include the following fields as Integer rather than as int, so can detect if not set in csv as will be null
    /**
     * the maximum number of shifts an employee is allowed to take (mandatory to abide by)
     */
    private final Integer maxWeeklyShifts;

    /**
     * the minimum number of shifts the employee will be happy with (optional to abide by)
     * <p>
     * default will be 0
     */
    private final int minWeeklyShifts;

    /**
     * this availabilities list should have no overlaps (note they can meet end-to-end) - CLASS INVARIANT
     * <p>
     * additionally, guarantees that abutting availabilities with the same types have been merged (CLASS INVARIANT)
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
        assert ((employeeId != null)&&(!"".equals(employeeId))&&
            ((mobile==null)||MobileValidator.validMobileNonEmpty(mobile))&&
            (email != null)&&(new EmailValidator()).isValid(email));

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
     * add availabilities using a list of availability loaders
     * <p>
     * the availabilities stored in this class should have no overlaps in the availability objects (merging types together under a single list)
     * <p>
     * NOTE - it is possible here to get adjacent availabilities of the same type. Could perhaps write a method to merge them, but not necessary
     * @return list of availabilities
     */
    public void addAvailabilities(List<AvailabilityLoader> loadedAvailabilities){
        // algorithm matches the below link (except mine is inclusive for start/end whilst the example is exclusive)
        // https://softwareengineering.stackexchange.com/questions/363091/split-overlapping-ranges-into-all-unique-ranges?newreg=93383e379afe4dd3a595480528ee1541
        // this array will contain datetime, type of availability, and whether start/end (note false is sorted before true in Java by default)
        assert loadedAvailabilities != null;
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

            // these variable names are matching variable names of linked algorithm above
            DateTime n = firstTime.getValue0();
            String a = firstTime.getValue1();
            Boolean e = firstTime.getValue2();
            DateTime m = secondTime.getValue0();
            if (!e){
                currentTypesList.add(a);
            }
            else{
                currentTypesList.remove(a);
            }

            // note that checking here that n <= m would be redundant - it is guaranteed here, since the datetimes will have been sorted first, and n was before m
            // we use != because we don't want to bother with availabilities of 0 width
            if (n.compareTo(m) != 0){
                // use a hashset since can contain null, which we require
                processedAvailabilities.add(new Triplet<>(n, m, new HashSet<String>(currentTypesList)));
            }
        }

        // now planning to get processed availabilities without empty types. Additionally, if abutting availabilities have same types, merge them
        // set to size of processed availabilities list to avoid wastage due to copying when large
        List<Triplet<DateTime, DateTime, HashSet<String>>> processedAvailabilitiesNoEmptyTypesMinimal = new ArrayList<>(processedAvailabilities.size());
        for (Triplet<DateTime, DateTime, HashSet<String>> availabilityInfo: processedAvailabilities){
            HashSet<String> currentTypes = availabilityInfo.getValue2();
            DateTime currentStart = availabilityInfo.getValue0();
            // only add if the number of types > 0 - otherwise it's not actually an availability
            if (currentTypes.size() > 0){
                // if the current start abuts with the previous end (i.e. they're equal) and the types match, merge (note due to above algorithm wont have overlap)
                if ((processedAvailabilitiesNoEmptyTypesMinimal.size()>0) &&
                    // currentStart.equals(previousEnd) i.e. abutting
                    currentStart.equals(processedAvailabilitiesNoEmptyTypesMinimal.get(processedAvailabilitiesNoEmptyTypesMinimal.size()-1).getValue1())&&
                    // currentTypes.equals(previousTypes)
                    currentTypes.equals(processedAvailabilitiesNoEmptyTypesMinimal.get(processedAvailabilitiesNoEmptyTypesMinimal.size()-1).getValue2())){
                        DateTime previousStart = processedAvailabilitiesNoEmptyTypesMinimal.get(processedAvailabilitiesNoEmptyTypesMinimal.size()-1).getValue0();
                        DateTime currentEnd = availabilityInfo.getValue1();
                        // set the last last value to have the previous start and current end
                        processedAvailabilitiesNoEmptyTypesMinimal.set(processedAvailabilitiesNoEmptyTypesMinimal.size()-1,
                            // previous start, current end, previous types
                            new Triplet<>(previousStart, currentEnd, currentTypes));
                }
                else{
                    // no abutting, thus add to the end (keeping in mind no overlapping)
                    processedAvailabilitiesNoEmptyTypesMinimal.add(availabilityInfo);
                }
            }
        }

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
     * get list of availabilities covering shift by time if cover it. Note we DO NOT consider whether the employee has a clash or whether the types work
     * <p>
     * implementation relies on the assumption that there's no overlaps between availabilities (CLASS INVARIANT)
     * @param shift a shift we're considering taking
     * @return null if availabilities do not cover it, or the list of availabilities if they do
     */
    private List<Availability> availabilitiesFullyCoveringShiftTime(Shift shift){
        // TODO = test this has no overlaps in the returned availabilities (property based test)
        List<Availability> availabilitiesOverlappingShift = new LinkedList<>();
        for (Availability availability: availabilities){
            if (availability.overlapsWithTimePeriod(shift)){
                availabilitiesOverlappingShift.add(availability);
            }
        }

        // no availabilities to offer
        if (availabilitiesOverlappingShift.size() == 0){
            return null;
        }

        // applying the efficient algorithm here for merging overlapping intervals
        // https://www.geeksforgeeks.org/merging-intervals/
        // use pair of datetime rather than interval since intervals are ordered based on duration for jodatime
        List<Pair<DateTime, DateTime>> intervalInfo = new ArrayList<>();
        // add intervals for availabilities overlapping shift, because only they are relevant in determining whether shift can be satisfied
        for (Availability availability: availabilitiesOverlappingShift){
            intervalInfo.add(new Pair<>(availability.getStart(), availability.getEnd()));
        }
        Collections.sort(intervalInfo); // sort by start then end
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
        else{
            for (Availability availability: availabilitiesOverlappingShift){
                // if the availability includes null, then anything is fine (shouldn't have empty string)
                if ((!availability.getTypes().contains(null))&&(!availability.getTypes().contains(shift.getType()))){
                    return false;
                }
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
     * get the employee id
     * @return employee id
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * confirm whether the suggested weekly number of shifts fits under the maximum allowable number of shifts for this employee
     * <p>
     * this is separate from checking minimum because this will be a hard heuristic
     */
    public boolean numberOfShiftsUnderMaximum(int weeklyNumberOfShifts){
        return ((maxWeeklyShifts == null) || (weeklyNumberOfShifts <= maxWeeklyShifts));
    }

    /**
     * confirm whether the suggested weekly number of shifts above the minimum preferred number of shifts for this employee
     * <p>
     * this is separate from checking maximum as it will be a soft heuristic
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
     * get the maximum number of weekly shifts allowed for this employee
     * <p>
     * null if no maximum for the employee
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
