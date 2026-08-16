package com.hutnyk.carfix.user;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.util.Validator;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class User {

    @EqualsAndHashCode.Include
    private final UserId id;
    private final String name;
    private final String surname;
    private final PhoneNumber phoneNumber;
    private final String email;
    private final UserRole role;

    @ToString.Exclude
//    @Getter(AccessLevel.NONE)
    private final PasswordHash passwordHash;

    //Nullable
    private final LocalDate dateOfBirth;

    //Nullable
    private final Integer addressId;

    //Nullable
    @With(AccessLevel.PRIVATE)
    private final Instant emailVerifiedAt;

    @Builder
    private User(
            UserId id,
            String name,
            String surname,
            PhoneNumber phoneNumber,
            String email,
            UserRole role,
            PasswordHash passwordHash,
            LocalDate dateOfBirth,
            Integer addressId,
            Instant emailVerifiedAt) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.surname = Validator.notBlank(surname, "surname");
        this.phoneNumber = Validator.notNull(phoneNumber, "phoneNumber");
        this.email = Validator.validateEmail(email, "email");
        this.role =  Validator.notNull(role, "role");
        this.passwordHash = Validator.notNull(passwordHash, "passwordHash");
        this.dateOfBirth = validateBirthDate(dateOfBirth); //TODO add age restriction?
        this.addressId = addressId;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public static User of(
            UserId id,
            String name,
            String surname,
            PhoneNumber phoneNumber,
            String email,
            UserRole role,
            PasswordHash passwordHash,
            LocalDate dateOfBirth,
            Integer addressId,
            Instant emailVerifiedAt){
        return User.builder()
                .id(id)
                .name(name)
                .surname(surname)
                .phoneNumber(phoneNumber)
                .email(email)
                .role(role)
                .passwordHash(passwordHash)
                .dateOfBirth(dateOfBirth)
                .addressId(addressId)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public User verifyEmail(Instant now) {
        if (isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }
        return withEmailVerifiedAt(Validator.notNull(now, "now"));
    }

    private static LocalDate validateBirthDate(LocalDate date){
        if(date == null){
            return null;
        }

        if(date.isAfter(LocalDate.now())){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_IN_FUTURE, "dateOfBirth", date);
        }

        if(date.isBefore(LocalDate.of(1900, 1, 1))){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_TOO_OLD, "dateOfBirth", date);
        }

        return date;
    }
}
