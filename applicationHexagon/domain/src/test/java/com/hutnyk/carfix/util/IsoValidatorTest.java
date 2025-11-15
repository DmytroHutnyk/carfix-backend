package com.hutnyk.carfix.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class IsoValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "US",
            "GB",
            "CA",
            "DE",
            "FR"
    })
    public void test_validateCountryIso_valid_iso_and_fieldName_not_null(String iso) {
        //given
        String fieldName = "countryIso";

        //when
        String result = IsoValidator.validateCountryIso(iso, fieldName);

        //then
        assertThat(result).isEqualTo(iso);
    }

    @Test
    public void test_validateCountryIso_valid_iso_and_fieldName_is_null() {
        //given
        String iso = "US";
        String fieldName = null;

        //when
        String result = IsoValidator.validateCountryIso(iso, fieldName);

        //then
        assertThat(result).isEqualTo(iso);
    }

    @Test
    public void test_validateCountryIso_throws_when_iso_is_null() {
        //given
        String iso = null;
        String fieldName = "countryIso";

        //when + then
        assertThatThrownBy(() -> IsoValidator.validateCountryIso(iso, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ",
            ""
    })
    public void test_validateCountryIso_throws_when_iso_empty_or_blank(String iso) {
        //given
        String fieldName = "countryIso";

        //when + then
        assertThatThrownBy(() -> IsoValidator.validateCountryIso(iso, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "A",           // too short
            "ABC",         // too long
    })
    public void test_validateCountryIso_throws_when_iso_wrong_length(String iso) {
        //given
        String fieldName = "countryIso";

        //when + then
        assertThatThrownBy(() -> IsoValidator.validateCountryIso(iso, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_ISO_CODE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XX",          // invalid ISO code
            "ZZ",          // invalid ISO code
            "QQ"           // invalid ISO code
    })
    public void test_validateCountryIso_throws_when_iso_not_in_valid_set(String iso) {
        //given
        String fieldName = "countryIso";

        //when + then
        assertThatThrownBy(() -> IsoValidator.validateCountryIso(iso, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_ISO_CODE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "us",
            "gb",
            "ca",
            "de",
            "fr"
    })
    public void test_validateCountryIso_normalizes_lowercase_to_uppercase(String iso) {
        //given
        String fieldName = "countryIso";
        String expectedUppercase = iso.toUpperCase();

        //when
        String result = IsoValidator.validateCountryIso(iso, fieldName);

        //then
        assertThat(result).isEqualTo(expectedUppercase);
    }

    @Test
    public void test_validateCountryIso_trims_whitespace() {
        //given
        String iso = "  US  ";
        String fieldName = "countryIso";
        String expectedTrimmed = "US";

        //when
        String result = IsoValidator.validateCountryIso(iso, fieldName);

        //then
        assertThat(result).isEqualTo(expectedTrimmed);
    }

    @Test
    public void test_validateCountryIso_normalizes_and_trims_mixed_case_with_whitespace() {
        //given
        String iso = "  us  ";
        String fieldName = "countryIso";
        String expected = "US";

        //when
        String result = IsoValidator.validateCountryIso(iso, fieldName);

        //then
        assertThat(result).isEqualTo(expected);
    }
}
