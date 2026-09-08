package com.hutnyk.carfix.exception;

import com.hutnyk.carfix.branch.exception.InvalidBranchRegistrationException;
import com.hutnyk.carfix.carCatalog.exception.CarBrandNotFoundException;
import com.hutnyk.carfix.carCatalog.exception.ModelVersionNotFoundException;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyTakenException;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.user.exception.PhoneNumberAlreadyTakenException;
import com.hutnyk.carfix.user.exception.UserAlreadyExistsException;
import com.hutnyk.carfix.user.exception.VerificationCodeAttemptsExceededException;
import com.hutnyk.carfix.user.exception.VerificationCodeExpiredException;
import com.hutnyk.carfix.user.exception.VerificationCodeInvalidException;
import com.hutnyk.carfix.user.exception.VerificationCodeNotFoundException;
import com.hutnyk.carfix.user.exception.VerificationCodeResendTooSoonException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class FeatureExceptionsTest {

    @Test
    public void test_car_profile_not_found_keeps_its_message_and_maps_to_not_found() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000007");
        CarProfileNotFoundException exception = new CarProfileNotFoundException(id);

        assertThat(exception.getMessage())
                .isEqualTo("Car profile not found: 00000000-0000-0000-0000-000000000007");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getErrorCode().code()).isEqualTo("CAR_PROFILE_NOT_FOUND");
    }

    @Test
    public void test_model_version_not_found_keeps_its_message_and_maps_to_not_found() {
        ModelVersionNotFoundException exception = new ModelVersionNotFoundException(42);

        assertThat(exception.getMessage()).isEqualTo("Model version not found: 42");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getErrorCode().code()).isEqualTo("MODEL_VERSION_NOT_FOUND");
    }

    @Test
    public void test_email_already_taken_reproduces_the_register_form_contract() {
        EmailAlreadyTakenException exception = new EmailAlreadyTakenException("john@example.com");

        assertThat(exception).isInstanceOf(UserAlreadyExistsException.class);
        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.details()).containsExactly(
                entry("email", "User with john@example.com email already exists"));
    }

    @Test
    public void test_phone_number_already_taken_reproduces_the_register_form_contract() {
        PhoneNumberAlreadyTakenException exception =
                new PhoneNumberAlreadyTakenException(new PhoneNumber("+48", "123456789"));

        assertThat(exception).isInstanceOf(UserAlreadyExistsException.class);
        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.details()).containsExactly(
                entry("phoneCountryCodeAndPhoneNumber", "User with 123456789 phone number already exists"));
    }

    @Test
    public void test_authenticated_user_missing_maps_to_authentication() {
        AuthenticatedUserMissingException byEmail =
                AuthenticatedUserMissingException.forEmail("john@example.com");
        AuthenticatedUserMissingException noAggregate =
                AuthenticatedUserMissingException.noCustomerAggregate("john@example.com");

        assertThat(byEmail.category()).isEqualTo(ErrorCategory.AUTHENTICATION);
        assertThat(byEmail.getMessage()).isEqualTo("Authenticated user not found: john@example.com");
        assertThat(noAggregate.getMessage())
                .isEqualTo("No customer aggregate for authenticated principal: john@example.com");
    }

    @Test
    public void test_car_brand_not_found_is_a_404_with_its_own_code() {
        CarBrandNotFoundException exception = new CarBrandNotFoundException(42);

        assertThat(exception.getErrorCode().code()).isEqualTo("CAR_BRAND_NOT_FOUND");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Car brand not found: 42");
    }

    @Test
    public void test_service_category_not_found_is_a_404_with_its_own_code() {
        ServiceCategoryNotFoundException exception = new ServiceCategoryNotFoundException(7);

        assertThat(exception.getErrorCode().code()).isEqualTo("SERVICE_CATEGORY_NOT_FOUND");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Service category not found: 7");
    }

    @Test
    public void test_invalid_branch_registration_publishes_the_offending_path() {
        InvalidBranchRegistrationException exception =
                InvalidBranchRegistrationException.unknownReference("serviceBays[0].type", "service bay type", "Lift");

        assertThat(exception.getErrorCode().code()).isEqualTo("INVALID_BRANCH_REGISTRATION");
        assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        assertThat(exception.details()).containsExactly(entry("serviceBays[0].type", "Unknown service bay type: Lift"));
    }

    @Test
    public void test_email_already_verified_maps_to_conflict_without_field_details() {
        EmailAlreadyVerifiedException exception = new EmailAlreadyVerifiedException("john@example.com");

        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.getErrorCode().code()).isEqualTo("EMAIL_ALREADY_VERIFIED");
        assertThat(exception.getMessage()).isEqualTo("Email john@example.com is already verified");
        assertThat(exception.details()).isEmpty();
    }

    @Test
    public void test_verification_code_invalid_reports_the_code_field_and_attempts_left() {
        VerificationCodeInvalidException exception = new VerificationCodeInvalidException(3);

        assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        assertThat(exception.getErrorCode().code()).isEqualTo("VERIFICATION_CODE_INVALID");
        assertThat(exception.details()).containsExactly(entry("code", "Incorrect verification code. 3 attempt(s) left"));
    }

    @Test
    public void test_verification_code_state_failures_map_to_their_categories() {
        assertThat(new VerificationCodeNotFoundException().category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(new VerificationCodeNotFoundException().getMessage()).isEqualTo("No active verification code. Request a new one");
        assertThat(new VerificationCodeExpiredException().category()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(new VerificationCodeExpiredException().getMessage()).isEqualTo("The verification code has expired. Request a new one");
        assertThat(new VerificationCodeAttemptsExceededException().category()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(new VerificationCodeAttemptsExceededException().getMessage()).isEqualTo("Too many incorrect attempts. Request a new code");
        assertThat(new VerificationCodeResendTooSoonException(42).category()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(new VerificationCodeResendTooSoonException(42).getMessage())
                .isEqualTo("A verification code was sent recently. You can request a new one in 42 seconds");
    }
}
