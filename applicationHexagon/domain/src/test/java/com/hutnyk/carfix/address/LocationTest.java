package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

public class LocationTest {

    private static final BigDecimal WARSAW_LAT = new BigDecimal("52.229700");
    private static final BigDecimal WARSAW_LNG = new BigDecimal("21.012200");

    @Test
    public void test_full_location_is_carried() {
        //when
        Location result = new Location("Warsaw", "Masovian Voivodeship", CountryIso.PL, WARSAW_LAT, WARSAW_LNG);

        //then
        assertThat(result.city()).isEqualTo("Warsaw");
        assertThat(result.region()).isEqualTo("Masovian Voivodeship");
        assertThat(result.countryIso()).isEqualTo(CountryIso.PL);
        assertThat(result.latitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(result.longitude()).isEqualByComparingTo(WARSAW_LNG);
    }

    @Test
    public void test_country_only_location_is_allowed() {
        //when
        Location result = new Location(null, null, CountryIso.DE, null, null);

        //then
        assertThat(result.city()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.latitude()).isNull();
        assertThat(result.longitude()).isNull();
    }

    @Test
    public void test_null_country_is_rejected() {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", null, null, null, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void test_blank_city_is_rejected(String city) {
        //when + then
        assertThatThrownBy(() -> new Location(city, "Masovian Voivodeship", CountryIso.PL, null, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void test_blank_region_is_rejected(String region) {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", region, CountryIso.PL, null, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_latitude_without_longitude_is_rejected() {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", null, CountryIso.PL, WARSAW_LAT, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "longitude");
    }

    @Test
    public void test_longitude_without_latitude_is_rejected() {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", null, CountryIso.PL, null, WARSAW_LNG))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "latitude");
    }

    @Test
    public void test_latitude_out_of_range_is_rejected() {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", null, CountryIso.PL, new BigDecimal("90.000001"), WARSAW_LNG))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.VALUE_OUT_OF_RANGE, "latitude");
    }

    @Test
    public void test_longitude_out_of_range_is_rejected() {
        //when + then
        assertThatThrownBy(() -> new Location("Warsaw", null, CountryIso.PL, WARSAW_LAT, new BigDecimal("-180.5")))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.VALUE_OUT_OF_RANGE, "longitude");
    }
}
