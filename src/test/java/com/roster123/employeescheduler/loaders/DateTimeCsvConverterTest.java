package com.roster123.employeescheduler.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import org.apache.commons.lang3.ArrayUtils;

public class DateTimeCsvConverterTest {
    private DateTimeCsvConverter loader;

    @BeforeEach
    public void before(){
        loader = new DateTimeCsvConverter();
    }

    @Test
    public void testNullString(){
        assertThrows(NullPointerException.class, ()->{loader.convert(null);});
    }

    @Test
    public void testNullDateTime(){
        assertThrows(IllegalArgumentException.class, ()->{DateTimeCsvConverter.convertDateTimeToString(null);});
    }

    @ParameterizedTest
    @MethodSource("validStringToDateTimeParameters")
    public void testValidStringToDateTimes(String s, DateTime dt) throws CsvDataTypeMismatchException, CsvConstraintViolationException{
        assertEquals(dt, loader.convert(s));
    }

    @ParameterizedTest
    @MethodSource("invalidStringToDateTimeParameters")
    public void testInvalidStringToDateTimes(String s){
        assertThrows(CsvDataTypeMismatchException.class, ()->{loader.convert(s);});
    }

    @ParameterizedTest
    @MethodSource("validDateTimeToStringParameters")
    public void testValidDateTimeToStrings(DateTime dt, String s){
        assertEquals(s, DateTimeCsvConverter.convertDateTimeToString(dt));
    }

    @ParameterizedTest
    @MethodSource("validStringPairedWithDateTimeParametersNonSymmetricCases")
    public void testValidStringPairedWithDateTimeParametersNonSymmetricCases(String s, DateTime dt){
        assertNotEquals(s, DateTimeCsvConverter.convertDateTimeToString(dt));
    }

    @ParameterizedTest
    @MethodSource("validDateTimePairedWithStringNonSymmetricCases")
    public void testValidDateTimePairedWithStringNonSymmetricCases(DateTime dt, String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException{
        assertNotEquals(dt, loader.convert(s));
    }


    // examples of valid cases where the string maps to the datetime, and the datetime is expected to map back to the string
    private static Stream<Arguments> validStringPairedWithDateTimeParametersSymmetricCases() {
        return Stream.of(
            Arguments.of("7/10/2021 10:30" , new DateTime(2021, 10, 7, 10, 30)),
            Arguments.of("17/10/2021 10:30" , new DateTime(2021, 10, 17, 10, 30)),
            Arguments.of("17/10/2021 8:30" , new DateTime(2021, 10, 17, 8, 30)),
            Arguments.of("7/10/2021 8:30" , new DateTime(2021, 10, 7, 8, 30)),
            Arguments.of("7/10/2021 17:30" , new DateTime(2021, 10, 7, 17, 30)),
            Arguments.of("17/10/2021 17:30" , new DateTime(2021, 10, 17, 17, 30)),
            Arguments.of("7/10/2021 23:30" , new DateTime(2021, 10, 7, 23, 30)),
            Arguments.of("17/10/2021 23:30" , new DateTime(2021, 10, 17, 23, 30)),
            Arguments.of("7/10/2021 12:30" , new DateTime(2021, 10, 7, 12, 30)),
            Arguments.of("17/10/2021 12:30" , new DateTime(2021, 10, 17, 12, 30)),
            Arguments.of("7/10/2021 0:30" , new DateTime(2021, 10, 7, 0, 30)),
            Arguments.of("17/10/2021 0:30" , new DateTime(2021, 10, 17, 0, 30)),
            
            Arguments.of("7/09/2021 10:30" , new DateTime(2021, 9, 7, 10, 30)),
            Arguments.of("17/09/2021 10:30" , new DateTime(2021, 9, 17, 10, 30)),
            Arguments.of("17/09/2021 8:30" , new DateTime(2021, 9, 17, 8, 30)),
            Arguments.of("7/09/2021 8:30" , new DateTime(2021, 9, 7, 8, 30)),
            Arguments.of("7/09/2021 17:30" , new DateTime(2021, 9, 7, 17, 30)),
            Arguments.of("17/09/2021 17:30" , new DateTime(2021, 9, 17, 17, 30)),
            Arguments.of("7/09/2021 23:30" , new DateTime(2021, 9, 7, 23, 30)),
            Arguments.of("17/09/2021 23:30" , new DateTime(2021, 9, 17, 23, 30)),
            Arguments.of("7/09/2021 12:30" , new DateTime(2021, 9, 7, 12, 30)),
            Arguments.of("17/09/2021 12:30" , new DateTime(2021, 9, 17, 12, 30)),
            Arguments.of("7/09/2021 0:30" , new DateTime(2021, 9, 7, 0, 30)),
            Arguments.of("17/09/2021 0:30" , new DateTime(2021, 9, 17, 0, 30)),

            Arguments.of("7/10/0000 10:30", new DateTime(0, 10, 7, 10, 30)),
            Arguments.of("7/10/0012 10:30", new DateTime(12, 10, 7, 10, 30)),
            Arguments.of("7/01/2021 10:30", new DateTime(2021, 1, 7, 10, 30)),
            Arguments.of("7/10/2021 10:03", new DateTime(2021, 10, 7, 10, 3))
        );
    }

    // examples of valid cases where the string maps to the datetime, but the datetime does not map back to the string
    private static Stream<Arguments> validStringPairedWithDateTimeParametersNonSymmetricCases(){
        return Stream.of(
            Arguments.of("07/10/2021 10:30" , new DateTime(2021, 10, 7, 10, 30)),
            Arguments.of("07/10/2021 8:30" , new DateTime(2021, 10, 7, 8, 30)),
            Arguments.of("7/10/2021 08:30" , new DateTime(2021, 10, 7, 8, 30)),
            Arguments.of("07/10/2021 08:30" , new DateTime(2021, 10, 7, 8, 30)),
            Arguments.of("07/10/2021 17:30" , new DateTime(2021, 10, 7, 17, 30)),
            Arguments.of("07/10/2021 23:30" , new DateTime(2021, 10, 7, 23, 30)),
            Arguments.of("07/10/2021 12:30" , new DateTime(2021, 10, 7, 12, 30)),
            Arguments.of("07/10/2021 00:30" , new DateTime(2021, 10, 7, 0, 30)),
            Arguments.of("17/10/2021 00:30" , new DateTime(2021, 10, 17, 0, 30)),

            Arguments.of("7/10/0 10:30", new DateTime(0, 10, 7, 10, 30)),
            Arguments.of("7/10/012 10:30", new DateTime(12, 10, 7, 10, 30)),
            Arguments.of("7/1/2021 10:30", new DateTime(2021, 1, 7, 10, 30)),
            Arguments.of("7/10/2021 10:3", new DateTime(2021, 10, 7, 10, 3))
        );
    }

    // example of DateTimes where the DateTime maps to the String, but the String does not map to the DateTime
    // note all cases of a valid DateTime object will map to a String, since extra information is dropped, and default is 0
    private static Stream<Arguments> validDateTimePairedWithStringNonSymmetricCases(){
        return Stream.of(
            Arguments.of(new DateTime(2021, 10, 7, 10, 30, 20), "7/10/2021 10:30"),
            Arguments.of(new DateTime(2021, 10, 7, 10, 30, 20, 30), "7/10/2021 10:30")
        );
    }

    // examples of strings which cannot be converted to DateTime
    private static Stream<Arguments> invalidStringToDateTimeParameters(){
        return Stream.of(
            Arguments.of("7/10/2021 10:30:20"),
            Arguments.of("0/10/2021 10:30"),
            Arguments.of("7/00/2021 10:30"),
            Arguments.of("7/10/2021 24:30"),
            Arguments.of("7/10/2021 23:60"),
            Arguments.of("7/10/2021 2330"),
            Arguments.of("710/2021 23:30"),
            Arguments.of("7/102021 23:30"),
            Arguments.of("7102021 23:30"),
            Arguments.of("7102021 2330"),
            Arguments.of("71020212330"),
            Arguments.of(""),
            Arguments.of("7/10/202110:30"),
            Arguments.of("7/10/2021  10:30"),
            Arguments.of("abc"),
            Arguments.of("7/10/2021 10"),
            Arguments.of("7/10/2021"),
            Arguments.of("10/2021 10:30:20"),
            Arguments.of("10/2021 10:30"),
            Arguments.of("2021 10:30:20"),
            Arguments.of("2021 10:30"),
            Arguments.of("10:30:20"),
            Arguments.of("10:30")
        );
    }

    // examples where converting the string should get the corresponding datetime
    private static Stream<Arguments> validStringToDateTimeParameters(){
        return Stream.concat(validStringPairedWithDateTimeParametersSymmetricCases(), validStringPairedWithDateTimeParametersNonSymmetricCases());
    }

    // examples where converting the datetime should get the corresponding string
    private static Stream<Arguments> validDateTimeToStringParameters(){
        return Stream.concat(flipArgumentsInStream(validStringPairedWithDateTimeParametersSymmetricCases()), validDateTimePairedWithStringNonSymmetricCases());
    }

    private static Arguments copyAndReturnReversedArguments(Arguments arguments){
        Object args[] = arguments.get().clone();
        ArrayUtils.reverse(args);
        return Arguments.of(args);

    }

    private static Stream<Arguments> flipArgumentsInStream(Stream<Arguments> stream){
        return stream.map(arguments->copyAndReturnReversedArguments(arguments));
    }
}
