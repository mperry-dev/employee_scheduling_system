package com.roster123.employeescheduler.domain;

import java.util.Comparator;

import org.javatuples.Triplet;

/**
 * special comparator for sorting triplets possibly with null values in them.
 * 
 * This is needed because the sorting of triplets uses the default natural ordering:
 * https://github.com/javatuples/javatuples/blob/master/src/main/java/org/javatuples/Tuple.java#L232
 */
public class TripletWithNullsComparator<T1 extends _T1, T2 extends _T2, T3  extends _T3,
                                            _T1 extends Comparable<_T1>, _T2 extends Comparable<_T2>, _T3 extends Comparable<_T3>> implements Comparator<Triplet<T1, T2, T3>>{
    @Override
    public int compare(Triplet<T1, T2, T3> triplet1, Triplet<T1, T2, T3> triplet2) {
        int comparison = compareElements(triplet1.getValue0(), triplet2.getValue0());
        if (comparison != 0){
            return comparison;
        }

        comparison = compareElements(triplet1.getValue1(), triplet2.getValue1());
        if (comparison != 0){
            return comparison;
        }

        comparison = compareElements(triplet1.getValue2(), triplet2.getValue2());
        if (comparison != 0){
            return comparison;
        }
        else{
            return 0;
        }
    }

    /**
     * comparison function which sorts null to the beginning
     * @param <T>
     * @param element1
     * @param element2
     * @return
     */
    private <T extends Comparable<T>> int compareElements(T element1, T element2){
        if ((element1 == null)&&(element2 == null)){
            return 0;
        }
        else if (element1 == null){
            return -1;
        }
        else if(element2 == null){
            return 1;
        }
        else{
            return element1.compareTo(element2);
        }
    }
    
}