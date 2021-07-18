package com.roster123.employeescheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;

import org.apache.commons.collections4.ListUtils;
import org.javatuples.Triplet;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import com.roster123.employeescheduler.domain.*;
import com.roster123.employeescheduler.exceptions.*;
import com.roster123.employeescheduler.loaders.*;

/**
 * main class for running module.
 * 
 * Only custom checked exceptions should be thrown when using this class (no asserts in classes it uses, etc, even if invalid user input)
 */
public class EmployeeSchedulingSystem{
    /**
     * collection of employees
     */
    private final List<Employee> employees;

    /**
     * unallocated shifts.
     * 
     * The union of the shifts in this and the shifts in allocatedShifts forms the collection of all shifts.
     * 
     * There should be no intersection between this list and the allocatedShifts
     */
    private final List<Shift> unallocatedShifts;

    /**
     * the allocated shifts, if there are any.
     * 
     * The union of the shifts in this and the shifts in unallocatedShifts forms the collection of all shifts.
     * 
     * There should be no intersection between this list and the unallocatedShifts
     */
    private List<Shift> allocatedShifts;

    /**
     * empty option loads from database
     */
    public EmployeeSchedulingSystem(){
        employees = new ArrayList<>();
        unallocatedShifts = new ArrayList<>();
        allocatedShifts = new ArrayList<>();
    }

    /**
     * accepts csv strings containing information about employees and a separate csv string containing information about their availability, to produce employees with availabilities.
     * 
     * We accept csv string for this since don't have to store file, and limiting csv file to 50KB (might tweak limit, but anything above 50KB likely spam).
     * @param informationCsvData a csv string with a unique business identifier for each employee, and name, mobile, email, importance, max weekly shifts, and min weekly shifts
     * @param availabilityCsvData a csv string where each row shows an availability period for an employee, identified by their business identifier
     * @throws DuplicatePlanningIdException thrown if have duplicate employees being created
     * @throws StartAfterEndException thrown if employee availability start >= end
     * @throws CsvInputInvalidException
     * @throws AvailabilityEmployeeIdNotMatchingAnyEmployeeException thrown if employee availability employeeId doesn't match any employee
     */
    public void processEmployeesInformationAndAvailabilityCSVs(String informationCsvData, String availabilityCsvData) throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        processEmployeeInformationCsv(informationCsvData);
        processEmployeeAvailabilityCsv(availabilityCsvData);
    }
    
    /**
     * process string of employee information in csv format into employee objects
     * @param informationCsvData a csv string with a unique business identifier for each employee, and name, mobile, email, importance, max weekly shifts, and min weekly shifts
     * @throws DuplicatePlanningIdException exception can be thrown if the employee ids are not unique
     * @throws CsvInputInvalidException
     */
    public void processEmployeeInformationCsv(String informationCsvData) throws DuplicatePlanningIdException, CsvInputInvalidException{
        List<EmployeeLoader> beans;
        try{
            beans = new CsvToBeanBuilder<EmployeeLoader>(new StringReader(informationCsvData))
                    .withType(EmployeeLoader.class)
                    .build()
                    .parse();
        }
        catch (RuntimeException e){
            throw new CsvInputInvalidException("failure trying to process file employee_information.csv");
        }
        for (EmployeeLoader loader: beans){
            Employee employee = loader.getEmployee();
            // could make more efficient, but n^2 complexity isn't likely to be an issue as employees list is employees reporting to the manager
            for (Employee existingEmployee: employees){
                // check here ensures that employee ids are unique
                if (employee.matchesEmployeeEmployeeId(existingEmployee)){
                    throw new DuplicatePlanningIdException("employeeId="+employee.getEmployeeId()+" for "+employee+" is a duplicate of "+existingEmployee);
                }
            }
            employees.add(employee);
        }
    }

    /**
     * process string of availability information in csv format, so it is stored in employees.
     * 
     * Must be run after running processEmployeeInformationCsv - otherwise don't know whether the business ids for employees are accurate
     * @param availabilityCsvData a csv string where each row shows an availability period for an employee, identified by their business identifier, timing of this availability, and the type of shift they can do
     * @throws StartAfterEndException thrown if availability start >= end
     * @throws CsvInputInvalidException
     * @throws AvailabilityEmployeeIdNotMatchingAnyEmployeeException thrown if availability employeeId doesn't match any employee
     */
    private void processEmployeeAvailabilityCsv(String availabilityCsvData) throws StartAfterEndException, CsvInputInvalidException, AvailabilityEmployeeIdNotMatchingAnyEmployeeException{
        List<AvailabilityLoader> beans;
        try{
            beans = new CsvToBeanBuilder<AvailabilityLoader>(new StringReader(availabilityCsvData))
                    .withType(AvailabilityLoader.class)
                    .build()
                    .parse();
        }
        catch (RuntimeException e){
            throw new CsvInputInvalidException("failure trying to process file employee_availability.csv");
        }
        // the set of availability loaders which haven't been connected to any employee
        Set<AvailabilityLoader> unusedAvailabilityLoaders = new HashSet<>(beans);
        for (Employee employee: employees){
            List<AvailabilityLoader> loadedAvailabilities = new LinkedList<>();
            for (AvailabilityLoader loader: beans){
                if (!loader.checkValidTimePeriod()){
                    throw new StartAfterEndException("availability csv has start after end");
                }
                if (employee.availabilityLoaderForThisEmployee(loader)){
                    loadedAvailabilities.add(loader);
                    // indicate the availability has matched to an employee
                    unusedAvailabilityLoaders.remove(loader);
                }
            }
            employee.addAvailabilities(loadedAvailabilities);
        }
        // raise exception if don't line up availability to an employee
        if (unusedAvailabilityLoaders.size() > 0){
            throw new AvailabilityEmployeeIdNotMatchingAnyEmployeeException("availability CSV has following loaded availabilities with employee id not matching any employee = "+unusedAvailabilityLoaders);
        }
    }

    /**
     * accepts csv strings containing information about shifts, to produce shift objects
     * @param shiftCsvData a csv string with a unique business identifier for each shift, the type of shift, and start and end times
     * @throws DuplicatePlanningIdException exception can be thrown if the shift ids are not unique
     * @throws StartAfterEndException thrown if shift start >= end
     * @throws CsvInputInvalidException
     */
    public void processShiftsCsv(String shiftCsvData) throws DuplicatePlanningIdException, StartAfterEndException, CsvInputInvalidException{
        List<ShiftLoader> beans;
        try{
            beans = new CsvToBeanBuilder<ShiftLoader>(new StringReader(shiftCsvData))
                    .withType(ShiftLoader.class)
                    .build()
                    .parse();
        }
        catch (RuntimeException e){
            throw new CsvInputInvalidException("failure trying to process file shift_information.csv");
        }
        for (ShiftLoader loader: beans){
            if (!loader.checkValidTimePeriod()){
                throw new StartAfterEndException("shift csv has start after end");
            }
            Shift shift = loader.getShift();
            // could make more efficient, but n^2 complexity isn't likely to be an issue as shifts list is shifts managed by the manager
            for (Shift existingShift: ListUtils.union(unallocatedShifts, allocatedShifts)) {
                // check here ensures that shift ids are unique
                if (shift.matchesShiftByShiftId(existingShift)){
                    throw new DuplicatePlanningIdException("shiftId="+shift.getShiftId()+" for "+shift+" is a duplicate of "+existingShift);
                }
            }
            unallocatedShifts.add(shift);
        }
    }

    /**
     * run optaplanner to try and satisfy constraints, and produce a good allocation
     * @param secondsToSpend the number of seconds to spend if configured (e.g. by a test), otherwise use the config file setting
     * @return the score of the allocations
     */
    public HardMediumSoftScore allocateShifts(Long secondsToSpend){
        // look at example 3.2 here
        // https://access.redhat.com/documentation/en-us/red_hat_decision_manager/7.2/html/getting_started_with_red_hat_business_optimizer/cloudbal-tutorial-con
        
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("employeesSchedulingSolverConfig.xml");
        
        // if was configured by the user, set the number of seconds to spend
        if (secondsToSpend != null){
            TerminationConfig terminationConfig = new TerminationConfig();
            terminationConfig.setSecondsSpentLimit(secondsToSpend);
            solverConfig.setTerminationConfig(terminationConfig);
        }

        SolverFactory<Allocations> solverFactory = SolverFactory.create(solverConfig);
        Solver<Allocations> solver = solverFactory.buildSolver();

        Allocations unsolvedAllocations = new Allocations(ListUtils.union(unallocatedShifts, allocatedShifts), employees);
        Allocations solvedAllocations = solver.solve(unsolvedAllocations);

        // transfer all shifts as now allocated
        allocatedShifts = new ArrayList<>(solvedAllocations.getShifts());
        unallocatedShifts.clear();

        printSolverDebuggingInfo(solverFactory, solvedAllocations);

        return solvedAllocations.getScore();
    }

    /**
     * helper method to assist in debugging constraints
     * @param solverFactory
     * @param solution
     */
    private static void printSolverDebuggingInfo(SolverFactory<Allocations> solverFactory, Allocations solution) {
        // https://docs.optaplanner.org/latest/optaplanner-docs/html_single/index.html#usingScoreCalculationOutsideTheSolver
        ScoreManager<Allocations, HardMediumSoftScore> scoreManager = ScoreManager.create(solverFactory);
        ScoreExplanation<Allocations, HardMediumSoftScore> scoreExplanation = scoreManager.explainScore(solution);
        System.out.println("\n################### EXPLANATION OF SCORE");
        System.out.println(scoreExplanation.toString());

        System.out.println("\n################### ALLOCATIONS INFORMATION ");
        System.out.println(solution);
        System.out.println();
    }

    /**
     * put solved allocations into a CSV file.
     * 
     * Note no unchecked exceptions should be raised by opencsv if program is correct
     * @param allocations solved allocations
     * @param path the file to store the allocations information in
     * @throws IOException exception can be thrown if don't have the permission to write to the file, or some error happens in writing to file otherwise
     */
    private void writeAllocationsToCsv(Path path) throws IOException{
        // remove the quotes with CSVWriter.NO_QUOTE_CHARACTER
        CSVWriter writer = new CSVWriter(new FileWriter(path.toString()), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        // add headers
        writer.writeNext(new String[]{"shiftId","employeeId","start","end","type"});
        for (Shift shift: allocatedShifts){
            writer.writeNext(shift.getShiftCsvString());
        }
        writer.close();
    }

    /**
     * get a string without special characters on the first line (only ASCII on first line).
     * 
     * Needed because Excel adds special characters into CSV files on the first line.
     * 
     * Note we don't strip off later lines, since it is valid for email addresses and names to have special characters
     * @param path path of the file
     * @throws IOException exception can be thrown if don't have permission to read from the file, or some error happens in reading from the file otherwise
     */
    private static String getCsvStringWithoutSpecialCharactersOnFirstLine(Path path) throws IOException{
        // excel files seem to automatically add UTF 65279 as first character - remove non-ascii on the first line only
        // https://www.baeldung.com/guava-string-charmatcher
        String csvString = Files.readString(path, StandardCharsets.UTF_8);
        String[] firstLineAndEverythingElse = csvString.split("\n", 2);  // get 2 results, i.e. split once
        firstLineAndEverythingElse[0] = CharMatcher.ascii().retainFrom(firstLineAndEverythingElse[0]);
        return String.join("\n", firstLineAndEverythingElse);
    }

    /**
     * user-friendly wrapper of getCsvStringWithoutSpecialCharacters which prints a message and returns null when something goes wrong with reading file
     * @param path path of the file
     * @return String of csv data loaded from the file
     */
    private static String getCsvStringWithoutSpecialCharactersUserFriendly(Path path){
        try{
            return getCsvStringWithoutSpecialCharactersOnFirstLine(path);
        }
        catch(IOException e){
            System.out.println("File "+path+" could not be loaded");
            return null;
        }
    }

    public static Path getPathOfCsvFolder(String inputPath){
        if ((new File(inputPath)).isAbsolute()){
            // absolute path
            return FileSystems.getDefault().getPath(inputPath);
        }
        else{
            // relative path
            return FileSystems.getDefault().getPath(System.getProperty("user.dir"), inputPath);
        }
    }

    /**
     * get triplet of employeeAvailabilityCsvString, employeeInformationCsvString, shiftInformationCsvString from the user input configuration
     * @param inputPath
     * @return triplet of employeeAvailabilityCsvString, employeeInformationCsvString, shiftInformationCsvString. Can have nulls
     */
    public static Triplet<String, String, String> getDataFromFiles(Path commandLineCsvFolder) {
        String employeeAvailabilityCsvString = getCsvStringWithoutSpecialCharactersUserFriendly(commandLineCsvFolder.resolve("employee_availability.csv"));
        String employeeInformationCsvString = getCsvStringWithoutSpecialCharactersUserFriendly(commandLineCsvFolder.resolve("employee_information.csv"));
        String shiftInformationCsvString = getCsvStringWithoutSpecialCharactersUserFriendly(commandLineCsvFolder.resolve("shift_information.csv"));

        return new Triplet<>(employeeAvailabilityCsvString, employeeInformationCsvString, shiftInformationCsvString);
    }

    public static void main(String []args){
        if (args.length != 1){
            System.out.println("should be specifying precisely 1 command line argument - the path of the folder containing CSV data");
            return;
        }

        Path commandLineCsvFolder = getPathOfCsvFolder(args[0]);
        
        Triplet<String, String, String> csvData = getDataFromFiles(commandLineCsvFolder);
        String employeeAvailabilityCsvString = csvData.getValue0();
        String employeeInformationCsvString = csvData.getValue1();
        String shiftInformationCsvString = csvData.getValue2();
        
        if (employeeAvailabilityCsvString == null || employeeInformationCsvString == null || shiftInformationCsvString == null){
            // message given to user when ran getCsvStringWithoutSpecialCharactersUserFriendly, so don't need extra message
            return;
        }

        EmployeeSchedulingSystem employeeSchedulingSystem = new EmployeeSchedulingSystem();
        // TODO = improve this, not granular
        try{
            employeeSchedulingSystem.processEmployeesInformationAndAvailabilityCSVs(employeeInformationCsvString, employeeAvailabilityCsvString);
        }
        catch (DuplicatePlanningIdException e){
            System.out.println("Your CSV file employee_availability.csv has a duplicate employee id");
            return;
        }
        catch (StartAfterEndException e){
            System.out.println("Your CSV file employee_availability.csv has 1 or more start times at the same time or after the corresponding end times");
            return;
        } catch (CsvInputInvalidException e) {
            System.out.println("Your CSV file employee_availability.csv or employee_information.csv disobeys the format requirements");
            return;
        } catch (AvailabilityEmployeeIdNotMatchingAnyEmployeeException e) {
            System.out.println("Your CSV employee_availability.csv has employee ids which don't match any employee in employee_information.csv");
        }
        try{
            employeeSchedulingSystem.processShiftsCsv(shiftInformationCsvString);
        }
        catch (DuplicatePlanningIdException e){
            System.out.println("Your CSV file shift_information.csv has a duplicate shift id");
            return;
        }
        catch (StartAfterEndException e){
            System.out.println("Your CSV file shift_information.csv has 1 or more start times at the same time or after the corresponding end times");
        } catch (CsvInputInvalidException e) {
            System.out.println("Your CSV file shift_information.csv or employee_information.csv disobeys the format requirements");
        }

        HardMediumSoftScore score = employeeSchedulingSystem.allocateShifts(null);
        
        if (score.isFeasible()){
            System.out.println("ALLOCATION SUCCESS. Score is = "+score.toString());
            try{
                employeeSchedulingSystem.writeAllocationsToCsv(commandLineCsvFolder.resolve("ALLOCATIONS_OUTPUT.csv"));
            }
            catch (IOException e){
                System.out.println("we couldn't write to the file ALLOCATIONS_OUTPUT.csv");
            }
        }
        else{
            System.out.println("ALLOCATION FAILED. Score is = "+score.toString());
        }
    }
}