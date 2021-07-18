package com.roster123.employeescheduler.exceptions;

/**
 * exception representing that there is duplication of a planning id in a csv file.
 * 
 * This is an issue caused by the user
 */
public class DuplicatePlanningIdException extends Exception {
    public DuplicatePlanningIdException(String errorMessage) {
        super(errorMessage);
    }
}
