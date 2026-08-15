package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CountryIsoTest {

    @Test
    public void test_parse_maps_an_iso_code_to_the_constant() {
        //when + then
        assertThat(CountryIso.parse("PL")).isEqualTo(CountryIso.PL);
        assertThat(CountryIso.parse(" de ")).isEqualTo(CountryIso.DE);
    }

    @Test
    public void test_parse_rejects_an_unsupported_country_with_a_readable_message() {
        //when + then
        assertThatThrownBy(() -> CountryIso.parse("XX"))
                .isInstanceOf(DomainObjectValidationException.class)
                .hasMessage("Country XX is not supported")
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.INVALID_ISO_CODE, "countryIso");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    public void test_parse_rejects_blank(String code) {
        //when + then
        assertThatThrownBy(() -> CountryIso.parse(code))
                .isInstanceOf(DomainObjectValidationException.class);
    }
}
