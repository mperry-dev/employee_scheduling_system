package tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;

import employeeschedulingsystem.*;

public class TripletWithNullsComparatorTest {
    private TripletWithNullsComparator<Integer, Integer, Integer, Integer, Integer, Integer> comparator = new TripletWithNullsComparator<>();

    @Test
    public void testNumbersSwitchAtIndex0(){
        List<Triplet<Integer, Integer, Integer>> numbers = new ArrayList<>(Arrays.asList(new Triplet<>(0, 0, 0), new Triplet<>(1,1,1), new Triplet<>(1,1,1), new Triplet<>(0, 0, 0)));
        List<Triplet<Integer, Integer, Integer>> expectedResult = new ArrayList<>(Arrays.asList(new Triplet<>(0, 0, 0), new Triplet<>(0, 0, 0), new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1)));
        Collections.sort(numbers, comparator);
        assertArrayEquals(expectedResult.toArray(), numbers.toArray());
    }

    @Test
    public void testNumbersSwitchAtIndex1(){
        List<Triplet<Integer, Integer, Integer>> numbers = new ArrayList<>(Arrays.asList(new Triplet<>(1, 0, 0), new Triplet<>(1,1,1), new Triplet<>(1,1,1), new Triplet<>(1, 0, 0)));
        List<Triplet<Integer, Integer, Integer>> expectedResult = new ArrayList<>(Arrays.asList(new Triplet<>(1, 0, 0), new Triplet<>(1, 0, 0), new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1)));
        Collections.sort(numbers, comparator);
        assertArrayEquals(expectedResult.toArray(), numbers.toArray());
    }

    @Test
    public void testNumbersSwitchAtIndex2(){
        List<Triplet<Integer, Integer, Integer>> numbers = new ArrayList<>(Arrays.asList(new Triplet<>(1, 1, 0), new Triplet<>(1,1,1), new Triplet<>(1,1,1), new Triplet<>(1, 1, 0)));
        List<Triplet<Integer, Integer, Integer>> expectedResult = new ArrayList<>(Arrays.asList(new Triplet<>(1, 1, 0), new Triplet<>(1, 1, 0), new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1)));
        Collections.sort(numbers, comparator);
        assertArrayEquals(expectedResult.toArray(), numbers.toArray());
    }

    @Test
    public void testNumbersAllSame(){
        List<Triplet<Integer, Integer, Integer>> numbers = new ArrayList<>(Arrays.asList(new Triplet<>(1, 1, 1), new Triplet<>(1,1,1), new Triplet<>(1,1,1), new Triplet<>(1, 1, 1)));
        List<Triplet<Integer, Integer, Integer>> expectedResult = new ArrayList<>(Arrays.asList(new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1), new Triplet<>(1, 1, 1)));
        Collections.sort(numbers, comparator);
        assertArrayEquals(expectedResult.toArray(), numbers.toArray());
    }
}
