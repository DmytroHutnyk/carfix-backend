package com.hutnyk.carfix.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

public class UserMapperTest {

    private static final Instant VERIFIED_AT = Instant.parse("2026-08-16T10:00:00Z");

    private static CityEntity warsaw() {
        CityEntity city = new CityEntity();
        city.setId(11);
        city.setName("Warsaw");
        return city;
    }

    private static User user(Integer addressId, Integer preferredCityId) {
        return user(addressId, preferredCityId, null);
    }

    private static User user(Integer addressId, Integer preferredCityId, Instant emailVerifiedAt) {
        return User.builder()
                .id(UserId.genId())
                .name("John")
                .surname("Doe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(LocalDate.of(1990, 5, 1))
                .addressId(addressId)
                .preferredCityId(preferredCityId)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    @Test
    public void test_toEntity_and_toDomain_round_trip_email_verified_at() {
        //given
        User user = user(null, null, VERIFIED_AT);
        //when
        UserEntity entity = UserMapper.toEntity(user, null, null);
        User back = UserMapper.toDomain(entity);
        //then
        assertThat(entity.getEmailVerifiedAt()).isEqualTo(VERIFIED_AT);
        assertThat(back.getEmailVerifiedAt()).isEqualTo(VERIFIED_AT);
        assertThat(back.getId()).isEqualTo(user.getId());
        assertThat(back.getEmail()).isEqualTo("john@example.com");
        assertThat(back.getPhoneNumber()).isEqualTo(new PhoneNumber("+48", "123456789"));
        assertThat(back.getPasswordHash()).isEqualTo(PasswordHash.of("$2a$10$storedhashvalue"));
    }

    @Test
    public void test_toDomain_keeps_an_unverified_user_unverified() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, null), null, null);
        //when + then
        assertThat(UserMapper.toDomain(entity).isEmailVerified()).isFalse();
    }

    @Test
    public void test_updateEntity_copies_every_scalar_field_of_the_domain_object() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, null), null, null);
        User changed = User.builder()
                .id(UserId.of(entity.getId()))
                .name("Jane")
                .surname("Roe")
                .phoneNumber(new PhoneNumber("+49", "987654321"))
                .email("jane@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$otherhash"))
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .addressId(null)
                .preferredCityId(null)
                .emailVerifiedAt(VERIFIED_AT)
                .build();
        //when
        UserMapper.updateEntity(entity, changed, null, null);
        //then
        assertThat(entity.getName()).isEqualTo("Jane");
        assertThat(entity.getSurname()).isEqualTo("Roe");
        assertThat(entity.getPhoneCountryCode()).isEqualTo("+49");
        assertThat(entity.getPhoneNumber()).isEqualTo("987654321");
        assertThat(entity.getEmail()).isEqualTo("jane@example.com");
        assertThat(entity.getPassword()).isEqualTo("$2a$10$otherhash");
        assertThat(entity.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(entity.getEmailVerifiedAt()).isEqualTo(VERIFIED_AT);
    }

    @Test
    public void test_toEntity_and_toDomain_round_trip_the_preferred_city() {
        //given
        CityEntity city = warsaw();

        //when
        UserEntity entity = UserMapper.toEntity(user(null, 11), null, city);
        User back = UserMapper.toDomain(entity);

        //then
        assertThat(entity.getPreferredCityEntity()).isSameAs(city);
        assertThat(back.getPreferredCityId()).isEqualTo(11);
        assertThat(back.getAddressId()).isNull();
    }

    @Test
    public void test_toDomain_yields_no_preferred_city_when_the_reference_is_null() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, null), null, null);

        //when + then
        assertThat(entity.getPreferredCityEntity()).isNull();
        assertThat(UserMapper.toDomain(entity).getPreferredCityId()).isNull();
    }

    @Test
    public void test_updateEntity_copies_editable_fields_address_link_and_preferred_city() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, null), null, null);
        AddressEntity address = new AddressEntity();
        address.setId(9);
        CityEntity city = warsaw();
        User updated = User.builder()
                .id(UserId.of(entity.getId()))
                .name("Jane")
                .surname("Roe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(null)
                .addressId(9)
                .preferredCityId(11)
                .build();

        //when
        UserMapper.updateEntity(entity, updated, address, city);

        //then
        assertThat(entity.getName()).isEqualTo("Jane");
        assertThat(entity.getSurname()).isEqualTo("Roe");
        assertThat(entity.getDateOfBirth()).isNull();
        assertThat(entity.getAddressEntity()).isSameAs(address);
        assertThat(entity.getPreferredCityEntity()).isSameAs(city);
        assertThat(UserMapper.toDomain(entity).getAddressId()).isEqualTo(9);
        assertThat(UserMapper.toDomain(entity).getPreferredCityId()).isEqualTo(11);
    }

    @Test
    public void test_updateEntity_clears_the_preferred_city_when_the_user_has_none() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, 11), null, warsaw());

        //when
        UserMapper.updateEntity(entity, user(null, null), null, null);

        //then
        assertThat(entity.getPreferredCityEntity()).isNull();
        assertThat(UserMapper.toDomain(entity).getPreferredCityId()).isNull();
    }
}
