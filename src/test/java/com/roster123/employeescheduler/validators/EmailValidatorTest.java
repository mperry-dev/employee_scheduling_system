package com.roster123.employeescheduler.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import com.opencsv.exceptions.CsvValidationException;

import org.junit.jupiter.api.BeforeEach;

import com.roster123.employeescheduler.loaders.DateTimeCsvConverter;

public class EmailValidatorTest {
    private EmailValidator validator;

    @BeforeEach
    public void before(){
        validator = new EmailValidator();
    }

    @Test
    public void testSetParameterString(){
        validator.setParameterString("");
    }

    @ParameterizedTest
    @MethodSource("emailValidityParameters")
    public void testIsValid(String email, boolean isValid){
        if (isValid != validator.isValid(email)){
            System.out.println(email+" "+isValid);
        }
        assertEquals(isValid, validator.isValid(email));
    }

    @ParameterizedTest
    @MethodSource("emailValidityParameters")
    public void testValid(String email, boolean isValid){
        if (isValid){
            assertTrue(validator.isValid(email));
        }
        else{
            assertThrows(CsvValidationException.class, ()->{validator.validate(email, new DateTimeCsvConverter());});
        }
    }

    private static Stream<Arguments> emailValidityParameters() {
        return Stream.of(
                Arguments.of("abc@gmail.com", true),
                Arguments.of("abcgmail.com", false),
                Arguments.of("", false),
                
                // some examples from https://gist.github.com/cjaoude/fd9910626629b53c4d25
                Arguments.of("email@example.com", true),
                Arguments.of("firstname.lastname@example.com", true),
                Arguments.of("email@subdomain.example.com", true),
                Arguments.of("firstname+lastname@example.com", true),
                Arguments.of("email@123.123.123.123", true),
                Arguments.of("email@[123.123.123.123]", true),
                Arguments.of("\"email\"@example.com", true),
                Arguments.of("1234567890@example.com", true),
                Arguments.of("email@example-one.com", true),
                Arguments.of("_______@example.com", true),
                Arguments.of("email@example.name", true),
                Arguments.of("email@example.museum", true),
                Arguments.of("email@example.co.jp", true),
                Arguments.of("firstname-lastname@example.com", true),

                Arguments.of("much.”more\\ unusual”@example.com", true),
                Arguments.of("very.unusual.”@”.unusual.com@example.com", true),
                Arguments.of("very.”(),:;<>[]”.VERY.”very@\\ \"very”.unusual@strange.example.com", true),

                Arguments.of("plainaddress", false),
                Arguments.of("#@%^%#$@#$@#.com", false),
                Arguments.of("@example.com", false),
                Arguments.of("Joe Smith <email@example.com>", false),
                Arguments.of("email.example.com", false),
                Arguments.of("email@example@example.com", false),
                Arguments.of(".email@example.com", false),
                Arguments.of("email.@example.com", false),
                Arguments.of("email..email@example.com", false),
                Arguments.of("あいうえお@example.com", false),
                Arguments.of("email@example.com (Joe Smith)", false),
                Arguments.of("email@example", false),
                Arguments.of("email@-example.com", false),
                Arguments.of("email@example.web", false),
                Arguments.of("email@111.222.333.44444", false),
                Arguments.of("email@example..com", false),
                Arguments.of("Abc..123@example.com", false),

                Arguments.of("”(),:;<>[\\]@example.com", false),
                Arguments.of("just”not”right@example.com", false),
                Arguments.of("this\\ is\"really\"not\\allowed@example.com", false)
        );
    }
}
