package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.address.query.LocationView;
import com.hutnyk.carfix.in.user.commands.LocationCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.user.EmailVerificationCodePortOut;
import com.hutnyk.carfix.out.user.UserNotificationPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.user.exception.VerificationCodeAttemptsExceededException;
import com.hutnyk.carfix.user.exception.VerificationCodeExpiredException;
import com.hutnyk.carfix.user.exception.VerificationCodeInvalidException;
import com.hutnyk.carfix.user.exception.VerificationCodeNotFoundException;
import com.hutnyk.carfix.user.exception.VerificationCodeResendTooSoonException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId EXISTING_ID = UserId.genId();
    private static final PhoneNumber EXISTING_PHONE = new PhoneNumber("+48", "123456789");
    private static final PasswordHash EXISTING_HASH = PasswordHash.of("$2a$10$storedhashvalue");
    private static final Instant NOW = Instant.parse("2026-08-16T10:00:00Z");
    private static final Instant VERIFIED_EARLIER = Instant.parse("2026-08-01T09:00:00Z");
    private static final UpdateUserAddressCommand ADDRESS_COMMAND = new UpdateUserAddressCommand(
            "Marszałkowska", "10", "3A", "00-001", "Warsaw", "Masovian Voivodeship", "PL",
            new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJ_place");

    private static User existingUser(Integer addressId) {
        return existingUser(addressId, null);
    }

    private static User existingUser(Integer addressId, Instant emailVerifiedAt) {
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
                .preferredCityId(11)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    private static final class StubUserPortOut implements UserPortOut {
        User stored;
        User updated;
        final List<String> calls;

        StubUserPortOut(User stored, List<String> calls) {
            this.stored = stored;
            this.calls = calls;
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
            calls.add("user.update");
            return user;
        }
    }

    private static final class StubAddressPortOut implements AddressPortOut {
        final List<Address> inserted = new ArrayList<>();
        final List<Address> updated = new ArrayList<>();
        final List<Integer> deleted = new ArrayList<>();
        final List<Region> insertedRegions = new ArrayList<>();
        final List<City> insertedCities = new ArrayList<>();
        final List<City> updatedCities = new ArrayList<>();
        Integer viewRequestedFor;
        int nextAddressId = 500;
        final List<String> calls;

        StubAddressPortOut(List<String> calls) {
            this.calls = calls;
        }

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
            calls.add("address.insert");
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
            calls.add("address.deleteById");
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
                    ? Optional.of(City.of(11, name, regionId, null, null))
                    : Optional.empty();
        }

        @Override
        public City insertCity(City city) {
            City saved = City.of(110, city.getName(), city.getRegionId(), city.getLatitude(), city.getLongitude());
            insertedCities.add(saved);
            return saved;
        }

        @Override
        public Optional<LocationView> loadCityView(Integer cityId) {
            throw new AssertionError("not used");
        }

        @Override
        public City updateCity(City city) {
            updatedCities.add(city);
            return city;
        }
    }

    private static final class StubCodePortOut implements EmailVerificationCodePortOut {
        EmailVerificationCode stored;
        EmailVerificationCode inserted;
        EmailVerificationCode updated;
        UserId deletedFor;

        @Override
        public Optional<EmailVerificationCode> findByUserId(UserId userId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public EmailVerificationCode insert(EmailVerificationCode code) {
            this.inserted = code;
            this.stored = code;
            return code;
        }

        @Override
        public EmailVerificationCode update(EmailVerificationCode code) {
            this.updated = code;
            this.stored = code;
            return code;
        }

        @Override
        public void deleteByUserId(UserId userId) {
            this.deletedFor = userId;
            this.stored = null;
        }
    }

    private static final class RecordingNotifier implements UserNotificationPortOut {
        User recipient;
        String plainCode;
        int calls;

        @Override
        public void sendEmailVerificationCode(User user, String plainCode) {
            this.recipient = user;
            this.plainCode = plainCode;
            this.calls++;
        }
    }

    private final List<String> calls = new ArrayList<>();
    private final StubUserPortOut userPortOut = new StubUserPortOut(existingUser(7), calls);
    private final StubAddressPortOut addressPortOut = new StubAddressPortOut(calls);
    private final StubCodePortOut codePortOut = new StubCodePortOut();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final UserService service = new UserService(userPortOut, addressPortOut, codePortOut, notifier, clock);

    private UserService serviceWithNoUser() {
        return new UserService(new StubUserPortOut(null, calls), addressPortOut, codePortOut, notifier, clock);
    }

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
        assertThat(result.getPreferredCityId()).isEqualTo(11);
        assertThat(addressPortOut.updatedCities).hasSize(1);
        assertThat(addressPortOut.updatedCities.get(0).getId()).isEqualTo(11);
        assertThat(addressPortOut.updatedCities.get(0).getLatitude()).isEqualByComparingTo("52.2297");
        assertThat(addressPortOut.updatedCities.get(0).getLongitude()).isEqualByComparingTo("21.0122");
    }

    @Test
    public void test_updateUser_preferred_location_in_an_unknown_city_inserts_region_and_city() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null,
                new LocationCommand("Berlin", "Berlin", "DE", new BigDecimal("52.52"), new BigDecimal("13.405")));

        //when
        User result = service.updateUser("john@example.com", command);

        //then
        assertThat(addressPortOut.insertedRegions).hasSize(1);
        assertThat(addressPortOut.insertedRegions.get(0).getCountryIso()).isEqualTo(CountryIso.DE);
        assertThat(addressPortOut.insertedCities).hasSize(1);
        assertThat(addressPortOut.insertedCities.get(0).getLatitude()).isEqualByComparingTo("52.52");
        assertThat(addressPortOut.insertedCities.get(0).getLongitude()).isEqualByComparingTo("13.405");
        assertThat(addressPortOut.updatedCities).isEmpty();
        assertThat(result.getPreferredCityId()).isEqualTo(110);
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
    public void test_updateUser_preserves_the_email_verification_stamp() {
        //given
        userPortOut.stored = existingUser(7, VERIFIED_EARLIER);
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null, null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(userPortOut.updated.getEmailVerifiedAt()).isEqualTo(VERIFIED_EARLIER);
    }

    @Test
    public void test_updateUser_null_dateOfBirth_and_null_location_clear_them_instead_of_keeping_old_values() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null, null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(userPortOut.updated.getDateOfBirth()).isNull();
        assertThat(userPortOut.updated.getPreferredCityId()).isNull();
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
        UserService serviceWithNoUser = serviceWithNoUser();
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
        StubUserPortOut freshUsers = new StubUserPortOut(existingUser(null), calls);
        UserService fresh = new UserService(freshUsers, addressPortOut, codePortOut, notifier, clock);

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
        assertThat(freshUsers.updated.getPreferredCityId()).isEqualTo(11);
        assertThat(addressPortOut.viewRequestedFor).isEqualTo(500);
        assertThat(view.city()).isEqualTo("Warsaw");
        assertThat(calls).containsExactly("address.insert", "user.update");
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
        assertThat(userPortOut.updated.getPreferredCityId()).isEqualTo(11);
        assertThat(addressPortOut.deleted).containsExactly(7);
        assertThat(calls).containsExactly("user.update", "address.deleteById");
    }

    @Test
    public void test_deleteAddress_without_address_is_a_no_op() {
        //given
        StubUserPortOut noAddress = new StubUserPortOut(existingUser(null), calls);
        UserService fresh = new UserService(noAddress, addressPortOut, codePortOut, notifier, clock);

        //when
        fresh.deleteAddress("john@example.com");

        //then
        assertThat(noAddress.updated).isNull();
        assertThat(addressPortOut.deleted).isEmpty();
    }

    // ---- requestEmailVerification -----------------------------------------------------------

    @Test
    public void test_request_issues_a_hashed_code_and_emails_the_plain_code_to_the_user() {
        //when
        service.requestEmailVerification(EMAIL);
        //then
        assertThat(codePortOut.inserted).isNotNull();
        assertThat(codePortOut.updated).isNull();
        assertThat(codePortOut.inserted.getUserId()).isEqualTo(EXISTING_ID);
        assertThat(codePortOut.inserted.getIssuedAt()).isEqualTo(NOW);
        assertThat(codePortOut.inserted.getExpiresAt()).isEqualTo(NOW.plus(EmailVerificationCode.TTL));
        assertThat(notifier.calls).isEqualTo(1);
        assertThat(notifier.recipient.getEmail()).isEqualTo(EMAIL);
        assertThat(notifier.plainCode).matches("[0-9]{6}");
        assertThat(codePortOut.inserted.getCodeHash()).isNotEqualTo(notifier.plainCode);
        assertThat(codePortOut.inserted.matches(notifier.plainCode)).isTrue();
    }

    @Test
    public void test_request_for_an_already_verified_email_is_a_conflict_and_sends_nothing() {
        //given
        userPortOut.stored = existingUser(7, VERIFIED_EARLIER);
        //when + then
        assertThatThrownBy(() -> service.requestEmailVerification(EMAIL))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
        assertThat(codePortOut.inserted).isNull();
        assertThat(notifier.calls).isZero();
    }

    @Test
    public void test_request_within_the_cooldown_is_refused_with_the_seconds_left() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "111111", NOW.minusSeconds(15));
        //when + then
        assertThatThrownBy(() -> service.requestEmailVerification(EMAIL))
                .isInstanceOf(VerificationCodeResendTooSoonException.class)
                .hasMessage("A verification code was sent recently. You can request a new one in 45 seconds");
        assertThat(codePortOut.updated).isNull();
        assertThat(codePortOut.inserted).isNull();
        assertThat(notifier.calls).isZero();
    }

    @Test
    public void test_request_after_the_cooldown_replaces_the_existing_code_through_update() {
        //given
        EmailVerificationCode old = EmailVerificationCode.issue(EXISTING_ID, "111111", NOW.minus(EmailVerificationCode.RESEND_COOLDOWN))
                .registerFailedAttempt();
        codePortOut.stored = old;
        //when
        service.requestEmailVerification(EMAIL);
        //then
        assertThat(codePortOut.inserted).isNull();
        assertThat(codePortOut.updated).isNotNull();
        assertThat(codePortOut.updated.getAttempts()).isZero();
        assertThat(codePortOut.updated.getIssuedAt()).isEqualTo(NOW);
        assertThat(codePortOut.updated.matches("111111")).isFalse();
        assertThat(codePortOut.updated.matches(notifier.plainCode)).isTrue();
    }

    @Test
    public void test_request_for_a_vanished_user_fails_as_authentication() {
        //when + then
        assertThatThrownBy(() -> serviceWithNoUser().requestEmailVerification("gone@example.com"))
                .isInstanceOf(AuthenticatedUserMissingException.class);
    }

    // ---- verifyEmail --------------------------------------------------------------------------

    @Test
    public void test_verify_with_the_right_code_marks_the_user_verified_now_and_consumes_the_code() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when
        User result = service.verifyEmail(EMAIL, "123456");
        //then
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(userPortOut.updated.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(userPortOut.updated.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(codePortOut.deletedFor).isEqualTo(EXISTING_ID);
    }

    @Test
    public void test_verify_with_a_wrong_code_records_the_attempt_and_reports_attempts_left() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "000000"))
                .isInstanceOf(VerificationCodeInvalidException.class)
                .hasMessage("Incorrect verification code. 4 attempt(s) left");
        assertThat(codePortOut.updated.getAttempts()).isEqualTo(1);
        assertThat(userPortOut.updated).isNull();
        assertThat(codePortOut.deletedFor).isNull();
    }

    @Test
    public void test_the_last_wrong_attempt_exhausts_the_code() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        for (int i = 0; i < EmailVerificationCode.MAX_ATTEMPTS - 1; i++) {
            code = code.registerFailedAttempt();
        }
        codePortOut.stored = code;
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "000000"))
                .isInstanceOf(VerificationCodeAttemptsExceededException.class);
        assertThat(codePortOut.updated.getAttempts()).isEqualTo(EmailVerificationCode.MAX_ATTEMPTS);
        assertThat(userPortOut.updated).isNull();
    }

    @Test
    public void test_an_exhausted_code_is_refused_even_with_the_right_digits() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        for (int i = 0; i < EmailVerificationCode.MAX_ATTEMPTS; i++) {
            code = code.registerFailedAttempt();
        }
        codePortOut.stored = code;
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeAttemptsExceededException.class);
        assertThat(userPortOut.updated).isNull();
    }

    @Test
    public void test_an_expired_code_is_refused_even_with_the_right_digits() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minus(EmailVerificationCode.TTL));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeExpiredException.class);
        assertThat(userPortOut.updated).isNull();
        assertThat(codePortOut.updated).isNull();
    }

    @Test
    public void test_verify_without_an_active_code_is_not_found() {
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeNotFoundException.class);
        assertThat(userPortOut.updated).isNull();
    }

    @Test
    public void test_verify_for_an_already_verified_email_is_a_conflict() {
        //given
        userPortOut.stored = existingUser(7, VERIFIED_EARLIER);
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
        assertThat(userPortOut.updated).isNull();
    }
}
