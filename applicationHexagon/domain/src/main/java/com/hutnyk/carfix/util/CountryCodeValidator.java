package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//TODO use Google's libphonenumber library
public class CountryCodeValidator {

    private static final String COUNTRY_CODES_RESOURCE = "/country-codes.txt";
    private static final Set<String> ALLOWED_COUNTRY_CODES = loadCountryCodes();

    private static Set<String> loadCountryCodes(){
        Set<String> result = new HashSet<>();

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                CountryCodeValidator.class.getResourceAsStream(COUNTRY_CODES_RESOURCE),
                                "Resource not found: " + COUNTRY_CODES_RESOURCE
                        )
                )
        )){
            String line;

            while ((line = reader.readLine()) != null){
                if(line.startsWith("#") || line.isBlank()){
                    continue;
                }
                result.add(line.trim());
            }
        }catch (IOException e){
            throw new RuntimeException("Failed to load country codes" + e.getMessage());
        }
        return result;
    }
    public static String validateCountryCode(String countryCode, String fieldName) {
        Validator.notBlank(countryCode, fieldName);

        String trimmedCountryCode = countryCode.trim();

        if (!ALLOWED_COUNTRY_CODES.contains(trimmedCountryCode)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_COUNTRY_CODE, fieldName, countryCode);
        }
        return trimmedCountryCode;
    }
}
