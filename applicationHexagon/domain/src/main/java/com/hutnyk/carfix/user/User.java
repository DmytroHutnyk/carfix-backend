package com.hutnyk.carfix.user;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.*;
import java.time.LocalDate;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class User {

    @EqualsAndHashCode.Include
    private final UserId id;
    private final String name;
    private final String surname;
    private final PhoneNumber phoneNumber;
    private final String email;

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    private final PasswordHash passwordHash;

    //Nullable
    private final LocalDate dateOfBirth;

    //Nullable
    private final Integer addressId;

    @Builder
    public User(
            UserId id,
            String name,
            String surname,
            PhoneNumber phoneNumber,
            String email,
            PasswordHash passwordHash,
            LocalDate dateOfBirth,
            Integer addressId) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.surname = Validator.notBlank(surname, "surname");
        this.phoneNumber = Validator.notNull(phoneNumber, "phoneNumber");
        this.email = Validator.validateEmail(email, "email");
        this.passwordHash = Validator.notNull(passwordHash, "passwordHash");
        this.dateOfBirth = validateBirthDate(dateOfBirth); //TODO add age restriction?
        this.addressId = addressId;
    }

    public static User of(
            UserId id,
            String name,
            String surname,
            PhoneNumber phoneNumber,
            String email,
            PasswordHash passwordHash,
            LocalDate dateOfBirth,
            Integer addressId){
        return User.builder()
                .id(id)
                .name(name)
                .surname(surname)
                .phoneNumber(phoneNumber)
                .email(email)
                .passwordHash(passwordHash)
                .dateOfBirth(dateOfBirth)
                .addressId(addressId)
                .build();
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
