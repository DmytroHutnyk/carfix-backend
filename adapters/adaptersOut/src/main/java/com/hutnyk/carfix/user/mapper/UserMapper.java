package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.entity.UserEntity;

public class UserMapper {

    public static UserEntity toEntity(User user, AddressEntity addressEntity, CityEntity preferredCityEntity) {
        if (user == null) {
            return null;
        }
        return new UserEntity(
                user.getId().id(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber().phoneNumber(),
                user.getPhoneNumber().countryCode(),
                user.getEmail(),
                user.getPasswordHash().getValue(),
                user.getRole(),
                user.getDateOfBirth(),
                user.getEmailVerifiedAt(),
                addressEntity,
                preferredCityEntity
        );
    }

    public static void updateEntity(UserEntity entity, User user, AddressEntity addressEntity, CityEntity preferredCityEntity) {
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        entity.setPhoneNumber(user.getPhoneNumber().phoneNumber());
        entity.setPhoneCountryCode(user.getPhoneNumber().countryCode());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPasswordHash().getValue());
        entity.setRole(user.getRole());
        entity.setDateOfBirth(user.getDateOfBirth());
        entity.setEmailVerifiedAt(user.getEmailVerifiedAt());
        entity.setAddressEntity(addressEntity);
        entity.setPreferredCityEntity(preferredCityEntity);
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserId userId = UserId.of(entity.getId());
        PhoneNumber phoneNumber = new PhoneNumber(
                entity.getPhoneCountryCode(),
                entity.getPhoneNumber()
        );
        PasswordHash passwordHash = PasswordHash.of(entity.getPassword());
        Integer addressId = entity.getAddressEntity() != null
                ? entity.getAddressEntity().getId()
                : null;
        Integer preferredCityId = entity.getPreferredCityEntity() != null
                ? entity.getPreferredCityEntity().getId()
                : null;
        return User.builder()
                .id(userId)
                .name(entity.getName())
                .surname(entity.getSurname())
                .phoneNumber(phoneNumber)
                .email(entity.getEmail())
                .role(entity.getRole())
                .passwordHash(passwordHash)
                .dateOfBirth(entity.getDateOfBirth())
                .addressId(addressId)
                .preferredCityId(preferredCityId)
                .emailVerifiedAt(entity.getEmailVerifiedAt())
                .build();
    }
}
