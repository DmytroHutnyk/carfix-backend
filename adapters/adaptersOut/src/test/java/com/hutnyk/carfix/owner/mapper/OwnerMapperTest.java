package com.hutnyk.carfix.owner.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.entity.OwnerEntity;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OwnerMapperTest {

    @Test
    public void test_toDomain_maps_user_and_business_fields() {
        //given
        UUID id = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName("Marek");
        user.setSurname("Kowalski");
        user.setPhoneNumber("600100200");
        user.setPhoneCountryCode("+48");
        user.setEmail("owner@carfix.dev");
        user.setPassword("$2a$10$storedhashvalue");
        user.setRole(UserRole.OWNER);
        OwnerEntity entity = new OwnerEntity();
        entity.setId(id);
        entity.setUserEntity(user);
        entity.setBusinessName("AutoSerwis Kowalski");
        entity.setVatIn("5252445567");
        entity.setRegon("146892132");

        //when
        Owner owner = OwnerMapper.toDomain(entity);

        //then
        assertThat(owner.getUser().getId().id()).isEqualTo(id);
        assertThat(owner.getUser().getRole()).isEqualTo(UserRole.OWNER);
        assertThat(owner.getBusinessName()).isEqualTo("AutoSerwis Kowalski");
        assertThat(owner.getVatIn()).isEqualTo("5252445567");
        assertThat(owner.getRegon()).isEqualTo("146892132");
        assertThat(OwnerMapper.toDomain(null)).isNull();
    }
}
