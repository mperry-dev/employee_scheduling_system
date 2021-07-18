package com.roster123.employeescheduler.exceptions;

/**
 * checked exception to be thrown when an unchecked exception thrown by opencsv when reading file
 */
public class CsvInputInvalidException extends Exception {
    public CsvInputInvalidException(String message){
        super(message);
    }
}
