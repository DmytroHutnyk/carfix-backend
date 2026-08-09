package com.hutnyk.carfix.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.ServiceSuggestionView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSuggestionView;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.search.SearchPortOut;
import com.hutnyk.carfix.search.exception.InvalidSearchFilterException;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SearchServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId CUSTOMER_ID = UserId.genId();
    private static final UUID CAR_PROFILE_ID = UUID.randomUUID();
    private static final Integer BRAND_ID = 12;
    private static final String CITY = "Warsaw";

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
        public List<WorkshopSuggestionView> findWorkshopSuggestions(SearchSuggestionsQuery query, int limit) {
            suggestionCalls.add("workshops:" + query.q() + ":" + limit);
            this.receivedWorkshopQuery = query;
            return List.of(new WorkshopSuggestionView(UUID.randomUUID(), "TireMax"));
        }

        @Override
        public WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, Integer brandId) {
            this.receivedQuery = query;
            this.receivedBrandId = brandId;
            this.brandIdReceived = true;
            return WorkshopSearchPage.empty(query.page(), query.size());
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

    private final StubSearchPortOut searchPortOut = new StubSearchPortOut();
    private final StubCarProfilePortOut carProfilePortOut = new StubCarProfilePortOut();
    private final SearchService service =
            new SearchService(searchPortOut, new StubCustomerPortOut(), carProfilePortOut);

    private static WorkshopSearchQuery query(String q, String serviceName, Integer categoryId, UUID carProfileId) {
        return new WorkshopSearchQuery(q, serviceName, categoryId, CITY, null, null,
                null, null, null, carProfileId, 0, 20);
    }

    private static WorkshopSearchQuery geoQuery(String city, BigDecimal lat, BigDecimal lng, Double radiusKm) {
        return new WorkshopSearchQuery("tire", null, null, city, null, null,
                lat, lng, radiusKm, null, 0, 20);
    }

    private static SearchSuggestionsQuery suggestionsQuery(String q) {
        return new SearchSuggestionsQuery(q, null, null, null);
    }

    @Test
    public void test_suggestions_short_query_returns_empty_without_port_call() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery(" t "));

        //then
        assertThat(result.services()).isEmpty();
        assertThat(result.categories()).isEmpty();
        assertThat(result.workshops()).isEmpty();
        assertThat(searchPortOut.suggestionCalls).isEmpty();
    }

    @Test
    public void test_suggestions_null_query_returns_empty_without_port_call() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery(null));

        //then
        assertThat(result.services()).isEmpty();
        assertThat(searchPortOut.suggestionCalls).isEmpty();
    }

    @Test
    public void test_suggestions_trims_and_queries_three_groups() {
        //when
        SearchSuggestionsView result = service.getSuggestions(suggestionsQuery("  tire "));

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
        service.getSuggestions(suggestionsQuery("  tire "));

        //then
        assertThat(searchPortOut.receivedServiceQ).isEqualTo("tire");
        assertThat(searchPortOut.receivedWorkshopQuery.q()).isEqualTo("tire");
    }

    @Test
    public void test_suggestions_passes_location_to_workshops_only() {
        //given
        SearchSuggestionsQuery located =
                new SearchSuggestionsQuery(" tire ", CITY, "Masovian Voivodeship", "Poland");

        //when
        service.getSuggestions(located);

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
        SearchSuggestionsQuery blankLocation = new SearchSuggestionsQuery("tire", CITY, "  ", null);

        //when
        service.getSuggestions(blankLocation);

        //then
        assertThat(searchPortOut.receivedWorkshopQuery.city()).isEqualTo(CITY);
        assertThat(searchPortOut.receivedWorkshopQuery.voivodeship()).isNull();
        assertThat(searchPortOut.receivedWorkshopQuery.country()).isNull();
    }

    @Test
    public void test_suggestions_without_location_still_queries_all_groups() {
        //when
        service.getSuggestions(suggestionsQuery("tire"));

        //then
        assertThat(searchPortOut.receivedWorkshopQuery.city()).isNull();
        assertThat(searchPortOut.suggestionCalls)
                .containsExactly("services:tire:5", "categories:tire:5", "workshops:tire:5");
    }

    @Test
    public void test_search_throws_when_no_filter_given() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query(null, null, null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_throws_when_two_filters_given() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query("tire", "Oil Change", null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_throws_when_blank_filter_counts_as_absent() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query(null, "   ", null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_throws_when_free_text_too_short() {
        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(query(" t ", null, null, null), null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_throws_when_no_location_given() {
        //given
        WorkshopSearchQuery noLocation = geoQuery(null, null, null, null);

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(noLocation, null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.receivedQuery).isNull();
    }

    @Test
    public void test_search_throws_when_location_is_blank() {
        //given
        WorkshopSearchQuery blankLocation = new WorkshopSearchQuery("tire", null, null, "  ", " ", "",
                null, null, null, null, 0, 20);

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(blankLocation, null))
                .isInstanceOf(InvalidSearchFilterException.class);
    }

    @Test
    public void test_search_accepts_voivodeship_only() {
        //given
        WorkshopSearchQuery voivodeshipOnly = new WorkshopSearchQuery("tire", null, null,
                null, "Masovian Voivodeship", null, null, null, null, null, 0, 20);

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
                null, null, "Poland", null, null, null, null, 0, 20);

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
                null, null, null, null, 0, 99);

        //when + then
        assertThatThrownBy(() -> service.searchWorkshops(oversized, null))
                .isInstanceOf(InvalidSearchFilterException.class);
        assertThat(searchPortOut.receivedQuery).isNull();
    }

    @Test
    public void test_search_rejects_negative_page() {
        //given
        WorkshopSearchQuery negativePage = new WorkshopSearchQuery("tire", null, null, CITY, null, null,
                null, null, null, null, -1, 20);

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
}
