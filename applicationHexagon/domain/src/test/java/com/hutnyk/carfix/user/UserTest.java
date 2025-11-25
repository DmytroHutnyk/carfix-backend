package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;

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
                null
        );
    }

    @Test
    public void test_validateBirthDate_valid_date_and_fieldName_not_null() {
        //when
        LocalDate birthDate = LocalDate.now().minusYears(30);
        User result = createUserWithBirthDate(birthDate);

        //then
        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_valid_date_exactly_1900_01_01() {
        //when
        LocalDate birthDate = LocalDate.of(1900, 1, 1);
        User result = createUserWithBirthDate(birthDate);

        //then
        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_valid_date_today() {
        //when
        LocalDate birthDate = LocalDate.now();
        User result = createUserWithBirthDate(birthDate);

        //then
        assertThat(result.getDateOfBirth()).isEqualTo(birthDate);
    }

    @Test
    public void test_validateBirthDate_returns_null_when_dateOfBirth_is_null() {
        //when
        User result = createUserWithBirthDate(null);

        //then
        assertThat(result.getDateOfBirth()).isNull();
    }

    @Test
    public void test_validateBirthDate_throws_when_date_in_future() {
        //when + then
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> createUserWithBirthDate(futureDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_IN_FUTURE);
    }

    @Test
    public void test_validateBirthDate_throws_when_date_before_1900_01_01() {
        //when + then
        LocalDate oldDate = LocalDate.of(1899, 5, 5);
        assertThatThrownBy(() -> createUserWithBirthDate(oldDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_OLD);
    }

}

