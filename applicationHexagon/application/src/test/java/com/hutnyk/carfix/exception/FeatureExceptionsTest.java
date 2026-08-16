package com.hutnyk.carfix.exception;

import com.hutnyk.carfix.branch.exception.InvalidBranchRegistrationException;
import com.hutnyk.carfix.carCatalog.exception.CarBrandNotFoundException;
import com.hutnyk.carfix.carCatalog.exception.ModelVersionNotFoundException;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyTakenException;
import com.hutnyk.carfix.user.exception.PhoneNumberAlreadyTakenException;
import com.hutnyk.carfix.user.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class FeatureExceptionsTest {

    @Test
    public void test_car_profile_not_found_keeps_its_message_and_maps_to_not_found() {
        //given
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000007");
        CarProfileNotFoundException exception = new CarProfileNotFoundException(id);

        //then
        assertThat(exception.getMessage())
                .isEqualTo("Car profile not found: 00000000-0000-0000-0000-000000000007");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getErrorCode().code()).isEqualTo("CAR_PROFILE_NOT_FOUND");
    }

    @Test
    public void test_model_version_not_found_keeps_its_message_and_maps_to_not_found() {
        //given
        ModelVersionNotFoundException exception = new ModelVersionNotFoundException(42);

        //then
        assertThat(exception.getMessage()).isEqualTo("Model version not found: 42");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getErrorCode().code()).isEqualTo("MODEL_VERSION_NOT_FOUND");
    }

    @Test
    public void test_email_already_taken_reproduces_the_register_form_contract() {
        //given
        EmailAlreadyTakenException exception = new EmailAlreadyTakenException("john@example.com");

        //then
        assertThat(exception).isInstanceOf(UserAlreadyExistsException.class);
        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.details()).containsExactly(
                entry("email", "User with john@example.com email already exists"));
    }

    @Test
    public void test_phone_number_already_taken_reproduces_the_register_form_contract() {
        //given
        PhoneNumberAlreadyTakenException exception =
                new PhoneNumberAlreadyTakenException(new PhoneNumber("+48", "123456789"));

        //then
        assertThat(exception).isInstanceOf(UserAlreadyExistsException.class);
        assertThat(exception.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(exception.details()).containsExactly(
                entry("phoneCountryCodeAndPhoneNumber", "User with 123456789 phone number already exists"));
    }

    @Test
    public void test_authenticated_user_missing_maps_to_authentication() {
        //given
        AuthenticatedUserMissingException byEmail =
                AuthenticatedUserMissingException.forEmail("john@example.com");
        AuthenticatedUserMissingException noAggregate =
                AuthenticatedUserMissingException.noCustomerAggregate("john@example.com");

        //then
        assertThat(byEmail.category()).isEqualTo(ErrorCategory.AUTHENTICATION);
        assertThat(byEmail.getMessage()).isEqualTo("Authenticated user not found: john@example.com");
        assertThat(noAggregate.getMessage())
                .isEqualTo("No customer aggregate for authenticated principal: john@example.com");
    }

    @Test
    public void test_car_brand_not_found_is_a_404_with_its_own_code() {
        //when
        CarBrandNotFoundException exception = new CarBrandNotFoundException(42);

        //then
        assertThat(exception.getErrorCode().code()).isEqualTo("CAR_BRAND_NOT_FOUND");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Car brand not found: 42");
    }

    @Test
    public void test_service_category_not_found_is_a_404_with_its_own_code() {
        //when
        ServiceCategoryNotFoundException exception = new ServiceCategoryNotFoundException(7);

        //then
        assertThat(exception.getErrorCode().code()).isEqualTo("SERVICE_CATEGORY_NOT_FOUND");
        assertThat(exception.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Service category not found: 7");
    }

    @Test
    public void test_invalid_branch_registration_publishes_the_offending_path() {
        //when
        InvalidBranchRegistrationException exception =
                InvalidBranchRegistrationException.unknownReference("serviceBays[0].type", "service bay type", "Lift");

        //then
        assertThat(exception.getErrorCode().code()).isEqualTo("INVALID_BRANCH_REGISTRATION");
        assertThat(exception.category()).isEqualTo(ErrorCategory.VALIDATION);
        assertThat(exception.details()).containsExactly(entry("serviceBays[0].type", "Unknown service bay type: Lift"));
    }
}
