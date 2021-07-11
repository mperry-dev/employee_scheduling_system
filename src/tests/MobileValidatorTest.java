package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import com.opencsv.exceptions.CsvValidationException;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import org.junit.jupiter.api.BeforeEach;

import employeeschedulingsystem.*;

@RunWith(JUnitQuickcheck.class)
public class MobileValidatorTest {
    private MobileValidator validator;

    @BeforeEach
    public void before(){
        validator = new MobileValidator();
    }

    @Test
    public void testSetParameterString(){
        validator.setParameterString("");
    }

    @ParameterizedTest
    @MethodSource("mobileValidityParameters")
    public void testIsValid(String mobile, boolean isValid){
        if (isValid != validator.isValid(mobile)){
            System.out.println(mobile+" "+isValid);
        }
        assertEquals(isValid, validator.isValid(mobile));
    }

    @ParameterizedTest
    @MethodSource("mobileValidityParameters")
    public void testValid(String mobile, boolean isValid){
        if (isValid){
            assertTrue(validator.isValid(mobile));
        }
        else{
            assertThrows(CsvValidationException.class, ()->{validator.validate(mobile, new DateTimeCsvLoader());});
        }
    }

    @Property(trials=10000)
    public void checkMobileValidatorConsistency(){
        assertFalse((new MobileValidator()).isValid("1234567"));
    }

    private static Stream<Arguments> mobileValidityParameters() {
        return Stream.of(
                // empty string is validated since mobile is optional
                Arguments.of("", true),

                Arguments.of("abc@gmail.com", false),
                Arguments.of("0", false),
                Arguments.of("+", false),
                Arguments.of("+0", false),

                // no international code
                Arguments.of("0491570006", false),
                
                // from France
                // https://www.regextester.com/95062
                // https://en.wikipedia.org/wiki/Telephone_numbers_in_France
                Arguments.of("+33199001234", false),
                Arguments.of("+33261911234", false),
                Arguments.of("+33353011234", false),
                Arguments.of("+33465711234", false),
                Arguments.of("+33536491234", false),
                Arguments.of("+33639981234", true),
                Arguments.of("+33612345678", true),
                Arguments.of("+33712345678", true),


                // all remaining numbers are tweaked from:  https://en.wikipedia.org/wiki/Fictitious_telephone_number

                // these lack international codes
                Arguments.of("0491570006", false),
                Arguments.of("0491570156", false),
                Arguments.of("0491570157", false),
                Arguments.of("0491570158", false),
                Arguments.of("0491570159", false),
                Arguments.of("0491570110", false),
                Arguments.of("0491570313", false),
                Arguments.of("0491570737", false),
                Arguments.of("0491571266", false),
                Arguments.of("0491571491", false),
                Arguments.of("0491571804", false),
                Arguments.of("0491572549", false),
                Arguments.of("0491572665", false),
                Arguments.of("0491572983", false),
                Arguments.of("0491573770", false),
                Arguments.of("0491573087", false),
                Arguments.of("0491574118", false),
                Arguments.of("0491574632", false),
                Arguments.of("0491575254", false),
                Arguments.of("0491575789", false),
                Arguments.of("0491576398", false),
                Arguments.of("0491576801", false),
                Arguments.of("0491577426", false),
                Arguments.of("0491577644", false),
                Arguments.of("0491578957", false),
                Arguments.of("0491578148", false),
                Arguments.of("0491578888", false),
                Arguments.of("0491579212", false),
                Arguments.of("0491579760", false),
                Arguments.of("0491579455", false),

                // Australian mobile numbers with international code
                Arguments.of("+61491570006", true),
                Arguments.of("+61491570156", true),
                Arguments.of("+61491570157", true),
                Arguments.of("+61491570158", true),
                Arguments.of("+61491570159", true),
                Arguments.of("+61491570110", true),
                Arguments.of("+61491570313", true),
                Arguments.of("+61491570737", true),
                Arguments.of("+61491571266", true),
                Arguments.of("+61491571491", true),
                Arguments.of("+61491571804", true),
                Arguments.of("+61491572549", true),
                Arguments.of("+61491572665", true),
                Arguments.of("+61491572983", true),
                Arguments.of("+61491573770", true),
                Arguments.of("+61491573087", true),
                Arguments.of("+61491574118", true),
                Arguments.of("+61491574632", true),
                Arguments.of("+61491575254", true),
                Arguments.of("+61491575789", true),
                Arguments.of("+61491576398", true),
                Arguments.of("+61491576801", true),
                Arguments.of("+61491577426", true),
                Arguments.of("+61491577644", true),
                Arguments.of("+61491578957", true),
                Arguments.of("+61491578148", true),
                Arguments.of("+61491578888", true),
                Arguments.of("+61491579212", true),
                Arguments.of("+61491579760", true),
                Arguments.of("+61491579455", true),

                // Australian mobile numbers with international code BUT
                // the 2 in +612 shouldn't be added as per https://www.stylemanual.gov.au/style-rules-and-conventions/numbers-and-measurements/telephone-numbers
                Arguments.of("+612491570006", false),
                Arguments.of("+612491570156", false),
                Arguments.of("+612491570157", false),
                Arguments.of("+612491570158", false),
                Arguments.of("+612491570159", false),
                Arguments.of("+612491570110", false),
                Arguments.of("+612491570313", false),
                Arguments.of("+612491570737", false),
                Arguments.of("+612491571266", false),
                Arguments.of("+612491571491", false),
                Arguments.of("+612491571804", false),
                Arguments.of("+612491572549", false),
                Arguments.of("+612491572665", false),
                Arguments.of("+612491572983", false),
                Arguments.of("+612491573770", false),
                Arguments.of("+612491573087", false),
                Arguments.of("+612491574118", false),
                Arguments.of("+612491574632", false),
                Arguments.of("+612491575254", false),
                Arguments.of("+612491575789", false),
                Arguments.of("+612491576398", false),
                Arguments.of("+612491576801", false),
                Arguments.of("+612491577426", false),
                Arguments.of("+612491577644", false),
                Arguments.of("+612491578957", false),
                Arguments.of("+612491578148", false),
                Arguments.of("+612491578888", false),
                Arguments.of("+612491579212", false),
                Arguments.of("+612491579760", false),
                Arguments.of("+612491579455", false),

                Arguments.of("612491570006", false),
                Arguments.of("612491570156", false),
                Arguments.of("612491570157", false),
                Arguments.of("612491570158", false),
                Arguments.of("612491570159", false),
                Arguments.of("612491570110", false),
                Arguments.of("612491570313", false),
                Arguments.of("612491570737", false),
                Arguments.of("612491571266", false),
                Arguments.of("612491571491", false),
                Arguments.of("612491571804", false),
                Arguments.of("612491572549", false),
                Arguments.of("612491572665", false),
                Arguments.of("612491572983", false),
                Arguments.of("612491573770", false),
                Arguments.of("612491573087", false),
                Arguments.of("612491574118", false),
                Arguments.of("612491574632", false),
                Arguments.of("612491575254", false),
                Arguments.of("612491575789", false),
                Arguments.of("612491576398", false),
                Arguments.of("612491576801", false),
                Arguments.of("612491577426", false),
                Arguments.of("612491577644", false),
                Arguments.of("612491578957", false),
                Arguments.of("612491578148", false),
                Arguments.of("612491578888", false),
                Arguments.of("612491579212", false),
                Arguments.of("612491579760", false),
                Arguments.of("612491579455", false),

                // 1800 numbers are not mobile numbers
                Arguments.of("+611800160401", false),
                Arguments.of("+611800975707", false),
                Arguments.of("+611800975708", false),
                Arguments.of("+611800975709", false),
                Arguments.of("+611800975710", false),
                Arguments.of("+611800975711", false),
                Arguments.of("+611300975707", false),
                Arguments.of("+611300975708", false),
                Arguments.of("+611300975709", false),
                Arguments.of("+611300975710", false),
                Arguments.of("+611300975711", false),

                // Australian landlines, local form, in different states
                Arguments.of("0255501234", false),
                Arguments.of("0270101234", false),
                Arguments.of("0355501234", false),
                Arguments.of("0370101234", false),
                Arguments.of("0755501234", false),
                Arguments.of("0770101234", false),
                Arguments.of("0855501234", false),
                Arguments.of("0870101234", false),

                // Australian landlines in international form, for different states
                Arguments.of("+61255501234", false),
                Arguments.of("+61270101234", false),
                Arguments.of("+61355501234", false),
                Arguments.of("+61370101234", false),
                Arguments.of("+61755501234", false),
                Arguments.of("+61770101234", false),
                Arguments.of("+61855501234", false),
                Arguments.of("+61870101234", false)
        );
    }
}
