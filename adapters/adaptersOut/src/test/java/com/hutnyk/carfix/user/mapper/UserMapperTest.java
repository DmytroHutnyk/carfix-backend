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

import java.time.LocalDate;

public class UserMapperTest {

    private static CityEntity warsaw() {
        CityEntity city = new CityEntity();
        city.setId(11);
        city.setName("Warsaw");
        return city;
    }

    private static User user(Integer addressId, Integer preferredCityId) {
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
                .build();
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
