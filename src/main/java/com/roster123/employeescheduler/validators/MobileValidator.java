package com.roster123.employeescheduler.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.opencsv.bean.BeanField;
import com.opencsv.bean.validators.StringValidator;
import com.opencsv.exceptions.CsvValidationException;

/**
 * validator for mobile numbers, for use with opencsv.
 * 
 * Mobile numbers are needed so can SMS employees (landline cannot do this).
 * 
 * We require mobile numbers to be in international format, otherwise cannot determine if mobile: https://github.com/google/libphonenumber/blob/master/FAQ.md
 */
public class MobileValidator implements StringValidator{
    // regex from https://stackoverflow.com/a/22378975
    // escaped on https://www.freeformatter.com/java-dotnet-escape.html
    public static final String mobileRegex = "^(\\+\\d{1,3}[- ]?)?\\d{10}$";

    @Override
    public boolean isValid(String mobile) {
        // mobile can be empty when read in (using opencsv validator), but has to be converted to null when apply opencsv processor after (thus allow empty string here)
        return (mobile==null)||"".equals(mobile)||validMobileString(mobile);
    }

    /**
     * checks validity. The stricter version - doesn't allow empty or null
     * @param mobile
     * @return
     */
    public static boolean validMobileString(String mobile){
        // https://github.com/google/libphonenumber/blob/master/java/libphonenumber/src/com/google/i18n/phonenumbers/PhoneNumberUtil.java#L2318
        // https://www.baeldung.com/java-libphonenumber
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try{
            PhoneNumber phone = phoneNumberUtil.parse(mobile, CountryCodeSource.UNSPECIFIED.name());
            
            return phoneNumberUtil.isValidNumber(phone)&&(phoneNumberUtil.getNumberType(phone)==PhoneNumberType.MOBILE);
        }
        catch (NumberParseException e){
            return false;
        }
    }

    @Override
    public void setParameterString(String param) {
        // does nothing - no parameters needed
    }

    @Override
    public void validate(String mobile, BeanField field) throws CsvValidationException {
        if (!isValid(mobile)){
            throw new CsvValidationException(field+" is not a valid mobile: "+mobile);
        }
    }
}
