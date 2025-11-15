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
        //given
        String fieldName = "countryCode";

        //when
        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        //then
        assertThat(result).isEqualTo(countryCode);
    }

    @Test
    public void test_validateCountryCode_valid_countryCode_and_fieldName_is_null() {
        //given
        String countryCode = "+1";
        String fieldName = null;

        //when
        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        //then
        assertThat(result).isEqualTo(countryCode);
    }

    @Test
    public void test_validateCountryCode_throws_when_countryCode_is_null() {
        //given
        String countryCode = null;
        String fieldName = "countryCode";

        //when + then
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
        //given
        String fieldName = "countryCode";

        //when + then
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
        //given
        String fieldName = "countryCode";

        //when + then
        assertThatThrownBy(() -> CountryCodeValidator.validateCountryCode(countryCode, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_COUNTRY_CODE);
    }

    @Test
    public void test_validateCountryCode_trims_whitespace() {
        //given
        String countryCode = "  +1  ";
        String fieldName = "countryCode";
        String expectedTrimmed = "+1";

        //when
        String result = CountryCodeValidator.validateCountryCode(countryCode, fieldName);

        //then
        assertThat(result).isEqualTo(expectedTrimmed);
    }
}

