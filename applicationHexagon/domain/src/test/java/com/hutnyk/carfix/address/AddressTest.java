package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AddressTest {

    private Address createAddressWithFlatNumber(String flatNumber) {
        return Address.of(
                1,
                "Main Street",
                "10",
                flatNumber,
                "00-001",
                1,
                null,
                null,
                null
        );
    }

    @Test
    public void test_of_carries_geo_fields() {
        //when
        Address result = Address.of(
                1, "Marszałkowska", "12", "3A", "00-001", 5,
                new BigDecimal("52.229676"), new BigDecimal("21.012229"), "ChIJAZ_place_id");

        //then
        assertThat(result.getLatitude()).isEqualByComparingTo("52.229676");
        assertThat(result.getLongitude()).isEqualByComparingTo("21.012229");
        assertThat(result.getGooglePlaceId()).isEqualTo("ChIJAZ_place_id");
    }

    @Test
    public void test_of_allows_null_geo_fields() {
        //when
        Address result = Address.of(1, "Marszałkowska", "12", null, "00-001", 5, null, null, null);

        //then
        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
        assertThat(result.getGooglePlaceId()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "10",
            "100",
            "1A",
            "10B",
            "A1",
            "B10",
            "1-2",
            "10/2",
            "Apt 1",
            "Flat 10"
    })
    public void test_validateFlatNumber_valid_flatNumber_and_fieldName_not_null(String flatNumber) {
        //when
        Address result = createAddressWithFlatNumber(flatNumber);

        //then
        assertThat(result.getFlatNumber()).isEqualTo(flatNumber);
    }

    @Test
    public void test_validateFlatNumber_returns_null_when_flatNumber_is_null() {
        //when
        Address result = createAddressWithFlatNumber(null);

        //then
        assertThat(result.getFlatNumber()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ",
            ""
    })
    public void test_validateFlatNumber_throws_when_flatNumber_is_blank(String flatNumber) {
        //when + then
        assertThatThrownBy(() -> createAddressWithFlatNumber(flatNumber))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_FLAT_NUMBER);
    }
}

