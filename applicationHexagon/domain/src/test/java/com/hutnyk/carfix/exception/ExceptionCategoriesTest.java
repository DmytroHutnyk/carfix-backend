package com.hutnyk.carfix.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ExceptionCategoriesTest {

    private static final class TestValidation extends ValidationException {
        TestValidation(String message, String fieldName, Object rejectedValue) {
            super(CoreErrorCode.VALIDATION_FAILED, message, fieldName, rejectedValue);
        }
    }

    private static final class TestNotFound extends NotFoundException {
        TestNotFound(String resourceType, Object resourceId) {
            super(CoreErrorCode.NOT_FOUND, resourceType, resourceId);
        }
    }

    private static final class TestConflict extends ConflictException {
        TestConflict(String message, String fieldName, Object rejectedValue) {
            super(CoreErrorCode.CONFLICT, message, fieldName, rejectedValue);
        }
    }

    @Test
    public void test_validation_exception_publishes_the_failing_field() {
        TestValidation exception = new TestValidation("vin: bad format", "vin", "ABC");

        assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        assertThat(exception.getFieldName()).isEqualTo("vin");
        assertThat(exception.getRejectedValue()).isEqualTo("ABC");
        assertThat(exception.details()).containsExactly(entry("vin", "vin: bad format"));
    }

    @Test
    public void test_validation_exception_without_a_field_publishes_no_details() {
        TestValidation exception = new TestValidation("something failed", null, null);

        assertThat(exception.details()).isEmpty();
    }

    @Test
    public void test_not_found_builds_its_message_from_resource_type_and_id() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000007");
        TestNotFound exception = new TestNotFound("Car profile", id);

        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getMessage())
                .isEqualTo("Car profile not found: 00000000-0000-0000-0000-000000000007");
        assertThat(exception.getResourceType()).isEqualTo("Car profile");
        assertThat(exception.getResourceId()).isEqualTo(id);
        assertThat(exception.details()).isEmpty();
    }

    @Test
    public void test_not_found_omits_the_id_when_there_is_none() {
        TestNotFound exception = new TestNotFound("Car profile", null);

        assertThat(exception.getMessage()).isEqualTo("Car profile not found");
    }

    @Test
    public void test_conflict_publishes_the_colliding_field() {
        TestConflict exception = new TestConflict("User with a@b.c email already exists", "email", "a@b.c");

        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.details())
                .containsExactly(entry("email", "User with a@b.c email already exists"));
    }

    @Test
    public void test_unexpected_state_is_concrete_and_internal() {
        UnexpectedStateException exception = new UnexpectedStateException("invariant broken");

        assertThat(exception.category()).isEqualTo(ErrorCategory.INTERNAL);
        assertThat(exception.getErrorCode()).isEqualTo(CoreErrorCode.INTERNAL_ERROR);
        assertThat(exception.getMessage()).isEqualTo("invariant broken");
    }

    @Test
    public void test_unexpected_state_keeps_its_cause() {
        Throwable cause = new IllegalArgumentException("root");
        UnexpectedStateException exception = new UnexpectedStateException("wrapped", cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }
}
