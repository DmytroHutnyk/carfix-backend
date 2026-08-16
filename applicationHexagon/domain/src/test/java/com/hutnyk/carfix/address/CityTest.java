package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CityTest {

    private static final BigDecimal WARSAW_LAT = new BigDecimal("52.229700");
    private static final BigDecimal WARSAW_LNG = new BigDecimal("21.012200");

    @Test
    public void test_of_carries_coordinates() {
        //when
        City result = City.of(11, "Warsaw", 3, WARSAW_LAT, WARSAW_LNG);

        //then
        assertThat(result.getId()).isEqualTo(11);
        assertThat(result.getName()).isEqualTo("Warsaw");
        assertThat(result.getRegionId()).isEqualTo(3);
        assertThat(result.getLatitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(result.getLongitude()).isEqualByComparingTo(WARSAW_LNG);
        assertThat(result.hasCoordinates()).isTrue();
    }

    @Test
    public void test_of_allows_missing_coordinates() {
        //when
        City result = City.of(null, "Warsaw", 3, null, null);

        //then
        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
        assertThat(result.hasCoordinates()).isFalse();
    }

    @Test
    public void test_boundary_coordinates_are_accepted() {
        //when
        City result = City.of(null, "Edge", 3, new BigDecimal("90"), new BigDecimal("-180"));

        //then
        assertThat(result.getLatitude()).isEqualByComparingTo("90");
        assertThat(result.getLongitude()).isEqualByComparingTo("-180");
    }

    @Test
    public void test_latitude_without_longitude_is_rejected() {
        //when + then
        assertThatThrownBy(() -> City.of(null, "Warsaw", 3, WARSAW_LAT, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "longitude");
    }

    @Test
    public void test_longitude_without_latitude_is_rejected() {
        //when + then
        assertThatThrownBy(() -> City.of(null, "Warsaw", 3, null, WARSAW_LNG))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "latitude");
    }

    @Test
    public void test_latitude_out_of_range_is_rejected() {
        //when + then
        assertThatThrownBy(() -> City.of(null, "Warsaw", 3, new BigDecimal("90.000001"), WARSAW_LNG))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.VALUE_OUT_OF_RANGE, "latitude");
    }

    @Test
    public void test_longitude_out_of_range_is_rejected() {
        //when + then
        assertThatThrownBy(() -> City.of(null, "Warsaw", 3, WARSAW_LAT, new BigDecimal("-180.5")))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.VALUE_OUT_OF_RANGE, "longitude");
    }

    @Test
    public void test_blank_name_is_rejected() {
        //when + then
        assertThatThrownBy(() -> City.of(null, " ", 3, null, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }
}
