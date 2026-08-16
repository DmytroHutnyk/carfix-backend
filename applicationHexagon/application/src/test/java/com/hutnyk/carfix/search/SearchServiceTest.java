package com.hutnyk.carfix.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.AvailableStartView;
import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.ServiceSuggestionView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSuggestionView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.search.SearchPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.search.exception.InvalidSearchFilterException;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SearchServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId CUSTOMER_ID = UserId.genId();
    private static final UUID CAR_PROFILE_ID = UUID.randomUUID();
    private static final Integer BRAND_ID = 12;
    private static final String CITY = "Warsaw";
    private static final String SERVICE_NAME = "Oil and filter change";
    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final Clock CLOCK = Clock.fixed(
            TODAY.atTime(10, 7).atZone(WARSAW).toInstant(), ZoneId.systemDefault());

    private static Customer customer() {
        return Customer.of(
                User.builder()
                        .id(CUSTOMER_ID)
                        .name("John")
                        .surname("Doe")
                        .phoneNumber(new PhoneNumber("+48", "123456789"))
                        .email(EMAIL)
                        .role(UserRole.CUSTOMER)
                        .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                        .dateOfBirth(LocalDate.of(1990, 5, 1))
                        .addressId(null)
                        .build(),
                CustomerStatus.ACTIVE);
    }

    private static CarProfileView carProfileView() {
        return new CarProfileView(CAR_PROFILE_ID, "Weekend Car", null, null, null, null,
                CUSTOMER_ID.id(), null, BRAND_ID, "BMW", 3, "X5", 7, "xDrive40i");
    }

    private static final class StubSearchPortOut implements SearchPortOut {
        final List<String> suggestionCalls = new ArrayList<>();
        WorkshopSearchQuery receivedQuery;
        Integer receivedBrandId;
        boolean brandIdReceived;
        String receivedServiceQ;
        SearchSuggestionsQuery receivedWorkshopQuery;
        Integer receivedWorkshopBrandId;
        boolean workshopBrandIdReceived;
        List<WorkshopResultView> candidates = List.of();
        WorkshopSearchQuery receivedCandidateQuery;
        Integer receivedCandidateLimit;
        boolean candidatesCalled;

        @Override
        public List<ServiceSuggestionView> findServiceSuggestions(String q, int limit) {
            suggestionCalls.add("services:" + q + ":" + limit);
            this.receivedServiceQ = q;
            return List.of(new ServiceSuggestionView("Tire replacement", "Tires"));
        }

        @Override
        public List<CategorySuggestionView> findCategorySuggestions(String q, int limit) {
            suggestionCalls.add("categories:" + q + ":" + limit);
            return List.of(new CategorySuggestionView(4, "Tires"));
        }

        @Override
        public List<WorkshopSuggestionView> findWorkshopSuggestions(
                SearchSuggestionsQuery query, Integer brandId, int limit) {
            suggestionCalls.add("workshops:" + query.q() + ":" + limit);
            this.receivedWorkshopQuery = query;
            this.receivedWorkshopBrandId = brandId;
            this.workshopBrandIdReceived = true;
            return List.of(new WorkshopSuggestionView(UUID.randomUUID(), "TireMax"));
        }

        @Override
        public WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, Integer brandId) {
            this.receivedQuery = query;
            this.receivedBrandId = brandId;
            this.brandIdReceived = true;
            return WorkshopSearchPage.empty(query.page(), query.size());
        }

        @Override
        public List<WorkshopResultView> findAvailabilityCandidates(
                WorkshopSearchQuery query, Integer brandId, int limit) {
            this.receivedCandidateQuery = query;
            this.receivedCandidateLimit = limit;
            this.candidatesCalled = true;
            return candidates;
        }

        Optional<String> categoryName = Optional.of("Brakes");
        Integer receivedCategoryNameId;

        @Override
        public Optional<String> findCategoryName(Integer categoryId) {
            this.receivedCategoryNameId = categoryId;
            return categoryName;
        }
    }

    private static final class StubCustomerPortOut implements CustomerPortOut {
        @Override
        public Customer insertCustomer(Customer customer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Customer loadCustomerByUsername(String email) {
            return customer();
        }
    }

    private static final class StubCarProfilePortOut implements CarProfilePortOut {
        Optional<CarProfileView> found = Optional.of(carProfileView());
        UUID receivedProfileId;
        UUID receivedCustomerId;

        @Override
        public List<CarProfileView> findAllByCustomerId(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CarProfileView> findByIdAndCustomerId(UUID profileId, UUID customerId) {
            this.receivedProfileId = profileId;
            this.receivedCustomerId = customerId;
            return found;
        }

        @Override
        public boolean existsByIdAndCustomerId(UUID profileId, UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarProfile insert(CarProfile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarProfile update(CarProfile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(UUID profileId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
            return toReturn;
        }
    
        @Override
        public Service insert(Service service) {
            throw new UnsupportedOperationException();
        }
}

    private static final class StubAvailabilityPortOut implements AvailabilityPortOut {
        final List<ServiceBay> bays = new ArrayList<>();
        final Map<BranchId, List<EmployeeCandidateView>> employees = new HashMap<>();
        final List<Equipment> equipment = new ArrayList<>();
        final List<ServiceBayAvailability> bayAvailability = new ArrayList<>();
        final List<ServiceBayBooking> bayOccupancy = new ArrayList<>();
        final List<EmployeeAvailability> employeeAvailability = new ArrayList<>();
        final List<EmployeeBooking> employeeOccupancy = new ArrayList<>();
        final List<EquipmentAvailability> equipmentAvailability = new ArrayList<>();
        final List<EquipmentBooking> equipmentOccupancy = new ArrayList<>();

        @Override
        public Map<BranchId, List<ServiceBay>> loadActiveBaysByBranch(Collection<BranchId> branchIds) {
            return bays.stream().collect(Collectors.groupingBy(ServiceBay::getBranchId));
        }

        @Override
        public Map<BranchId, List<EmployeeCandidateView>> loadActiveEmployeesByBranch(Collection<BranchId> branchIds) {
            return employees;
        }

        @Override
        public Map<BranchId, List<Equipment>> loadActiveEquipmentByBranch(Collection<BranchId> branchIds) {
            return equipment.stream().collect(Collectors.groupingBy(Equipment::getBranchId));
        }

        @Override
        public List<ServiceBayAvailability> loadBayAvailability(Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            return bayAvailability;
        }

        @Override
        public List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            return bayOccupancy;
        }

        @Override
        public List<EmployeeAvailability> loadEmployeeAvailability(Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeAvailability;
        }

        @Override
        public List<EmployeeBooking> loadEmployeeOccupancy(Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeOccupancy;
        }

        @Override
        public List<EquipmentAvailability> loadEquipmentAvailability(Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return equipmentAvailability;
        }

        @Override
        public List<EquipmentBooking> loadEquipmentOccupancy(Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return equipmentOccupancy;
        }

        @Override
        public List<ServiceBay> loadActiveBays(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Equipment> loadActiveEquipment(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OpeningHours> loadOpeningHours(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
            throw new UnsupportedOperationException();
        }
    }

    private final StubSearchPortOut searchPortOut = new StubSearchPortOut();
    private final StubCarProfilePortOut carProfilePortOut = new StubCarProfilePortOut();
    private final StubServicePortOut servicePortOut = new StubServicePortOut();
    private final StubAvailabilityPortOut availabilityPortOut = new StubAvailabilityPortOut();
    private final SearchService service = new SearchService(searchPortOut, new StubCustomerPortOut(), carProfilePortOut,
            servicePortOut, availabilityPortOut, CLOCK);

    private static WorkshopSearchQuery query(String q, String serviceName, Integer categoryId, UUID carProfileId) {
        return new WorkshopSearchQuery(q, serviceName, categoryId, CITY, null, null,
                null, null, null, carProfileId, 0, 20, null, null, null);
    }

    private static WorkshopSearchQuery geoQuery(String city, BigDecimal lat, BigDecimal lng, Double radiusKm) {
        return new WorkshopSearchQuery("tire", null, null, city, null, null,
                lat, lng, radiusKm, null, 0, 20, null, null, null);
    }

    private static SearchSuggestionsQuery suggestionsQuery(String q) {
        return new SearchSuggestionsQuery(q, null, null, null, null);
    }

    private static WorkshopSearchQuery availabilityQuery(String serviceName, String q, LocalDate from, LocalDate to,
                                                         LocalTime timeFrom, LocalTime timeTo, int page, int size) {
        return new WorkshopSearchQuery(q, serviceName, null, CITY, null, null, null, null, null, null, page, size,
                null, null, new AvailabilityWindow(from, to, timeFrom, timeTo));
    }

    /* A candidate branch that layer 2 will keep: lift bay + mechanic + service, all free tomorrow 09-12 */
    private WorkshopResultView availableCandidate(UUID branchId, int serviceId, int bayId, UUID mechanicId) {
        List<Service> services = new ArrayList<>(servicePortOut.toReturn);
        services.add(Service.of(serviceId, SERVICE_NAME, null, (short) 60, BigDecimal.TEN, ServiceStatus.ACTIVE,
                BranchId.of(branchId), 1, Set.of(1), List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(10))), List.of()));
        servicePortOut.toReturn = services;
        availabilityPortOut.bays.add(ServiceBay.of(bayId, "Bay", ServiceBayStatus.ACTIVE, null, 1, BranchId.of(branchId)));
        availabilityPortOut.employees.put(BranchId.of(branchId), List.of(new EmployeeCandidateView(mechanicId, Set.of(10))));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(bayId,
                TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 1, bayId));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(bayId,
                TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 2, UserId.of(mechanicId)));
        return new WorkshopResultView(branchId, "Branch", "Street", "1", CITY, new BigDecimal("52.2"), new BigDecimal("21.0"),
                null, null, null, List.of(new MatchedServiceView(serviceId, SERVICE_NAME, BigDecimal.TEN, (short) 60, "Engine")),
                "Europe/Warsaw", null);
    }

    @Test
    public void test_suggestions_short_query_returns_empty_without_port_call() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery(" t "), null);

        //then
        assertThat(result.services()).isEmpty();
        assertThat(result.categories()).isEmpty();
        assertThat(result.workshops()).isEmpty();
        assertThat(searchPortOut.suggestionCalls).isEmpty();
    }

    @Test
    public void test_suggestions_null_query_returns_empty_without_port_call() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery(null), null);

        //then
        assertThat(result.services()).isEmpty();
        assertThat(searchPortOut.suggestionCalls).isEmpty();
    }

    @Test
    public void test_suggestions_trims_and_queries_three_groups() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery("  tire "), null);

        //then
        assertThat(searchPortOut.suggestionCalls)
                .containsExactly("services:tire:5", "categories:tire:5", "workshops:tire:5");
        assertThat(result.services()).hasSize(1);
        assertThat(result.categories()).hasSize(1);
        assertThat(result.workshops()).hasSize(1);
    }

    @Test
    public void test_suggestions_normalizes_query_before_delegating() {
        //when
        service.getSuggestions(suggestionsQuery("  tire "), null);

        //then
        assertThat(searchPortOut.receivedServiceQ).isEqualTo("tire");
        assertThat(searchPortOut.receivedWorkshopQuery.q()).isEqualTo("tire");
    }

    @Test
    public void test_suggestions_passes_location_to_workshops_only() {
        //given
        SearchSuggestionsQuery located =
                new SearchSuggestionsQuery(" tire ", CITY, "Masovian Voivodeship", "Poland", null);

        //when
        service.getSuggestions(located, null);

        //then
        assertThat(searchPortOut.receivedServiceQ).isEqualTo("tire");
        assertThat(searchPortOut.suggestionCalls).contains("categories:tire:5");
        assertThat(searchPortOut.receivedWorkshopQuery.city()).isEqualTo(CITY);
        assertThat(searchPortOut.receivedWorkshopQuery.voivodeship()).isEqualTo("Masovian Voivodeship");
        assertThat(searchPortOut.receivedWorkshopQuery.country()).isEqualTo("Poland");
    }

    @Test
    public void test_suggestions_normalizes_blank_location_to_null() {
        //given
        SearchSuggestionsQuery blankLocation = new SearchSuggestionsQuery("tire", CITY, "  ", null, null);

        //when
        service.getSuggestions(blankLocation, null);

        //then
        assertThat(searchPortOut.receivedWorkshopQuery.city()).isEqualTo(CITY);
        assertThat(searchPortOut.receivedWorkshopQuery.voivodeship()).isNull();
        assertThat(searchPortOut.receivedWorkshopQuery.country()).isNull();
    }

    @Test
    public void test_suggestions_without_location_still_queries_all_groups() {
        //when
        service.getSuggestions(suggestionsQuery("tire"), null);

        //then
        assertThat(searchPortOut.receivedWorkshopQuery.city()).isNull();
        assertThat(searchPortOut.suggestionCalls)
                .containsExactly("services:tire:5", "categories:tire:5", "workshops:tire:5");
    }

    @Test
    public void test_suggestions_without_car_profile_passes_null_brand() {
        //when
        service.getSuggestions(suggestionsQuery("tire"), null);

        //then
        assertThat(searchPortOut.workshopBrandIdReceived).isTrue();
        assertThat(searchPortOut.receivedWorkshopBrandId).isNull();
    }

    @Test
    public void test_suggestions_with_owned_car_profile_passes_its_brand_to_workshops_only() {
        //given
        SearchSuggestionsQuery withCar =
                new SearchSuggestionsQuery("tire", null, null, null, CAR_PROFILE_ID);

        //when
        service.getSuggestions(withCar, EMAIL);

        //then
        assertThat(carProfilePortOut.receivedProfileId).isEqualTo(CAR_PROFILE_ID);
        assertThat(carProfilePortOut.receivedCustomerId).isEqualTo(CUSTOMER_ID.id());
        assertThat(searchPortOut.receivedWorkshopBrandId).isEqualTo(BRAND_ID);
        assertThat(searchPortOut.suggestionCalls)
                .containsExactly("services:tire:5", "categories:tire:5", "workshops:tire:5");
    }

    @Test
    public void test_suggestions_with_car_profile_and_anonymous_caller_throws_not_found() {
        //given
        SearchSuggestionsQuery withCar =
                new SearchSuggestionsQuery("tire", null, null, null, CAR_PROFILE_ID);

        //when + then
        assertThatThrownBy(() -> service.getSuggestions(withCar, null))
                .isInstanceOf(CarProfileNotFoundException.class);
    }

    @Test
    public void test_suggestions_with_car_profile_not_owned_throws_not_found() {
        //given
        carProfilePortOut.found = Optional.empty();
        SearchSuggestionsQuery withCar =
                new SearchSuggestionsQuery("tire", null, null, null, CAR_PROFILE_ID);

        //when + then
        assertThatThrownBy(() -> service.getSuggestions(withCar, EMAIL))
                .isInstanceOf(CarProfileNotFoundException.class);
    }

    @Test
    public void test_search_without_any_filter_is_browse_mode() {
        //given
        WorkshopSearchQuery browse = new WorkshopSearchQuery(null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null, null);

        //when
        service.searchWorkshops(browse, null);

        //then
        assertThat(searchPortOut.receivedQuery.q()).isNull();
        assertThat(searchPortOut.receivedQuery.serviceName()).isNull();
        assertThat(searchPortOut.receivedQuery.categoryId()).isNull();
        assertThat(searchPortOut.receivedQuery.city()).isNull();
    }

    @Test
    public void test_search_throws_when_two_filters_given() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query("tire", "Oil Change", null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_blank_text_filter_falls_back_to_browse() {
        //when
        service.searchWorkshops(query(null, "   ", null, null), null);

        //then
        assertThat(searchPortOut.receivedQuery.serviceName()).isNull();
        assertThat(searchPortOut.receivedQuery.city()).isEqualTo(CITY);
    }

    @Test
    public void test_search_throws_when_free_text_too_short() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query(" t ", null, null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_without_location_passes_through() {
        //given
        WorkshopSearchQuery noLocation = new WorkshopSearchQuery("tire", null, null, null, null, null,
                null, null, null, null, 0, 20, null, null, null);

        //when
        service.searchWorkshops(noLocation, null);

        //then
        assertThat(searchPortOut.receivedQuery.city()).isNull();
        assertThat(searchPortOut.receivedQuery.voivodeship()).isNull();
        assertThat(searchPortOut.receivedQuery.country()).isNull();
    }

    @Test
    public void test_search_blank_location_normalized_to_null_passes_through() {
        //given
        WorkshopSearchQuery blankLocation = new WorkshopSearchQuery("tire", null, null, "  ", " ", "",
                null, null, null, null, 0, 20, null, null, null);

        //when
        service.searchWorkshops(blankLocation, null);

        //then
        assertThat(searchPortOut.receivedQuery.city()).isNull();
    }

    @Test
    public void test_search_accepts_voivodeship_only() {
        //given
        WorkshopSearchQuery voivodeshipOnly = new WorkshopSearchQuery("tire", null, null,
                null, "Masovian Voivodeship", null, null, null, null, null, 0, 20, null, null, null);

        //when
        service.searchWorkshops(voivodeshipOnly, null);

        //then
        assertThat(searchPortOut.receivedQuery.voivodeship()).isEqualTo("Masovian Voivodeship");
        assertThat(searchPortOut.receivedQuery.city()).isNull();
    }

    @Test
    public void test_search_accepts_country_only() {
        //given
        WorkshopSearchQuery countryOnly = new WorkshopSearchQuery("tire", null, null,
                null, null, "Poland", null, null, null, null, 0, 20, null, null, null);

        //when
        service.searchWorkshops(countryOnly, null);

        //then
        assertThat(searchPortOut.receivedQuery.country()).isEqualTo("Poland");
    }

    @Test
    public void test_search_throws_when_radius_given_without_coordinates() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(geoQuery(CITY, null, null, 10.0), null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.receivedQuery).isNull();
    }

    @Test
    public void test_search_throws_when_only_one_coordinate_given() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(
                geoQuery(CITY, new BigDecimal("52.23"), null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_without_coordinates_passes_nulls_through() {
        //when
        service.searchWorkshops(query(null, null, 7, null), null);

        //then
        assertThat(searchPortOut.receivedQuery.city()).isEqualTo(CITY);
        assertThat(searchPortOut.receivedQuery.lat()).isNull();
        assertThat(searchPortOut.receivedQuery.lng()).isNull();
        assertThat(searchPortOut.receivedQuery.radiusKm()).isNull();
    }

    @Test
    public void test_search_with_coordinates_and_radius_passes_them_through() {
        //when
        service.searchWorkshops(
                geoQuery(CITY, new BigDecimal("52.23"), new BigDecimal("21.01"), 5.0), null);

        //then
        assertThat(searchPortOut.receivedQuery.lat()).isEqualByComparingTo("52.23");
        assertThat(searchPortOut.receivedQuery.lng()).isEqualByComparingTo("21.01");
        assertThat(searchPortOut.receivedQuery.radiusKm()).isEqualTo(5.0);
    }

    @Test
    public void test_search_rejects_page_size_over_the_maximum() {
        //given
        WorkshopSearchQuery oversized = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                null, null, null, null, 0, 99, null, null, null);

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(oversized, null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.receivedQuery).isNull();
    }

    @Test
    public void test_search_rejects_negative_page() {
        //given
        WorkshopSearchQuery negativePage = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                null, null, null, null, -1, 20, null, null, null);

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(negativePage, null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_rejects_radius_over_the_maximum() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(
                geoQuery(CITY, new BigDecimal("52.23"), new BigDecimal("21.01"), 5000.0), null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.receivedQuery).isNull();
    }

    @Test
    public void test_search_rejects_latitude_out_of_range() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(
                geoQuery(CITY, new BigDecimal("99"), new BigDecimal("21.01"), null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_without_car_profile_passes_null_brand() {
        //when
        WorkshopSearchPage result = service.searchWorkshops(query(null, null, 7, null), null);

        //then
        assertThat(result.totalElements()).isZero();
        assertThat(searchPortOut.brandIdReceived).isTrue();
        assertThat(searchPortOut.receivedBrandId).isNull();
        assertThat(searchPortOut.receivedQuery.categoryId()).isEqualTo(7);
    }

    @Test
    public void test_search_normalizes_free_text_before_delegating() {
        //when
        service.searchWorkshops(query("  tire  ", null, null, null), null);

        //then
        assertThat(searchPortOut.receivedQuery.q()).isEqualTo("tire");
        assertThat(searchPortOut.receivedQuery.serviceName()).isNull();
    }

    @Test
    public void test_search_attaches_echo_of_normalized_filters() {
        //when
        WorkshopSearchPage result = service.searchWorkshops(query("  tire  ", null, null, null), null);

        //then
        assertThat(result.echo().q()).isEqualTo("tire");
        assertThat(result.echo().city()).isEqualTo(CITY);
        assertThat(result.echo().categoryId()).isNull();
        assertThat(result.echo().categoryName()).isNull();
        assertThat(searchPortOut.receivedCategoryNameId).isNull();
    }

    @Test
    public void test_search_category_echo_resolves_category_name() {
        //when
        WorkshopSearchPage result = service.searchWorkshops(query(null, null, 7, null), null);

        //then
        assertThat(searchPortOut.receivedCategoryNameId).isEqualTo(7);
        assertThat(result.echo().categoryId()).isEqualTo(7);
        assertThat(result.echo().categoryName()).isEqualTo("Brakes");
    }

    @Test
    public void test_search_unknown_category_echoes_null_name() {
        //given
        searchPortOut.categoryName = Optional.empty();

        //when
        WorkshopSearchPage result = service.searchWorkshops(query(null, null, 99, null), null);

        //then
        assertThat(result.echo().categoryName()).isNull();
    }

    @Test
    public void test_search_browse_echo_is_all_null() {
        //given
        WorkshopSearchQuery browse = new WorkshopSearchQuery(null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null, null);

        //when
        WorkshopSearchPage result = service.searchWorkshops(browse, null);

        //then
        assertThat(result.echo().q()).isNull();
        assertThat(result.echo().serviceName()).isNull();
        assertThat(result.echo().categoryName()).isNull();
        assertThat(result.echo().city()).isNull();
    }

    @Test
    public void test_search_with_car_profile_and_anonymous_caller_throws_not_found() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query("tire", null, null, CAR_PROFILE_ID), null))
                .isInstanceOf(CarProfileNotFoundException.class);
    }

    @Test
    public void test_search_with_car_profile_not_owned_throws_not_found() {
        //given
        carProfilePortOut.found = Optional.empty();

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query("tire", null, null, CAR_PROFILE_ID), EMAIL))
                .isInstanceOf(CarProfileNotFoundException.class);
    }

    @Test
    public void test_search_with_owned_car_profile_passes_its_brand() {
        //when
        service.searchWorkshops(query("tire", null, null, CAR_PROFILE_ID), EMAIL);

        //then
        assertThat(carProfilePortOut.receivedProfileId).isEqualTo(CAR_PROFILE_ID);
        assertThat(carProfilePortOut.receivedCustomerId).isEqualTo(CUSTOMER_ID.id());
        assertThat(searchPortOut.receivedBrandId).isEqualTo(BRAND_ID);
    }

    @Test
    void sort_defaults_to_distance_when_coordinates_are_present() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);
        WorkshopSearchQuery query = geoQuery(CITY, BigDecimal.valueOf(52.2), BigDecimal.valueOf(21.0), null);

        //when
        service.searchWorkshops(query, null);

        //then
        assertThat(searchPort.receivedQuery.sort()).isEqualTo(WorkshopSearchQuery.SORT_DISTANCE);
    }

    @Test
    void sort_defaults_to_name_without_coordinates() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);

        //when
        service.searchWorkshops(query("tire", null, null, null), null);

        //then
        assertThat(searchPort.receivedQuery.sort()).isEqualTo(WorkshopSearchQuery.SORT_NAME);
    }

    @Test
    void explicit_name_sort_wins_over_coordinates() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);
        WorkshopSearchQuery query = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                BigDecimal.valueOf(52.2), BigDecimal.valueOf(21.0), null, null, 0, 20, "NAME", null, null);

        //when
        service.searchWorkshops(query, null);

        //then
        assertThat(searchPort.receivedQuery.sort()).isEqualTo(WorkshopSearchQuery.SORT_NAME);
    }

    @Test
    void distance_sort_without_coordinates_is_rejected() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);
        WorkshopSearchQuery query = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                null, null, null, null, 0, 20, "distance", null, null);

        //when / then
        assertThatThrownBy(() -> service.searchWorkshops(query, null))
                .isInstanceOf(InvalidSearchFilterException.class)
                .hasMessageContaining("lat and lng");
    }

    @Test
    void unknown_sort_value_is_rejected() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);
        WorkshopSearchQuery query = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                null, null, null, null, 0, 20, "rating", null, null);

        //when / then
        assertThatThrownBy(() -> service.searchWorkshops(query, null))
                .isInstanceOf(InvalidSearchFilterException.class)
                .hasMessageContaining("sort");
    }

    @Test
    void pinned_branch_id_is_passed_through_untouched() {
        //given
        StubSearchPortOut searchPort = new StubSearchPortOut();
        SearchService service = new SearchService(searchPort, new StubCustomerPortOut(), new StubCarProfilePortOut(),
                servicePortOut, availabilityPortOut, CLOCK);
        UUID pinned = UUID.randomUUID();
        WorkshopSearchQuery query = new WorkshopSearchQuery("kowalski", null, null, CITY, null, null,
                null, null, null, null, 0, 20, null, pinned, null);

        //when
        service.searchWorkshops(query, null);

        //then
        assertThat(searchPort.receivedQuery.pinnedBranchId()).isEqualTo(pinned);
    }

    @Test
    public void test_availability_without_service_name_rejected() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(null, "tire", TOMORROW, TOMORROW, null, null, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.candidatesCalled).isFalse();
        assertThat(searchPortOut.brandIdReceived).isFalse();
    }

    @Test
    public void test_availability_from_without_to_rejected() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, null, null, null, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_availability_to_before_from_rejected() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TODAY, null, null, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_availability_longer_than_seven_days_rejected_seven_accepted() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW.plusDays(7), null, null, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);

        service.searchWorkshops(availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW.plusDays(6), null, null, 0, 20), null);
        assertThat(searchPortOut.candidatesCalled).isTrue();
    }

    @Test
    public void test_time_window_without_dates_rejected() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, null, null, LocalTime.of(13, 0), null, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_time_from_not_before_time_to_rejected_single_bound_accepted() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, LocalTime.of(15, 0), LocalTime.of(13, 0), 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);

        service.searchWorkshops(availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, LocalTime.of(13, 0), null, 0, 20), null);
        assertThat(searchPortOut.candidatesCalled).isTrue();
    }

    @Test
    public void test_time_to_at_midnight_rejected() {
        assertThatThrownBy(() -> service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, null, LocalTime.MIDNIGHT, 0, 20), null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.candidatesCalled).isFalse();
    }

    @Test
    public void test_all_null_window_means_no_availability_filter() {
        //when
        service.searchWorkshops(availabilityQuery(SERVICE_NAME, null, null, null, null, null, 0, 20), null);

        //then
        assertThat(searchPortOut.candidatesCalled).isFalse();
        assertThat(searchPortOut.brandIdReceived).isTrue();
        assertThat(searchPortOut.receivedQuery.availability()).isNull();
    }

    @Test
    public void test_availability_path_filters_candidates_and_pages_in_memory() {
        //given
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        searchPortOut.candidates = List.of(
                availableCandidate(a, 1, 100, UUID.randomUUID()),
                availableCandidate(b, 2, 200, UUID.randomUUID()),
                availableCandidate(c, 3, 300, UUID.randomUUID()));

        //when
        WorkshopSearchPage first = service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, null, null, 0, 2), null);
        WorkshopSearchPage second = service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, null, null, 1, 2), null);
        WorkshopSearchPage beyond = service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, null, null, 5, 2), null);

        //then
        assertThat(searchPortOut.brandIdReceived).isFalse();
        assertThat(searchPortOut.receivedCandidateLimit).isEqualTo(100);
        assertThat(searchPortOut.receivedCandidateQuery.availability())
                .isEqualTo(new AvailabilityWindow(TOMORROW, TOMORROW, null, null));
        assertThat(first.content()).extracting(WorkshopResultView::branchId).containsExactly(a, b);
        assertThat(first.totalElements()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.page()).isEqualTo(0);
        assertThat(first.size()).isEqualTo(2);
        assertThat(first.content().getFirst().nextAvailableStarts())
                .containsExactly(new AvailableStartView(TOMORROW, LocalTime.of(9, 0)),
                        new AvailableStartView(TOMORROW, LocalTime.of(9, 15)),
                        new AvailableStartView(TOMORROW, LocalTime.of(9, 30)));
        assertThat(first.echo().availability()).isEqualTo(new AvailabilityWindow(TOMORROW, TOMORROW, null, null));
        assertThat(first.echo().serviceName()).isEqualTo(SERVICE_NAME);
        assertThat(second.content()).extracting(WorkshopResultView::branchId).containsExactly(c);
        assertThat(beyond.content()).isEmpty();
        assertThat(beyond.totalElements()).isEqualTo(3);
    }

    @Test
    public void test_availability_path_drops_unavailable_candidate_from_totals() {
        //given
        UUID a = UUID.randomUUID();
        WorkshopResultView kept = availableCandidate(a, 1, 100, UUID.randomUUID());
        WorkshopResultView noResources = new WorkshopResultView(UUID.randomUUID(), "Empty", "Street", "2", CITY,
                new BigDecimal("52.2"), new BigDecimal("21.0"), null, null, null,
                List.of(new MatchedServiceView(9, SERVICE_NAME, BigDecimal.TEN, (short) 60, "Engine")), "Europe/Warsaw", null);
        searchPortOut.candidates = List.of(noResources, kept);

        //when
        WorkshopSearchPage page = service.searchWorkshops(
                availabilityQuery(SERVICE_NAME, null, TOMORROW, TOMORROW, null, null, 0, 20), null);

        //then
        assertThat(page.content()).extracting(WorkshopResultView::branchId).containsExactly(a);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
    }
}
