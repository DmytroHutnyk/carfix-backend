package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidBranchRegistrationException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;
import com.hutnyk.carfix.carCatalog.exception.CarBrandNotFoundException;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.address.query.LocationView;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchAddressCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchOpeningHoursCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceBayCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceCommand;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOpeningHoursExceptionCommand;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOverviewCommand;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.carCatalog.CarCatalogPortOut;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.owner.OwnerPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BranchServiceTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-17T08:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String OWNER_EMAIL = "owner@carfix.dev";
    private static final UserId OWNER_ID = UserId.genId();

    private static BranchView view() {
        return new BranchView(
                BRANCH_ID, "AutoFix Mokotow", "+48221234567", "contact@autofix.pl",
                "A workshop.", "Free cancellation up to 24 hours.",
                new BigDecimal("4.7"), 236,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), null, "Europe/Warsaw",
                List.of(), List.of(), List.of());
    }

    private static Owner owner() {
        return Owner.of(ownerUser(), "AutoSerwis Kowalski", "5252445567", "146892132");
    }

    private static User ownerUser() {
        return User.builder()
                .id(OWNER_ID)
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(OWNER_EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    private static AddressView address() {
        return new AddressView(55, "Pulawska", "45", null, "02-515", "Warsaw", "Masovian Voivodeship",
                CountryIso.PL, "Poland", new BigDecimal("52.180000"), new BigDecimal("21.020000"), "ChIJx");
    }

    private static OwnerBranchDetailView detail() {
        return new OwnerBranchDetailView(
                BRANCH_ID, "AutoFix Mokotow", BranchStatus.ACTIVE,
                "A workshop.", CancellationPolicy.MODERATE,
                "+48221234567", "contact@autofix.pl", "Europe/Warsaw",
                address(), List.of(), List.of(), List.of());
    }

    private static OwnerBranchSummaryView summary() {
        return new OwnerBranchSummaryView(
                BRANCH_ID, "AutoFix Mokotow", BranchStatus.ACTIVE,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("4.7"), 236,
                true, 12, 7, 3, 5,
                List.of());
    }

    /* ---------- stubs: every port records what it received and hands back ids ---------- */

    private static final class StubBranchPortOut implements BranchPortOut {
        BranchView view;
        boolean branchExists = true;
        Branch inserted;
        List<OpeningHours> hours;
        Set<Integer> linkedBrands;
        Branch updated;
        List<OpeningHours> replacedHours;
        List<OpeningHoursException> replacedExceptions;
        Set<Integer> replacedBrands;

        @Override public void updateRating(BranchId branchId, BranchRating rating) { throw new UnsupportedOperationException(); }
        @Override public Optional<BranchView> findViewById(BranchId branchId) { return Optional.ofNullable(view); }
        @Override public boolean existsActiveById(BranchId branchId) { return branchExists; }
        @Override public boolean existsByIdAndOwnerId(BranchId branchId, UUID ownerId) { throw new UnsupportedOperationException(); }
        @Override public Optional<ZoneId> findActiveBranchZone(BranchId branchId) { throw new UnsupportedOperationException(); }
        @Override public Branch insert(Branch branch) { this.inserted = branch; return branch; }
        @Override public void insertOpeningHours(List<OpeningHours> openingHours) { this.hours = openingHours; }
        @Override public void linkCarBrands(BranchId branchId, Set<Integer> carBrandIds) { this.linkedBrands = carBrandIds; }
        @Override public Branch update(Branch branch) { this.updated = branch; return branch; }
        @Override public void replaceOpeningHours(BranchId branchId, List<OpeningHours> openingHours) { this.replacedHours = openingHours; }
        @Override public void replaceOpeningHoursExceptions(BranchId branchId, List<OpeningHoursException> exceptions) { this.replacedExceptions = exceptions; }
        @Override public void replaceCarBrands(BranchId branchId, Set<Integer> carBrandIds) { this.replacedBrands = carBrandIds; }
    }

    private static final class StubReviewPortOut implements ReviewPortOut {
        BranchReviewsQuery receivedQuery;

        @Override public boolean existsByBookingId(BookingId bookingId) { throw new UnsupportedOperationException(); }
        @Override public void deleteByCustomerId(UUID customerId) { throw new UnsupportedOperationException(); }
        @Override public Optional<Review> findByBookingId(BookingId bookingId) { throw new UnsupportedOperationException(); }
        @Override public Review insert(Review review) { throw new UnsupportedOperationException(); }
        @Override public Optional<BranchId> findBranchIdByBookingId(BookingId bookingId) { throw new UnsupportedOperationException(); }
        @Override public List<Integer> findStarsByBranchId(BranchId branchId) { throw new UnsupportedOperationException(); }

        @Override
        public BranchReviewsPage findReviewsPage(BranchReviewsQuery query) {
            this.receivedQuery = query;
            return BranchReviewsPage.empty(query.page(), query.size());
        }
    }

    private static final class StubOwnerPortOut implements OwnerPortOut {
        Owner owner = owner();
        @Override public Optional<Owner> findOwnerByUsername(String email) { return Optional.ofNullable(owner); }
    }

    private static final class StubAddressPortOut implements AddressPortOut {
        Address inserted;
        Address updated;
        final List<Region> regions = new ArrayList<>();
        final List<City> cities = new ArrayList<>();

        @Override public Optional<AddressView> loadView(Integer addressId) { throw new UnsupportedOperationException(); }
        @Override public Address update(Address address) { this.updated = address; return address; }
        @Override public void deleteById(Integer addressId) { throw new UnsupportedOperationException(); }

        @Override
        public Address insert(Address address) {
            inserted = Address.of(55, address.getStreetName(), address.getBuildingNumber(), address.getFlatNumber(),
                    address.getPostalCode(), address.getCityId(), address.getLatitude(), address.getLongitude(),
                    address.getGooglePlaceId());
            return inserted;
        }
        @Override public Optional<Region> findRegion(String name, CountryIso countryIso) { return regions.stream().findFirst(); }
        @Override public Region insertRegion(Region region) { Region r = Region.of(100, region.getName(), region.getCountryIso()); regions.add(r); return r; }
        @Override public Optional<City> findCity(String name, Integer regionId) { return cities.stream().findFirst(); }
        @Override public City insertCity(City city) { City c = City.of(200, city.getName(), city.getRegionId(), null, null); cities.add(c); return c; }
        @Override public Optional<LocationView> loadCityView(Integer cityId) { throw new UnsupportedOperationException(); }
        @Override public City updateCity(City city) { throw new UnsupportedOperationException(); }
    }

    private static final class StubCarCatalogPortOut implements CarCatalogPortOut {
        int brandLoads;
        @Override public List<CarBrand> findAllBrands() { brandLoads++; return List.of(CarBrand.of(1, "Audi"), CarBrand.of(2, "BMW"), CarBrand.of(3, "Toyota")); }
        @Override public List<CarModel> findModelsByBrandId(Integer brandId) { throw new UnsupportedOperationException(); }
        @Override public List<ModelVersion> findVersionsByModelId(Integer modelId) { throw new UnsupportedOperationException(); }
        @Override public boolean existsVersionById(Integer id) { throw new UnsupportedOperationException(); }
    }

    private static final class StubServiceBayPortOut implements ServiceBayPortOut {
        final List<ServiceBayType> types = new ArrayList<>();
        final List<ServiceBay> bays = new ArrayList<>();
        @Override public ServiceBayType insertType(ServiceBayType type) { ServiceBayType t = ServiceBayType.of(400 + types.size(), type.getName(), type.getBranchId()); types.add(t); return t; }
        @Override public ServiceBay insert(ServiceBay bay) { bays.add(bay); return bay; }
    }

    private static final class StubEquipmentPortOut implements EquipmentPortOut {
        final List<EquipmentType> types = new ArrayList<>();
        final List<Equipment> units = new ArrayList<>();
        @Override public EquipmentType insertType(EquipmentType type) { EquipmentType t = EquipmentType.of(500 + types.size(), type.getName(), type.getBranchId()); types.add(t); return t; }
        @Override public Equipment insert(Equipment equipment) { units.add(equipment); return equipment; }
        @Override public Equipment update(Equipment equipment) { throw new UnsupportedOperationException(); }
        @Override public List<OwnerEquipmentView> findViewsByBranchId(UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<Equipment> findByIdAndBranchId(Integer equipmentId, UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<EquipmentType> findTypeByNameForBranch(String name, UUID branchId) { throw new UnsupportedOperationException(); }
    }

    private static final class StubRolePortOut implements RolePortOut {
        final List<Role> roles = new ArrayList<>();
        @Override public Role insert(Role role) { Role r = Role.of(600 + roles.size(), role.getName(), role.getBranchId()); roles.add(r); return r; }
    }

    private static final class StubEmployeePortOut implements EmployeePortOut {
        final List<Employee> employees = new ArrayList<>();
        @Override public Employee insert(Employee employee) { employees.add(employee); return employee; }
    }

    private static final class StubServicePortOut implements ServicePortOut {
        final List<Service> services = new ArrayList<>();
        @Override public List<Service> loadByIds(java.util.Collection<Integer> serviceIds) { throw new UnsupportedOperationException(); }
        @Override public Service insert(Service service) { services.add(service); return service; }
    }

    private static final class StubServiceCategoryPortOut implements ServiceCategoryPortOut {
        @Override public List<ServiceCategory> findAll() { return List.of(ServiceCategory.of(1, "Brakes"), ServiceCategory.of(2, "Maintenance")); }
    }

    private static final class StubOwnerBranchPortOut implements OwnerBranchPortOut {
        UserId receivedOwnerId;
        Instant receivedNow;
        UUID receivedBranchId;
        OwnerBranchDetailView detail = detail();

        @Override
        public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) {
            this.receivedOwnerId = ownerId;
            this.receivedNow = now;
            return List.of(summary());
        }

        @Override
        public Optional<OwnerBranchDetailView> findDetailByIdAndOwnerId(UUID branchId, UserId ownerId) {
            this.receivedBranchId = branchId;
            this.receivedOwnerId = ownerId;
            return Optional.ofNullable(detail);
        }
    }

    private static final class StubUserPortOut implements UserPortOut {
        User user;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.ofNullable(user);
        }

        @Override
        public boolean existsByEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User update(User user) {
            throw new UnsupportedOperationException();
        }
    }

    private final StubBranchPortOut branchStub = new StubBranchPortOut();
    private final StubReviewPortOut reviewStub = new StubReviewPortOut();
    private final StubOwnerPortOut ownerStub = new StubOwnerPortOut();
    private final StubOwnerBranchPortOut ownerBranchStub = new StubOwnerBranchPortOut();
    private final StubUserPortOut userStub = new StubUserPortOut();
    private final StubAddressPortOut addressStub = new StubAddressPortOut();
    private final StubCarCatalogPortOut carCatalogStub = new StubCarCatalogPortOut();
    private final StubServiceBayPortOut serviceBayStub = new StubServiceBayPortOut();
    private final StubEquipmentPortOut equipmentStub = new StubEquipmentPortOut();
    private final StubRolePortOut roleStub = new StubRolePortOut();
    private final StubEmployeePortOut employeeStub = new StubEmployeePortOut();
    private final StubServicePortOut serviceStub = new StubServicePortOut();
    private final StubServiceCategoryPortOut serviceCategoryStub = new StubServiceCategoryPortOut();

    private final BranchService service = new BranchService(branchStub, reviewStub, ownerStub, ownerBranchStub,
            userStub, addressStub, carCatalogStub, serviceBayStub, equipmentStub, roleStub, employeeStub, serviceStub,
            serviceCategoryStub, FIXED_CLOCK);

    /* ---------- reads (unchanged behaviour) ---------- */

    @Test
    public void test_getBranch_returns_view() {
        //given
        branchStub.view = view();

        //when
        BranchView result = service.getBranch(BRANCH_ID);

        //then
        assertThat(result.name()).isEqualTo("AutoFix Mokotow");
    }

    @Test
    public void test_getBranch_unknown_id_throws_not_found() {
        //given
        branchStub.view = null;

        //when + then
        assertThatThrownBy(() -> service.getBranch(BRANCH_ID))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void test_getReviews_defaults_null_sort_to_newest() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 10));

        //then
        assertThat(reviewStub.receivedQuery.sort()).isEqualTo(BranchReviewsQuery.SORT_NEWEST);
    }

    @Test
    public void test_getReviews_normalizes_sort_case() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, "HIGHEST", 0, 10));

        //then
        assertThat(reviewStub.receivedQuery.sort()).isEqualTo(BranchReviewsQuery.SORT_HIGHEST);
    }

    @Test
    public void test_getReviews_unknown_sort_throws_before_the_port() {
        //when + then
        assertThatThrownBy(() -> service.getReviews(new BranchReviewsQuery(BRANCH_ID, "bogus", 0, 10)))
                .isInstanceOf(InvalidReviewsSortException.class);
        assertThat(reviewStub.receivedQuery).isNull();
    }

    @Test
    public void test_getReviews_unknown_branch_throws_not_found() {
        //given
        branchStub.branchExists = false;

        //when + then
        assertThatThrownBy(() -> service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 10)))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(reviewStub.receivedQuery).isNull();
    }

    @Test
    public void test_getReviews_caps_size_at_50() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 99));

        //then
        assertThat(reviewStub.receivedQuery.size()).isEqualTo(50);
    }

    /* ---------- registerBranch ---------- */

    @Test
    public void test_registerBranch_persists_the_whole_workshop_and_resolves_names_to_ids() {
        //given
        RegisterBranchCommand cmd = BranchRegistrationValidatorTest.valid();

        //when
        Branch result = service.registerBranch(OWNER_EMAIL, cmd);

        //then — branch + address
        assertThat(result).isSameAs(branchStub.inserted);
        assertThat(result.getStatus()).isEqualTo(BranchStatus.ACTIVE);
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getAddressId()).isEqualTo(55);
        assertThat(result.getTz().getId()).isEqualTo("Europe/Warsaw");
        assertThat(addressStub.inserted.getCityId()).isEqualTo(200);
        assertThat(addressStub.regions).extracting(Region::getCountryIso).containsExactly(CountryIso.PL);
        // hours + brands
        assertThat(branchStub.hours).hasSize(2);
        assertThat(branchStub.hours.get(1).getMode()).isEqualTo(OpeningHoursMode.BY_APPOINTMENT);
        assertThat(branchStub.hours).allSatisfy(h -> assertThat(h.getBranchId()).isEqualTo(result.getId()));
        assertThat(branchStub.linkedBrands).containsExactlyInAnyOrder(1, 2);
        // types are branch-scoped, references resolved case-insensitively
        assertThat(serviceBayStub.types).extracting(ServiceBayType::getName).containsExactly("Basic", "With lift");
        assertThat(serviceBayStub.types).allSatisfy(t -> assertThat(t.getBranchId()).isEqualTo(result.getId()));
        Integer basicId = serviceBayStub.types.get(0).getId();
        Integer withLiftId = serviceBayStub.types.get(1).getId();
        assertThat(serviceBayStub.bays).extracting(ServiceBay::getName, ServiceBay::getServiceBayTypeId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Bay 1", basicId),
                        org.assertj.core.groups.Tuple.tuple("Bay 2", withLiftId));
        Integer liftTypeId = equipmentStub.types.get(0).getId();
        assertThat(equipmentStub.units).singleElement()
                .satisfies(u -> assertThat(u.getEquipmentTypeId()).isEqualTo(liftTypeId));
        Integer mechanicId = roleStub.roles.get(0).getId();
        Integer evId = roleStub.roles.get(1).getId();
        assertThat(employeeStub.employees).singleElement().satisfies(e -> {
            assertThat(e.getFirstName()).isEqualTo("Oleh");
            assertThat(e.getRoleIds()).containsExactly(evId);
            assertThat(e.getBranchId()).isEqualTo(result.getId());
        });
        // service graph
        assertThat(serviceStub.services).singleElement().satisfies(s -> {
            assertThat(s.getBranchId()).isEqualTo(result.getId());
            assertThat(s.getServiceCategoryId()).isEqualTo(2);
            assertThat(s.getServiceBayTypeIds()).containsExactly(basicId);
            assertThat(s.getEmployeeRequirements()).singleElement()
                    .satisfies(r -> assertThat(r.getRoleIds()).containsExactlyInAnyOrder(mechanicId, evId));
            assertThat(s.getEquipmentRequirements()).singleElement()
                    .satisfies(r -> assertThat(r.getEquipmentTypeIds()).containsExactly(liftTypeId));
        });
    }

    @Test
    public void test_registerBranch_with_empty_lists_creates_just_the_branch() {
        //given
        RegisterBranchCommand c = BranchRegistrationValidatorTest.valid();
        RegisterBranchCommand minimal = new RegisterBranchCommand(c.name(), c.phoneNumber(), c.email(), c.timezone(),
                c.address(), List.of(), Set.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        //when
        Branch result = service.registerBranch(OWNER_EMAIL, minimal);

        //then
        assertThat(result).isNotNull();
        assertThat(branchStub.hours).isEmpty();
        assertThat(branchStub.linkedBrands).isNull();
        assertThat(carCatalogStub.brandLoads).isZero();
        assertThat(serviceBayStub.types).isEmpty();
        assertThat(serviceStub.services).isEmpty();
    }

    @Test
    public void test_registerBranch_rejects_inconsistent_payload_before_writing_anything() {
        //given
        RegisterBranchCommand c = BranchRegistrationValidatorTest.valid();
        RegisterBranchCommand bad = BranchRegistrationValidatorTest.with(c, c.serviceBayTypes(),
                List.of(new RegisterBranchServiceBayCommand("Bay 1", "Pit")), c.equipmentTypes(), c.equipment(),
                c.roles(), c.employees(), c.services(), c.openingHours());

        //when + then
        assertThatThrownBy(() -> service.registerBranch(OWNER_EMAIL, bad))
                .isInstanceOf(InvalidBranchRegistrationException.class);
        assertThat(addressStub.inserted).isNull();
        assertThat(branchStub.inserted).isNull();
    }

    @Test
    public void test_registerBranch_unknown_car_brand_is_not_found_and_writes_nothing() {
        //given
        RegisterBranchCommand c = BranchRegistrationValidatorTest.valid();
        RegisterBranchCommand bad = new RegisterBranchCommand(c.name(), c.phoneNumber(), c.email(), c.timezone(),
                c.address(), c.openingHours(), Set.of(1, 99), c.serviceBayTypes(), c.serviceBays(), c.equipmentTypes(),
                c.equipment(), c.roles(), c.employees(), c.services());

        //when + then
        assertThatThrownBy(() -> service.registerBranch(OWNER_EMAIL, bad))
                .isInstanceOf(CarBrandNotFoundException.class)
                .hasMessage("Car brand not found: 99");
        assertThat(branchStub.inserted).isNull();
    }

    @Test
    public void test_registerBranch_unknown_service_category_is_not_found_and_writes_nothing() {
        //given
        RegisterBranchCommand c = BranchRegistrationValidatorTest.valid();
        RegisterBranchServiceCommand s = c.services().get(0);
        RegisterBranchCommand bad = BranchRegistrationValidatorTest.with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), c.equipment(), c.roles(), c.employees(),
                List.of(new RegisterBranchServiceCommand(s.name(), s.description(), s.durationMinutes(), s.price(), 77,
                        s.status(), s.bayTypes(), s.employeeRequirements(), s.equipmentRequirements())),
                c.openingHours());

        //when + then
        assertThatThrownBy(() -> service.registerBranch(OWNER_EMAIL, bad))
                .isInstanceOf(ServiceCategoryNotFoundException.class);
        assertThat(branchStub.inserted).isNull();
    }

    @Test
    public void test_registerBranch_without_owner_row_is_an_authentication_failure() {
        //given
        ownerStub.owner = null;

        //when + then
        assertThatThrownBy(() -> service.registerBranch(OWNER_EMAIL, BranchRegistrationValidatorTest.valid()))
                .isInstanceOf(AuthenticatedUserMissingException.class);
        assertThat(branchStub.inserted).isNull();
    }

    @Test
    public void test_registerBranch_bad_timezone_surfaces_as_domain_validation() {
        //given
        RegisterBranchCommand c = BranchRegistrationValidatorTest.valid();
        RegisterBranchCommand bad = new RegisterBranchCommand(c.name(), c.phoneNumber(), c.email(), "Mars/Olympus",
                c.address(), c.openingHours(), c.carBrandIds(), c.serviceBayTypes(), c.serviceBays(), c.equipmentTypes(),
                c.equipment(), c.roles(), c.employees(), c.services());

        //when + then
        assertThatThrownBy(() -> service.registerBranch(OWNER_EMAIL, bad))
                .isInstanceOf(com.hutnyk.carfix.exception.DomainObjectValidationException.class)
                .extracting("fieldName").isEqualTo("timezone");
    }

    @Test
    public void test_registerBranch_reuses_existing_region_and_city() {
        //given
        addressStub.regions.add(Region.of(5, "Masovian Voivodeship", CountryIso.PL));
        addressStub.cities.add(City.of(9, "Warsaw", 5, null, null));

        //when
        service.registerBranch(OWNER_EMAIL, BranchRegistrationValidatorTest.valid());

        //then
        assertThat(addressStub.inserted.getCityId()).isEqualTo(9);
        assertThat(addressStub.regions).hasSize(1);
        assertThat(addressStub.cities).hasSize(1);
    }

    @Test
    public void test_getMyBranchSummaries_resolves_owner_and_passes_clock_instant() {
        //given
        userStub.user = ownerUser();

        //when
        List<OwnerBranchSummaryView> result = service.getMyBranchSummaries(OWNER_EMAIL);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("AutoFix Mokotow");
        assertThat(ownerBranchStub.receivedOwnerId).isEqualTo(OWNER_ID);
        assertThat(ownerBranchStub.receivedNow).isEqualTo(NOW);
    }

    @Test
    public void test_getMyBranchSummaries_unknown_principal_throws_before_the_port() {
        //given
        userStub.user = null;

        //when + then
        assertThatThrownBy(() -> service.getMyBranchSummaries(OWNER_EMAIL))
                .isInstanceOf(AuthenticatedUserMissingException.class);
        assertThat(ownerBranchStub.receivedOwnerId).isNull();
    }

    @Test
    public void test_getMyBranch_returns_the_detail_view_for_the_resolved_owner() {
        //given
        userStub.user = ownerUser();

        //when
        OwnerBranchDetailView result = service.getMyBranch(OWNER_EMAIL, BRANCH_ID);

        //then
        assertThat(result).isSameAs(ownerBranchStub.detail);
        assertThat(ownerBranchStub.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(ownerBranchStub.receivedOwnerId).isEqualTo(OWNER_ID);
    }

    @Test
    public void test_getMyBranch_of_a_branch_owned_by_someone_else_is_not_found() {
        //given
        userStub.user = ownerUser();
        ownerBranchStub.detail = null;

        //when + then
        assertThatThrownBy(() -> service.getMyBranch(OWNER_EMAIL, BRANCH_ID))
                .isInstanceOf(BranchNotFoundException.class);
    }

    /* ---------- updateBranchOverview ---------- */

    private static UpdateBranchOverviewCommand overview() {
        return new UpdateBranchOverviewCommand(
                "AutoFix Wola", "  Renamed workshop.  ", CancellationPolicy.STRICT,
                new RegisterBranchAddressCommand("Wolska", "12", "3", "01-001", "Warsaw",
                        "Masovian Voivodeship", "PL",
                        new BigDecimal("52.230000"), new BigDecimal("20.980000"), "ChIJnew"),
                List.of(new RegisterBranchOpeningHoursCommand(
                                DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0), OpeningHoursMode.OPEN),
                        new RegisterBranchOpeningHoursCommand(
                                DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(13, 0), OpeningHoursMode.BY_APPOINTMENT)),
                List.of(new UpdateBranchOpeningHoursExceptionCommand(
                        LocalDate.of(2026, 12, 24), null, null, false, "Christmas Eve")),
                Set.of(1, 2));
    }

    @Test
    public void test_updateBranchOverview_rebuilds_the_branch_and_replaces_its_children() {
        //given
        userStub.user = ownerUser();

        //when
        OwnerBranchDetailView result = service.updateBranchOverview(OWNER_EMAIL, BRANCH_ID, overview());

        //then — the re-read view comes back
        assertThat(result).isSameAs(ownerBranchStub.detail);
        // edited fields change, server-owned ones are preserved
        Branch updated = branchStub.updated;
        assertThat(updated.getId().id()).isEqualTo(BRANCH_ID);
        assertThat(updated.getName()).isEqualTo("AutoFix Wola");
        assertThat(updated.getDescription()).isEqualTo("Renamed workshop.");
        assertThat(updated.getCancellationPolicy()).isEqualTo(CancellationPolicy.STRICT);
        assertThat(updated.getPhoneNumber()).isEqualTo("+48221234567");
        assertThat(updated.getEmail()).isEqualTo("contact@autofix.pl");
        assertThat(updated.getStatus()).isEqualTo(BranchStatus.ACTIVE);
        assertThat(updated.getTz()).isEqualTo(ZoneId.of("Europe/Warsaw"));
        assertThat(updated.getAddressId()).isEqualTo(55);
        assertThat(updated.getOwnerId()).isEqualTo(OWNER_ID);
        // address is updated in place, on the resolved city
        assertThat(addressStub.updated.getId()).isEqualTo(55);
        assertThat(addressStub.updated.getStreetName()).isEqualTo("Wolska");
        assertThat(addressStub.updated.getCityId()).isEqualTo(200);
        assertThat(addressStub.inserted).isNull();
        // children are replaced wholesale
        assertThat(branchStub.replacedHours).hasSize(2);
        assertThat(branchStub.replacedHours.get(1).getMode()).isEqualTo(OpeningHoursMode.BY_APPOINTMENT);
        assertThat(branchStub.replacedHours).allSatisfy(h -> assertThat(h.getBranchId().id()).isEqualTo(BRANCH_ID));
        assertThat(branchStub.replacedExceptions).singleElement().satisfies(e -> {
            assertThat(e.getDate()).isEqualTo(LocalDate.of(2026, 12, 24));
            assertThat(e.getIsOpen()).isFalse();
            assertThat(e.getReason()).isEqualTo("Christmas Eve");
            assertThat(e.getBranchId().id()).isEqualTo(BRANCH_ID);
        });
        assertThat(branchStub.replacedBrands).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void test_updateBranchOverview_blank_description_is_cleared() {
        //given
        userStub.user = ownerUser();
        UpdateBranchOverviewCommand c = overview();

        //when
        service.updateBranchOverview(OWNER_EMAIL, BRANCH_ID, new UpdateBranchOverviewCommand(c.name(), "   ",
                c.cancellationPolicy(), c.address(), c.openingHours(), c.openingHoursExceptions(), c.carBrandIds()));

        //then
        assertThat(branchStub.updated.getDescription()).isNull();
    }

    @Test
    public void test_updateBranchOverview_of_a_branch_owned_by_someone_else_writes_nothing() {
        //given
        userStub.user = ownerUser();
        ownerBranchStub.detail = null;

        //when + then
        assertThatThrownBy(() -> service.updateBranchOverview(OWNER_EMAIL, BRANCH_ID, overview()))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(branchStub.updated).isNull();
        assertThat(addressStub.updated).isNull();
    }

    @Test
    public void test_updateBranchOverview_unknown_car_brand_writes_nothing() {
        //given
        userStub.user = ownerUser();
        UpdateBranchOverviewCommand c = overview();

        //when + then
        assertThatThrownBy(() -> service.updateBranchOverview(OWNER_EMAIL, BRANCH_ID,
                new UpdateBranchOverviewCommand(c.name(), c.description(), c.cancellationPolicy(), c.address(),
                        c.openingHours(), c.openingHoursExceptions(), Set.of(1, 99))))
                .isInstanceOf(CarBrandNotFoundException.class);
        assertThat(branchStub.updated).isNull();
        assertThat(addressStub.updated).isNull();
    }

    @Test
    public void test_updateBranchOverview_duplicate_weekday_writes_nothing() {
        //given
        userStub.user = ownerUser();
        UpdateBranchOverviewCommand c = overview();

        //when + then
        assertThatThrownBy(() -> service.updateBranchOverview(OWNER_EMAIL, BRANCH_ID,
                new UpdateBranchOverviewCommand(c.name(), c.description(), c.cancellationPolicy(), c.address(),
                        List.of(c.openingHours().get(0), c.openingHours().get(0)),
                        c.openingHoursExceptions(), c.carBrandIds())))
                .isInstanceOf(InvalidBranchRegistrationException.class);
        assertThat(branchStub.updated).isNull();
        assertThat(addressStub.updated).isNull();
    }
}
