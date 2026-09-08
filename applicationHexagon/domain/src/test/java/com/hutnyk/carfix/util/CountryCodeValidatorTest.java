package com.hutnyk.carfix.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CountryCodeValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "+1",
            "+44"
    })
    public void test_validateCountryCode_valid_countryCode_and_fieldName_not_null(String countryCode) {
        String fieldName = "countryCode";

        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        assertThat(result).isEqualTo(countryCode);
    }

    @Test
    public void test_validateCountryCode_valid_countryCode_and_fieldName_is_null() {
        String countryCode = "+1";
        String fieldName = null;

        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        assertThat(result).isEqualTo(countryCode);
    }

    @Test
    public void test_validateCountryCode_throws_when_countryCode_is_null() {
        String countryCode = null;
        String fieldName = "countryCode";

        assertThatThrownBy(() -> CountryCodeValidator.validateCountryCode(countryCode, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ",
            ""
    })
    public void test_validateCountryCode_throws_when_countryCode_empty_or_blank(String countryCode) {
        String fieldName = "countryCode";

        assertThatThrownBy(() -> CountryCodeValidator.validateCountryCode(countryCode, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+999",         // invalid country code
            "+0",           // invalid country code
            "+12345",       // invalid country code
            "1",            // missing plus sign
            "44",           // missing plus sign
            "+",            // only plus sign
            "+abc"          // invalid format
    })
    public void test_validateCountryCode_throws_when_countryCode_not_valid(String countryCode) {
        String fieldName = "countryCode";

        assertThatThrownBy(() -> CountryCodeValidator.validateCountryCode(countryCode, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_COUNTRY_CODE);
    }

    @Test
    public void test_validateCountryCode_trims_whitespace() {
        String countryCode = "  +1  ";
        String fieldName = "countryCode";
        String expectedTrimmed = "+1";

        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        assertThat(result).isEqualTo(expectedTrimmed);
    }
}

