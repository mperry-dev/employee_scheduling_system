package employeeschedulingsystem;

/**
 * exception to indicate an availability employee id didn't match up to any employee
 */
public class AvailabilityEmployeeIdNotMatchingAnyEmployeeException extends Exception {
    public AvailabilityEmployeeIdNotMatchingAnyEmployeeException(String message){
        super(message);
    }
}
