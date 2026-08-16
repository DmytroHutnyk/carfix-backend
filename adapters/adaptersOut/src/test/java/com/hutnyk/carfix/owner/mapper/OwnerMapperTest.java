package com.hutnyk.carfix.owner.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.entity.OwnerEntity;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

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
    public void test_toDomain_null_guard() {
        assertThat(OwnerMapper.toDomain(null, user())).isNull();
    }
}
