package com.hutnyk.carfix.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static User user(Instant emailVerifiedAt) {
        return User.builder()
                .id(UserId.genId())
                .name("John")
                .surname("Doe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(LocalDate.of(1990, 5, 1))
                .addressId(null)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    @Test
    public void test_toEntity_and_toDomain_round_trip_email_verified_at() {
        //given
        User user = user(VERIFIED_AT);
        //when
        UserEntity entity = UserMapper.toEntity(user, null);
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
        UserEntity entity = UserMapper.toEntity(user(null), null);
        //when + then
        assertThat(UserMapper.toDomain(entity).isEmailVerified()).isFalse();
    }

    @Test
    public void test_updateEntity_copies_every_scalar_field_of_the_domain_object() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null), null);
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
                .emailVerifiedAt(VERIFIED_AT)
                .build();
        //when
        UserEntity result = UserMapper.updateEntity(entity, changed);
        //then
        assertThat(result).isSameAs(entity);
        assertThat(entity.getName()).isEqualTo("Jane");
        assertThat(entity.getSurname()).isEqualTo("Roe");
        assertThat(entity.getPhoneCountryCode()).isEqualTo("+49");
        assertThat(entity.getPhoneNumber()).isEqualTo("987654321");
        assertThat(entity.getEmail()).isEqualTo("jane@example.com");
        assertThat(entity.getPassword()).isEqualTo("$2a$10$otherhash");
        assertThat(entity.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(entity.getEmailVerifiedAt()).isEqualTo(VERIFIED_AT);
    }
}
