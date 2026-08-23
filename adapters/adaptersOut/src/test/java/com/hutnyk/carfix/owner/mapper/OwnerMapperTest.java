package com.hutnyk.carfix.owner.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.entity.OwnerEntity;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.entity.UserEntity;
import com.hutnyk.carfix.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OwnerMapperTest {

    private static User user() {
        return User.builder()
                .id(UserId.genId())
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email("owner@carfix.dev")
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    @Test
    public void test_toDomain_copies_business_fields_and_wraps_user() {
        //given
        OwnerEntity entity = new OwnerEntity();
        entity.setBusinessName("AutoSerwis Kowalski");
        entity.setVatIn("5252445567");
        entity.setRegon("146892132");
        User user = user();

        //when
        Owner owner = OwnerMapper.toDomain(entity, user);

        //then
        assertThat(owner.getUser()).isSameAs(user);
        assertThat(owner.getBusinessName()).isEqualTo("AutoSerwis Kowalski");
        assertThat(owner.getVatIn()).isEqualTo("5252445567");
        assertThat(owner.getRegon()).isEqualTo("146892132");
    }

    @Test
    public void test_toDomain_composes_the_owner_around_the_mapped_user_entity() {
        //given
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setName("Marek");
        userEntity.setSurname("Kowalski");
        userEntity.setPhoneCountryCode("+48");
        userEntity.setPhoneNumber("600100200");
        userEntity.setEmail("owner@carfix.dev");
        userEntity.setPassword("$2a$10$storedhashvalue");
        userEntity.setRole(UserRole.OWNER);
        OwnerEntity entity = new OwnerEntity();
        entity.setId(userId);
        entity.setUserEntity(userEntity);
        entity.setBusinessName("AutoSerwis Kowalski");
        entity.setVatIn("5252445567");
        entity.setRegon("146892132");

        //when
        Owner owner = OwnerMapper.toDomain(entity, UserMapper.toDomain(entity.getUserEntity()));

        //then
        assertThat(owner.getUser().getId().id()).isEqualTo(userId);
        assertThat(owner.getUser().getRole()).isEqualTo(UserRole.OWNER);
        assertThat(owner.getUser().getEmail()).isEqualTo("owner@carfix.dev");
        assertThat(owner.getBusinessName()).isEqualTo("AutoSerwis Kowalski");
    }

    @Test
    public void test_toDomain_null_guard() {
        assertThat(OwnerMapper.toDomain(null, user())).isNull();
    }
}
