package com.roster123.employeescheduler.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * class representing an availability of an employee
 */
public class Availability extends TimePeriod{
    /** 
     * types of the availability.
     * 
     * NOTE - null in this list means anything is allowed.
     * 
     * Must specifically be a hashset so that it can contain null (e.g. TreeSet cannot contain null).
     */
    private final HashSet<String> types;

    /**
     * constructor taking all input
     */
    public Availability(HashSet<String> types, DateTime start, DateTime end) {
        super(start, end);
        assert types != null;
        this.types = new HashSet<>(types);
    }

    /**
     * get unmodifiable hashset of the types.
     * 
     * Note whilst this returns a set type, unmodifiableSet is just a wrapper around our HashSet rather than a new set of unknown type:
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Collections.html#unmodifiableSet(java.util.Set)
     * @return unmodifiable hashset of types
     */
    public Set<String> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    @Override
    public boolean equals(Object obj){
        if (!super.equals(obj)){
            return false;
        }
        Availability other = (Availability)obj;
        return Objects.equals(types, other.types);
    }

    @Override
    public String toString(){
        return super.toString()+", types="+types;
    }

    @Override
    public int hashCode() {
        return Objects.hash(types, super.hashCode());
    }
}
