package com.hutnyk.carfix.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Location;
import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UserMapperTest {

    private static final Location WARSAW = new Location("Warsaw", "Masovian Voivodeship", CountryIso.PL,
            new BigDecimal("52.229700"), new BigDecimal("21.012200"));

    private static User user(Integer addressId, Location preferredLocation) {
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
                .preferredLocation(preferredLocation)
                .build();
    }

    @Test
    public void test_toEntity_and_toDomain_round_trip_the_preferred_location() {
        //given
        User user = user(null, WARSAW);

        //when
        UserEntity entity = UserMapper.toEntity(user, null);
        User back = UserMapper.toDomain(entity);

        //then
        assertThat(entity.getPreferredCity()).isEqualTo("Warsaw");
        assertThat(entity.getPreferredRegion()).isEqualTo("Masovian Voivodeship");
        assertThat(entity.getPreferredCountryIso()).isEqualTo(CountryIso.PL);
        assertThat(entity.getPreferredLatitude()).isEqualByComparingTo("52.229700");
        assertThat(entity.getPreferredLongitude()).isEqualByComparingTo("21.012200");
        assertThat(back.getPreferredLocation()).isEqualTo(WARSAW);
        assertThat(back.getAddressId()).isNull();
    }

    @Test
    public void test_toDomain_yields_no_location_when_the_country_column_is_null() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, null), null);

        //when + then
        assertThat(entity.getPreferredCountryIso()).isNull();
        assertThat(UserMapper.toDomain(entity).getPreferredLocation()).isNull();
    }

    @Test
    public void test_updateEntity_copies_editable_fields_address_link_and_preferred_location() {
        //given
        UserEntity entity = UserMapper.toEntity(user(null, WARSAW), null);
        AddressEntity address = new AddressEntity();
        address.setId(9);
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
                .preferredLocation(null)
                .build();

        //when
        UserMapper.updateEntity(entity, updated, address);

        //then
        assertThat(entity.getName()).isEqualTo("Jane");
        assertThat(entity.getSurname()).isEqualTo("Roe");
        assertThat(entity.getDateOfBirth()).isNull();
        assertThat(entity.getAddressEntity()).isSameAs(address);
        assertThat(entity.getPreferredCity()).isNull();
        assertThat(entity.getPreferredRegion()).isNull();
        assertThat(entity.getPreferredCountryIso()).isNull();
        assertThat(entity.getPreferredLatitude()).isNull();
        assertThat(entity.getPreferredLongitude()).isNull();
        assertThat(UserMapper.toDomain(entity).getAddressId()).isEqualTo(9);
    }
}
