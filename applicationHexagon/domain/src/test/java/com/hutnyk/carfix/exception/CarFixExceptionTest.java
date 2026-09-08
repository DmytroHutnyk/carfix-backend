package com.hutnyk.carfix.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarFixExceptionTest {

    private static final class TestException extends CarFixException {
        TestException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }

    @Test
    public void test_exception_exposes_its_code_message_and_category() {
        TestException exception = new TestException(CoreErrorCode.NOT_FOUND, "nothing here");

        assertThat(exception.getErrorCode()).isEqualTo(CoreErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("nothing here");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
    }

    @Test
    public void test_details_are_empty_unless_a_subtype_overrides_them() {
        TestException exception = new TestException(CoreErrorCode.INTERNAL_ERROR, "boom");

        assertThat(exception.details()).isEmpty();
    }

    @Test
    public void test_a_null_error_code_is_rejected_at_construction() {
        assertThatThrownBy(() -> new TestException(null, "boom"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("errorCode");
    }

    @Test
    public void test_every_core_code_reports_a_category_and_a_non_blank_code() {
        for (CoreErrorCode code : CoreErrorCode.values()) {
            assertThat(code.category()).as(code.name()).isNotNull();
            assertThat(code.code()).as(code.name()).isNotBlank();
        }
    }

    @Test
    public void test_it_is_an_unchecked_exception() {
        assertThat(RuntimeException.class)
                .isAssignableFrom(CarFixException.class);
    }
}
