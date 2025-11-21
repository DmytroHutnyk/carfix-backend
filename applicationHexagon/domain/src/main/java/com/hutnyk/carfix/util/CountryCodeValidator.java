package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Validates phone country codes (ITU-T E.164 format, e.g., "+1", "+44").
 *
 * <p>Loads country codes from a resource file.
 * The resource file can be updated without code changes and the implementation.
 */

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


    /**
     * Validates that the provided country code is a valid phone country code.
     *
     * @param countryCode the country code to validate (e.g., "+1", "+44")
     * @param fieldName the name of the field being validated (for error messages)
     * @throws DomainObjectValidationException if the country code is invalid
     * @return trimmed input string if validation succeeded
     */
    public static String validateCountryCode(String countryCode, String fieldName) {
        Validator.notBlank(countryCode, fieldName);

        String trimmedCountryCode = countryCode.trim();

        if (!ALLOWED_COUNTRY_CODES.contains(trimmedCountryCode)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_COUNTRY_CODE, fieldName, countryCode);
        }
        return trimmedCountryCode;
    }
}
