package com.hutnyk.carfix.user;

import com.hutnyk.carfix.InvalidDomainObjectError;
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
        this.id = Validator.notNull(id);
        this.name = Validator.notEmpty(name);
        this.surname = Validator.notEmpty(surname);
        this.phoneNumber = Validator.notNull(phoneNumber);
        this.email = validateEmail(email);
        this.passwordHash = Validator.notNull(passwordHash); //TODO add proper password validation
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

    private static String validateEmail(String email){
       Validator.notEmpty(email);

       if(!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")){
           throw new InvalidDomainObjectError("Email is not valid");
       }
       return email;
    }

    private static LocalDate validateBirthDate(LocalDate date){
        if(date == null){
            return null;
        }

        if(date.isAfter(LocalDate.now())){
            throw new InvalidDomainObjectError("Date can not be in future");
        }

        if(date.isBefore(LocalDate.of(1900, 1, 1))){
            throw new InvalidDomainObjectError("Date can not be smaller than 1900.01.01");
        }

        return date;
    }
}
