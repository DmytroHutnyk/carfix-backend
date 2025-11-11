package com.hutnyk.carfix.user;

import com.hutnyk.carfix.InvalidDomainObjectError;
import com.hutnyk.carfix.util.CountryCodeValidator;
import com.hutnyk.carfix.util.Validator;

public record PhoneNumber(String countryCode, String phoneNumber) {
    public PhoneNumber{
        CountryCodeValidator.validateCountryCode(countryCode);
        validatePhoneNumber(phoneNumber);
    }

    private static void validatePhoneNumber(String phoneNumber){
        Validator.notEmpty(phoneNumber);

        if(!phoneNumber.matches("^[0-9]{5,15}$")){
            throw new InvalidDomainObjectError("Invalid phone number");
        }
    }
}
