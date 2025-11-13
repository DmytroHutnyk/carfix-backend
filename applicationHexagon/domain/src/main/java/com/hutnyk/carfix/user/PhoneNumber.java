package com.hutnyk.carfix.user;

import com.hutnyk.carfix.util.CountryCodeValidator;
import com.hutnyk.carfix.util.Validator;

public record PhoneNumber(String countryCode, String phoneNumber) {
    public PhoneNumber{
        CountryCodeValidator.validateCountryCode(countryCode, "countryCode");
        Validator.validatePhoneNumber(phoneNumber, "phoneNumber");
    }
}
