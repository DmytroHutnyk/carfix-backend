package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CountryIsoTest {

    @Test
    public void test_parse_maps_an_iso_code_to_the_constant() {
        assertThat(CountryIso.parse("PL")).isEqualTo(CountryIso.PL);
        assertThat(CountryIso.parse(" de ")).isEqualTo(CountryIso.DE);
    }

    @Test
    public void test_parse_rejects_an_unsupported_country_with_a_readable_message() {
        assertThatThrownBy(() -> CountryIso.parse("XX"))
                .isInstanceOf(DomainObjectValidationException.class)
                .hasMessage("Country XX is not supported")
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.INVALID_ISO_CODE, "countryIso");
    }

    @Test
    public void test_parse_rejects_null_as_null_value() {
        assertThatThrownBy(() -> CountryIso.parse(null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "countryIso");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    public void test_parse_rejects_blank_as_empty_string(String code) {
        assertThatThrownBy(() -> CountryIso.parse(code))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.EMPTY_STRING, "countryIso");
    }
}
