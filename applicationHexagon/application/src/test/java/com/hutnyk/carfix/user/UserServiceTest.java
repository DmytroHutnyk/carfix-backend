package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Location;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.user.commands.LocationCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserServiceTest {

    private static final UserId EXISTING_ID = UserId.genId();
    private static final PhoneNumber EXISTING_PHONE = new PhoneNumber("+48", "123456789");
    private static final PasswordHash EXISTING_HASH = PasswordHash.of("$2a$10$storedhashvalue");
    private static final Location EXISTING_LOCATION = new Location("Kraków", "Lesser Poland Voivodeship", CountryIso.PL, null, null);
    private static final UpdateUserAddressCommand ADDRESS_COMMAND = new UpdateUserAddressCommand(
            "Marszałkowska", "10", "3A", "00-001", "Warsaw", "Masovian Voivodeship", "PL",
            new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJ_place");

    private static User existingUser(Integer addressId) {
        return User.builder()
                .id(EXISTING_ID)
                .name("Old")
                .surname("Name")
                .phoneNumber(EXISTING_PHONE)
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(EXISTING_HASH)
                .dateOfBirth(LocalDate.of(1990, 5, 1))
                .addressId(addressId)
                .preferredLocation(EXISTING_LOCATION)
                .build();
    }

    private static final class StubUserPortOut implements UserPortOut {
        User stored;
        User updated;

        StubUserPortOut(User stored) {
            this.stored = stored;
        }

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.ofNullable(stored);
        }

        @Override
        public boolean existsByEmail(String email) {
            return stored != null;
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            return stored != null;
        }

        @Override
        public User update(User user) {
            this.updated = user;
            this.stored = user;
            return user;
        }
    }

    private static final class StubAddressPortOut implements AddressPortOut {
        final List<Address> inserted = new ArrayList<>();
        final List<Address> updated = new ArrayList<>();
        final List<Integer> deleted = new ArrayList<>();
        final List<Region> insertedRegions = new ArrayList<>();
        final List<City> insertedCities = new ArrayList<>();
        Integer viewRequestedFor;
        int nextAddressId = 500;

        @Override
        public Optional<AddressView> loadView(Integer addressId) {
            this.viewRequestedFor = addressId;
            return Optional.of(new AddressView(addressId, "Marszałkowska", "10", "3A", "00-001",
                    "Warsaw", "Masovian Voivodeship", CountryIso.PL, "Poland",
                    new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJ_place"));
        }

        @Override
        public Address insert(Address address) {
            Address saved = Address.of(nextAddressId++, address.getStreetName(), address.getBuildingNumber(),
                    address.getFlatNumber(), address.getPostalCode(), address.getCityId(),
                    address.getLatitude(), address.getLongitude(), address.getGooglePlaceId());
            inserted.add(saved);
            return saved;
        }

        @Override
        public Address update(Address address) {
            updated.add(address);
            return address;
        }

        @Override
        public void deleteById(Integer addressId) {
            deleted.add(addressId);
        }

        @Override
        public Optional<Region> findRegion(String name, CountryIso countryIso) {
            return "Masovian Voivodeship".equals(name) && countryIso == CountryIso.PL
                    ? Optional.of(Region.of(3, name, countryIso))
                    : Optional.empty();
        }

        @Override
        public Region insertRegion(Region region) {
            Region saved = Region.of(30, region.getName(), region.getCountryIso());
            insertedRegions.add(saved);
            return saved;
        }

        @Override
        public Optional<City> findCity(String name, Integer regionId) {
            return "Warsaw".equals(name) && regionId == 3
                    ? Optional.of(City.of(11, name, regionId))
                    : Optional.empty();
        }

        @Override
        public City insertCity(City city) {
            City saved = City.of(110, city.getName(), city.getRegionId());
            insertedCities.add(saved);
            return saved;
        }
    }

    private final StubUserPortOut userPortOut = new StubUserPortOut(existingUser(7));
    private final StubAddressPortOut addressPortOut = new StubAddressPortOut();
    private final UserService service = new UserService(userPortOut, addressPortOut);

    // ---- updateUser -------------------------------------------------------------------------

    @Test
    public void test_updateUser_takes_editable_fields_from_the_command() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", LocalDate.of(2000, 1, 15),
                new LocationCommand("Warsaw", "Masovian Voivodeship", "PL", new BigDecimal("52.2297"), new BigDecimal("21.0122")));

        //when
        User result = service.updateUser("john@example.com", command);

        //then
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSurname()).isEqualTo("Surname");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(result.getPreferredLocation()).isEqualTo(
                new Location("Warsaw", "Masovian Voivodeship", CountryIso.PL, new BigDecimal("52.2297"), new BigDecimal("21.0122")));
    }

    @Test
    public void test_updateUser_preserves_credentials_identity_and_address_link_from_the_loaded_user() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null, null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(userPortOut.updated.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(userPortOut.updated.getEmail()).isEqualTo("john@example.com");
        assertThat(userPortOut.updated.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(userPortOut.updated.getPhoneNumber()).isEqualTo(EXISTING_PHONE);
        assertThat(userPortOut.updated.getId()).isEqualTo(EXISTING_ID);
        assertThat(userPortOut.updated.getAddressId()).isEqualTo(7);
    }

    @Test
    public void test_updateUser_null_dateOfBirth_and_null_location_clear_them_instead_of_keeping_old_values() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null, null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(userPortOut.updated.getDateOfBirth()).isNull();
        assertThat(userPortOut.updated.getPreferredLocation()).isNull();
    }

    @Test
    public void test_updateUser_rejects_an_unsupported_country_before_touching_the_port() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null,
                new LocationCommand("Nowhere", null, "XX", null, null));

        //when + then
        assertThatThrownBy(() -> service.updateUser("john@example.com", command))
                .isInstanceOf(DomainObjectValidationException.class)
                .hasMessage("Country XX is not supported");
        assertThat(userPortOut.updated).isNull();
    }

    @Test
    public void test_updating_a_user_whose_record_vanished_fails_as_authentication() {
        //given
        UserService serviceWithNoUser = new UserService(new StubUserPortOut(null), addressPortOut);
        UpdateUserCommand command = new UpdateUserCommand("John", "Doe", LocalDate.of(1990, 5, 1), null);

        //when + then
        assertThatThrownBy(() -> serviceWithNoUser.updateUser("gone@example.com", command))
                .isInstanceOf(AuthenticatedUserMissingException.class)
                .hasMessage("Authenticated user not found: gone@example.com");
    }

    // ---- updateAddress ----------------------------------------------------------------------

    @Test
    public void test_updateAddress_for_a_user_without_address_inserts_it_links_the_user_and_returns_the_view() {
        //given
        StubUserPortOut freshUsers = new StubUserPortOut(existingUser(null));
        UserService fresh = new UserService(freshUsers, addressPortOut);

        //when
        AddressView view = fresh.updateAddress("john@example.com", ADDRESS_COMMAND);

        //then
        assertThat(addressPortOut.inserted).hasSize(1);
        Address inserted = addressPortOut.inserted.get(0);
        assertThat(inserted.getId()).isEqualTo(500);
        assertThat(inserted.getStreetName()).isEqualTo("Marszałkowska");
        assertThat(inserted.getBuildingNumber()).isEqualTo("10");
        assertThat(inserted.getFlatNumber()).isEqualTo("3A");
        assertThat(inserted.getPostalCode()).isEqualTo("00-001");
        assertThat(inserted.getCityId()).isEqualTo(11);
        assertThat(inserted.getGooglePlaceId()).isEqualTo("ChIJ_place");
        assertThat(addressPortOut.updated).isEmpty();
        assertThat(freshUsers.updated.getAddressId()).isEqualTo(500);
        assertThat(freshUsers.updated.getPreferredLocation()).isEqualTo(EXISTING_LOCATION);
        assertThat(addressPortOut.viewRequestedFor).isEqualTo(500);
        assertThat(view.city()).isEqualTo("Warsaw");
    }

    @Test
    public void test_updateAddress_for_a_user_with_address_updates_the_same_row_and_does_not_touch_the_user() {
        //when
        AddressView view = service.updateAddress("john@example.com", ADDRESS_COMMAND);

        //then
        assertThat(addressPortOut.inserted).isEmpty();
        assertThat(addressPortOut.updated).hasSize(1);
        assertThat(addressPortOut.updated.get(0).getId()).isEqualTo(7);
        assertThat(userPortOut.updated).isNull();
        assertThat(addressPortOut.viewRequestedFor).isEqualTo(7);
        assertThat(view.id()).isEqualTo(7);
    }

    @Test
    public void test_updateAddress_creates_missing_region_and_city_on_demand() {
        //given
        UpdateUserAddressCommand berlin = new UpdateUserAddressCommand(
                "Unter den Linden", "1", null, "10117", "Berlin", "Berlin", "DE", null, null, null);

        //when
        service.updateAddress("john@example.com", berlin);

        //then
        assertThat(addressPortOut.insertedRegions).hasSize(1);
        assertThat(addressPortOut.insertedRegions.get(0).getCountryIso()).isEqualTo(CountryIso.DE);
        assertThat(addressPortOut.insertedCities).hasSize(1);
        assertThat(addressPortOut.insertedCities.get(0).getRegionId()).isEqualTo(30);
        assertThat(addressPortOut.updated.get(0).getCityId()).isEqualTo(110);
    }

    @Test
    public void test_updateAddress_rejects_an_unsupported_country_before_any_write() {
        //given
        UpdateUserAddressCommand bad = new UpdateUserAddressCommand(
                "Street", "1", null, "00000", "Town", "Region", "XX", null, null, null);

        //when + then
        assertThatThrownBy(() -> service.updateAddress("john@example.com", bad))
                .isInstanceOf(DomainObjectValidationException.class)
                .hasMessage("Country XX is not supported");
        assertThat(addressPortOut.inserted).isEmpty();
        assertThat(addressPortOut.updated).isEmpty();
        assertThat(addressPortOut.insertedRegions).isEmpty();
    }

    // ---- deleteAddress ----------------------------------------------------------------------

    @Test
    public void test_deleteAddress_unlinks_the_user_then_deletes_the_row() {
        //when
        service.deleteAddress("john@example.com");

        //then
        assertThat(userPortOut.updated.getAddressId()).isNull();
        assertThat(userPortOut.updated.getPreferredLocation()).isEqualTo(EXISTING_LOCATION);
        assertThat(addressPortOut.deleted).containsExactly(7);
    }

    @Test
    public void test_deleteAddress_without_address_is_a_no_op() {
        //given
        StubUserPortOut noAddress = new StubUserPortOut(existingUser(null));
        UserService fresh = new UserService(noAddress, addressPortOut);

        //when
        fresh.deleteAddress("john@example.com");

        //then
        assertThat(noAddress.updated).isNull();
        assertThat(addressPortOut.deleted).isEmpty();
    }
}
