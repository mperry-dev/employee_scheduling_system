package employeeschedulingsystem;

/**
 * exception representing that the start time is after the end time (or at the same time) in a CSV file
 * <p>
 * this is an issue caused by the user
 */
public class StartAfterEndException extends Exception {
    public StartAfterEndException(String errorMessage) {
        super(errorMessage);
    }
}
