package employeeschedulingsystem;

import java.util.Comparator;

import org.javatuples.Triplet;

/**
 * special comparator for sorting triplets possibly with null values in them
 * <p>
 * this is needed because the sorting of triplets uses the default natural ordering:
 * <p>
 * https://github.com/javatuples/javatuples/blob/master/src/main/java/org/javatuples/Tuple.java#L232
 */
public class TripletWithNullsComparator<T1 extends _T1, T2 extends _T2, T3  extends _T3,
                                            _T1 extends Comparable<_T1>, _T2 extends Comparable<_T2>, _T3 extends Comparable<_T3>> implements Comparator<Triplet<T1, T2, T3>>{
    @Override
    public int compare(Triplet<T1, T2, T3> o1, Triplet<T1, T2, T3> o2) {
        int comparison = compareElements(o1.getValue0(), o2.getValue0());
        if (comparison != 0){
            return comparison;
        }

        comparison = compareElements(o1.getValue1(), o2.getValue1());
        if (comparison != 0){
            return comparison;
        }

        comparison = compareElements(o1.getValue2(), o2.getValue2());
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
     * @param e1
     * @param e2
     * @return
     */
    private <T extends Comparable<T>> int compareElements(T e1, T e2){
        if ((e1 == null)&&(e2 == null)){
            return 0;
        }
        else if (e1 == null){
            return -1;
        }
        else if(e2 == null){
            return 1;
        }
        else{
            return e1.compareTo(e2);
        }
    }
    
}