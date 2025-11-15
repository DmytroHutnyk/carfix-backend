package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates ISO 3166-1 alpha-2 country codes using Java's built-in Locale support.
 * This ensures validation against the complete ISO standard.
 */
public class IsoValidator {
    
    /**
     * Cached set of valid ISO country codes for performance.
     * Loaded once using Java's built-in Locale.getISOCountries().
     */
    private static final Set<String> VALID_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toUnmodifiableSet());
    
    /**
     * Validates that the provided ISO code is a valid ISO country code.
     * Uses Java's Locale.getISOCountries() which provides all valid ISO country codes.
     *
     * @param iso the ISO country code to validate (will be normalized to uppercase)
     * @param fieldName the name of the field being validated (for error messages)
     * @return the validated ISO code (normalized to uppercase and trimmed)
     * @throws DomainObjectValidationException if the ISO code is invalid
     */
    public static String validateCountryIso(String iso, String fieldName) {
        Validator.notBlank(iso, fieldName);
        
        String normalizedIso = iso.toUpperCase().trim();
        
        if (normalizedIso.length() != 2) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_ISO_CODE, fieldName, iso);
        }
        

        if (!VALID_COUNTRY_CODES.contains(normalizedIso)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_ISO_CODE, fieldName, iso);
        }
        
        return normalizedIso;
    }
}
