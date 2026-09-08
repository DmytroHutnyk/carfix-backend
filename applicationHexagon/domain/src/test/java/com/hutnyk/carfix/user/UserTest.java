package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

public class UserTest {

    private User createUserWithBirthDate(LocalDate dateOfBirth) {
        return User.of(
                UserId.genId(),
                "John",
                "Doe",
                new PhoneNumber("+1", "1234567890"),
                "john.doe@example.com",
                UserRole.CUSTOMER,
                PasswordHash.of("hashedPassword123"),
                dateOfBirth,
                null,
                null,
                null
        );
    }

    private User createUserWithPreferredCity(Integer preferredCityId) {
        return User.of(
                UserId.genId(),
                "John",
                "Doe",
                new PhoneNumber("+1", "1234567890"),
                "john.doe@example.com",
                UserRole.CUSTOMER,
                PasswordHash.of("hashedPassword123"),
                LocalDate.of(1990, 5, 1),
                null,
                preferredCityId,
                null
        );
    }

    @Test
    public void test_validateBirthDate_valid_date_and_fieldName_not_null() {
        LocalDate birthDate = LocalDate.now().minusYears(30);
        User result = createUserWithBirthDate(birthDate);

        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_valid_date_exactly_1900_01_01() {
        LocalDate birthDate = LocalDate.of(1900, 1, 1);
        User result = createUserWithBirthDate(birthDate);

        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_valid_date_today() {
        LocalDate birthDate = LocalDate.now();
        User result = createUserWithBirthDate(birthDate);

        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_returns_null_when_dateOfBirth_is_null() {
        User result = createUserWithBirthDate(null);

        assertThat(result.getDateOfBirth()).isNull();
    }

    @Test
    public void test_validateBirthDate_throws_when_date_in_future() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> createUserWithBirthDate(futureDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_IN_FUTURE);
    }

    @Test
    public void test_validateBirthDate_throws_when_date_before_1900_01_01() {
        LocalDate oldDate = LocalDate.of(1899, 5, 5);
        assertThatThrownBy(() -> createUserWithBirthDate(oldDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_OLD);
    }

    @Test
    public void test_of_carries_the_preferred_city_id() {
        User result = User.of(UserId.genId(), "John", "Doe", new PhoneNumber("+1", "1234567890"),
                "john.doe@example.com", UserRole.CUSTOMER, PasswordHash.of("hashedPassword123"),
                null, null, 11, null);

        assertThat(result.getPreferredCityId()).isEqualTo(11);
    }

    @Test
    public void test_linkAddress_returns_a_copy_pointing_at_the_address_and_keeps_everything_else() {
        User user = createUserWithPreferredCity(11);

        User linked = user.linkAddress(42);

        assertThat(linked.getAddressId()).isEqualTo(42);
        assertThat(linked.getId()).isEqualTo(user.getId());
        assertThat(linked.getEmail()).isEqualTo(user.getEmail());
        assertThat(linked.getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(linked.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(linked.getName()).isEqualTo(user.getName());
        assertThat(linked.getSurname()).isEqualTo(user.getSurname());
        assertThat(linked.getPhoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(linked.getRole()).isEqualTo(user.getRole());
        assertThat(linked.getPreferredCityId()).isEqualTo(11);
        assertThat(user.getAddressId()).isNull();
    }

    @Test
    public void test_linkAddress_rejects_null_and_unlinkAddress_clears() {
        User user = createUserWithBirthDate(null).linkAddress(7);

        assertThatThrownBy(() -> user.linkAddress(null))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThat(user.unlinkAddress().getAddressId()).isNull();
    }

    @Test
    public void test_a_new_user_is_not_email_verified() {
        User user = createUserWithBirthDate(null);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getEmailVerifiedAt()).isNull();
    }

    @Test
    public void test_verifyEmail_stamps_the_time_and_keeps_every_other_field() {
        User user = createUserWithBirthDate(LocalDate.of(1990, 5, 1));
        Instant now = Instant.parse("2026-08-16T10:00:00Z");
        User verified = user.verifyEmail(now);
        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(verified.getEmailVerifiedAt()).isEqualTo(now);
        assertThat(verified.getId()).isEqualTo(user.getId());
        assertThat(verified.getEmail()).isEqualTo(user.getEmail());
        assertThat(verified.getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(verified.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    public void test_verifyEmail_twice_is_a_conflict() {
        User verified = createUserWithBirthDate(null).verifyEmail(Instant.parse("2026-08-16T10:00:00Z"));
        assertThatThrownBy(() -> verified.verifyEmail(Instant.parse("2026-08-16T11:00:00Z")))
                .isInstanceOf(EmailAlreadyVerifiedException.class)
                .hasMessage("Email john.doe@example.com is already verified");
    }

    @Test
    public void test_verifyEmail_rejects_a_null_instant() {
        assertThatThrownBy(() -> createUserWithBirthDate(null).verifyEmail(null))
                .isInstanceOf(DomainObjectValidationException.class);
    }
}
