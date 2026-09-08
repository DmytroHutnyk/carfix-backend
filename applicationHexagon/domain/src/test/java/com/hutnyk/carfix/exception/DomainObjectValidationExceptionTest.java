package com.hutnyk.carfix.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DomainObjectValidationExceptionTest {

    @Test
    public void test_it_is_a_car_fix_validation_exception() {
        DomainObjectValidationException exception =
                new DomainObjectValidationException(ValidationErrorType.INVALID_VIN_FORMAT, "vin", "ABC");

        assertThat(exception).isInstanceOf(ValidationException.class);
        assertThat(exception).isInstanceOf(CarFixException.class);
        assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        assertThat(exception.getErrorCode()).isEqualTo(ValidationErrorType.INVALID_VIN_FORMAT);
        assertThat(exception.getErrorType()).isEqualTo(ValidationErrorType.INVALID_VIN_FORMAT);
    }

    @Test
    public void test_the_three_argument_message_is_unchanged() {
        DomainObjectValidationException exception =
                new DomainObjectValidationException(ValidationErrorType.INVALID_VIN_FORMAT, "vin", "ABC");

        assertThat(exception.getMessage()).isEqualTo("vin: VIN format is not valid (received: ABC)");
        assertThat(exception.getFieldName()).isEqualTo("vin");
        assertThat(exception.getRejectedValue()).isEqualTo("ABC");
    }

    @Test
    public void test_the_two_argument_message_is_unchanged() {
        DomainObjectValidationException exception =
                new DomainObjectValidationException(ValidationErrorType.NULL_VALUE, "date");

        assertThat(exception.getMessage()).isEqualTo("date: Value cannot be null");
        assertThat(exception.getRejectedValue()).isNull();
    }

    @Test
    public void test_the_custom_message_constructor_is_unchanged() {
        DomainObjectValidationException exception = new DomainObjectValidationException(
                ValidationErrorType.VALUE_OUT_OF_RANGE, "price", -1, "price must be positive");

        assertThat(exception.getMessage()).isEqualTo("price must be positive");
    }

    @Test
    public void test_it_publishes_the_failing_field_as_details() {
        DomainObjectValidationException exception =
                new DomainObjectValidationException(ValidationErrorType.INVALID_VIN_FORMAT, "vin", "ABC");

        assertThat(exception.details())
                .containsExactly(entry("vin", "vin: VIN format is not valid (received: ABC)"));
    }

    @Test
    public void test_every_validation_error_type_is_a_validation_category_code() {
        for (ValidationErrorType type : ValidationErrorType.values()) {
            assertThat(type.category()).as(type.name()).isEqualTo(ErrorCategory.VALIDATION);
            assertThat(type.code()).as(type.name()).isEqualTo(type.getCode());
        }
    }
}
