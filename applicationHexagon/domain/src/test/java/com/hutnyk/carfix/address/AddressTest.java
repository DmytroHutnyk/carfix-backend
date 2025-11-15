package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AddressTest {

    private Address createAddressWithFlatNumber(String flatNumber) {
        return Address.of(
                1,
                "10",
                flatNumber,
                1L,
                1
        );
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

