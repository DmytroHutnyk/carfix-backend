package com.hutnyk.carfix.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.search.SearchPortIn;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.AvailableStartView;
import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
import com.hutnyk.carfix.in.search.query.SearchEchoView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.ServiceSuggestionView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSuggestionView;
import com.hutnyk.carfix.search.exception.InvalidSearchFilterException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class SearchControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static WorkshopResultView card(Double distanceKm, BigDecimal rating, Integer reviewCount,
                                           List<AvailableStartView> starts) {
        return new WorkshopResultView(
                BRANCH_ID, "AutoFix Mokotow", "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), distanceKm,
                rating, reviewCount,
                List.of(new MatchedServiceView(9, "Brake pads replacement",
                        new BigDecimal("150.00"), (short) 60, "Brakes")),
                "Europe/Warsaw", starts);
    }

    private static final class StubSearchPortIn implements SearchPortIn {
        SearchSuggestionsQuery receivedSuggestionsQuery;
        String receivedSuggestionsPrincipalEmail;
        WorkshopSearchQuery receivedQuery;
        String receivedPrincipalEmail;
        boolean searchCalled;
        RuntimeException toThrow;
        Double distanceKm = 3.2;
        BigDecimal rating = new BigDecimal("4.7");
        Integer reviewCount = 236;
        List<AvailableStartView> starts = null;
        AvailabilityWindow echoAvailability = null;

        @Override
        public SearchSuggestionsView getSuggestions(SearchSuggestionsQuery query, String principalEmail) {
            this.receivedSuggestionsQuery = query;
            this.receivedSuggestionsPrincipalEmail = principalEmail;
            return new SearchSuggestionsView(
                    List.of(new ServiceSuggestionView("Tire Replacement", "Tires")),
                    List.of(new CategorySuggestionView(4, "Tires")),
                    List.of(new WorkshopSuggestionView(BRANCH_ID, "TireMax")));
        }

        @Override
        public WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, String principalEmail) {
            this.receivedQuery = query;
            this.receivedPrincipalEmail = principalEmail;
            this.searchCalled = true;
            if (toThrow != null) {
                throw toThrow;
            }
            return new WorkshopSearchPage(List.of(card(distanceKm, rating, reviewCount, starts)), 0, 20, 1, 1,
                    new SearchEchoView("brake", null, null, null, "Warsaw", null, null, echoAvailability));
        }
    }

    private final StubSearchPortIn stub = new StubSearchPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SearchController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate() {
        UserDetails principal = User.withUsername(EMAIL).password("irrelevant").roles("CUSTOMER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    public void test_suggestions_returns_three_groups() throws Exception {
        mockMvc.perform(get("/api/search/suggestions").param("q", "tire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services[0].name").value("Tire Replacement"))
                .andExpect(jsonPath("$.services[0].categoryName").value("Tires"))
                .andExpect(jsonPath("$.categories[0].categoryId").value(4))
                .andExpect(jsonPath("$.categories[0].name").value("Tires"))
                .andExpect(jsonPath("$.workshops[0].branchId").value(BRANCH_ID.toString()));

        assertThat(stub.receivedSuggestionsQuery.q()).isEqualTo("tire");
    }

    @Test
    public void test_suggestions_passes_location_to_port() throws Exception {
        mockMvc.perform(get("/api/search/suggestions")
                        .param("q", "tire")
                        .param("city", "Warsaw")
                        .param("voivodeship", "Masovian Voivodeship")
                        .param("country", "Poland"))
                .andExpect(status().isOk());

        assertThat(stub.receivedSuggestionsQuery.city()).isEqualTo("Warsaw");
        assertThat(stub.receivedSuggestionsQuery.voivodeship()).isEqualTo("Masovian Voivodeship");
        assertThat(stub.receivedSuggestionsQuery.country()).isEqualTo("Poland");
    }

    @Test
    public void test_suggestions_defaults_to_no_location() throws Exception {
        mockMvc.perform(get("/api/search/suggestions").param("q", "tire"))
                .andExpect(status().isOk());

        assertThat(stub.receivedSuggestionsQuery.city()).isNull();
        assertThat(stub.receivedSuggestionsQuery.voivodeship()).isNull();
        assertThat(stub.receivedSuggestionsQuery.country()).isNull();
        assertThat(stub.receivedSuggestionsQuery.carProfileId()).isNull();
        assertThat(stub.receivedSuggestionsPrincipalEmail).isNull();
    }

    @Test
    public void test_suggestions_passes_car_profile_and_principal() throws Exception {
        authenticate();
        UUID carProfileId = UUID.randomUUID();

        mockMvc.perform(get("/api/search/suggestions")
                        .param("q", "tire")
                        .param("carProfileId", carProfileId.toString()))
                .andExpect(status().isOk());

        assertThat(stub.receivedSuggestionsQuery.carProfileId()).isEqualTo(carProfileId);
        assertThat(stub.receivedSuggestionsPrincipalEmail).isEqualTo(EMAIL);
    }

    @Test
    public void test_workshops_returns_page_with_matched_services() throws Exception {
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw")
                        .param("lat", "52.23")
                        .param("lng", "21.01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].name").value("AutoFix Mokotow"))
                .andExpect(jsonPath("$.content[0].city").value("Warsaw"))
                .andExpect(jsonPath("$.content[0].distanceKm").value(3.2))
                .andExpect(jsonPath("$.content[0].rating").value(4.7))
                .andExpect(jsonPath("$.content[0].reviewCount").value(236))
                .andExpect(jsonPath("$.content[0].matchedServices[0].price").value(150.00))
                .andExpect(jsonPath("$.content[0].matchedServices[0].durationMinutes").value(60))
                .andExpect(jsonPath("$.content[0].matchedServices[0].categoryName").value("Brakes"))
                .andExpect(jsonPath("$.echo.q").value("brake"))
                .andExpect(jsonPath("$.echo.city").value("Warsaw"))
                .andExpect(jsonPath("$.echo.categoryName").doesNotExist());
    }

    @Test
    public void test_workshops_without_coordinates_returns_null_distance() throws Exception {
        stub.distanceKm = null;

        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].distanceKm").doesNotExist());

        assertThat(stub.receivedQuery.lat()).isNull();
        assertThat(stub.receivedQuery.lng()).isNull();
        assertThat(stub.receivedQuery.radiusKm()).isNull();
    }

    @Test
    public void test_workshops_unrated_branch_serializes_null_rating() throws Exception {
        stub.rating = null;
        stub.reviewCount = null;

        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").doesNotExist())
                .andExpect(jsonPath("$.content[0].reviewCount").doesNotExist());
    }

    @Test
    public void test_workshops_applies_paging_defaults_and_anonymous_principal() throws Exception {
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk());

        assertThat(stub.receivedQuery.page()).isZero();
        assertThat(stub.receivedQuery.size()).isEqualTo(20);
        assertThat(stub.receivedPrincipalEmail).isNull();
    }

    @Test
    public void test_workshops_passes_all_filters_and_authenticated_principal() throws Exception {
        authenticate();
        UUID carProfileId = UUID.randomUUID();

        mockMvc.perform(get("/api/search/workshops")
                        .param("categoryId", "4")
                        .param("city", "Warsaw")
                        .param("voivodeship", "Masovian Voivodeship")
                        .param("country", "Poland")
                        .param("lat", "52.23")
                        .param("lng", "21.01")
                        .param("radiusKm", "25")
                        .param("carProfileId", carProfileId.toString())
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        assertThat(stub.receivedQuery.categoryId()).isEqualTo(4);
        assertThat(stub.receivedQuery.city()).isEqualTo("Warsaw");
        assertThat(stub.receivedQuery.voivodeship()).isEqualTo("Masovian Voivodeship");
        assertThat(stub.receivedQuery.country()).isEqualTo("Poland");
        assertThat(stub.receivedQuery.lat()).isEqualByComparingTo("52.23");
        assertThat(stub.receivedQuery.lng()).isEqualByComparingTo("21.01");
        assertThat(stub.receivedQuery.radiusKm()).isEqualTo(25.0);
        assertThat(stub.receivedQuery.carProfileId()).isEqualTo(carProfileId);
        assertThat(stub.receivedQuery.page()).isEqualTo(2);
        assertThat(stub.receivedQuery.size()).isEqualTo(5);
        assertThat(stub.receivedPrincipalEmail).isEqualTo(EMAIL);
    }

    @Test
    public void test_workshops_maps_invalid_filter_to_400() throws Exception {
        stub.toThrow = new InvalidSearchFilterException("exactly one filter");

        mockMvc.perform(get("/api/search/workshops").param("city", "Warsaw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SEARCH_FILTER"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    public void test_workshops_rejects_oversized_page_before_the_port() throws Exception {
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw")
                        .param("size", "99"))
                .andExpect(status().isBadRequest());

        assertThat(stub.searchCalled).isFalse();
    }

    @Test
    public void test_workshops_rejects_latitude_out_of_range_before_the_port() throws Exception {
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw")
                        .param("lat", "99")
                        .param("lng", "21.01"))
                .andExpect(status().isBadRequest());

        assertThat(stub.searchCalled).isFalse();
    }

    @Test
    public void test_suggestions_missing_query_is_rejected_before_the_port() throws Exception {
        mockMvc.perform(get("/api/search/suggestions"))
                .andExpect(status().isBadRequest());

        assertThat(stub.receivedSuggestionsQuery).isNull();
    }

    @Test
    public void test_workshops_forwards_availability_params() throws Exception {
        mockMvc.perform(get("/api/search/workshops")
                        .param("serviceName", "Oil and filter change").param("city", "Warsaw")
                        .param("from", "2026-08-18").param("to", "2026-08-20")
                        .param("timeFrom", "13:00").param("timeTo", "19:00"))
                .andExpect(status().isOk());

        assertThat(stub.receivedQuery.availability()).isEqualTo(new AvailabilityWindow(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 20), LocalTime.of(13, 0), LocalTime.of(19, 0)));
    }

    @Test
    public void test_workshops_without_availability_params_passes_null_window() throws Exception {
        mockMvc.perform(get("/api/search/workshops").param("serviceName", "Oil and filter change"))
                .andExpect(status().isOk());

        assertThat(stub.receivedQuery.availability()).isNull();
    }

    @Test
    public void test_workshops_partial_availability_params_still_reach_the_port() throws Exception {
        mockMvc.perform(get("/api/search/workshops").param("serviceName", "Oil").param("from", "2026-08-18"))
                .andExpect(status().isOk());

        assertThat(stub.receivedQuery.availability())
                .isEqualTo(new AvailabilityWindow(LocalDate.of(2026, 8, 18), null, null, null));
    }

    @Test
    public void test_workshops_malformed_time_or_date_is_400() throws Exception {
        mockMvc.perform(get("/api/search/workshops").param("serviceName", "Oil")
                        .param("from", "2026-08-18").param("to", "2026-08-18").param("timeFrom", "1pm"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/search/workshops").param("serviceName", "Oil")
                        .param("from", "18.08.2026").param("to", "2026-08-18"))
                .andExpect(status().isBadRequest());

        assertThat(stub.searchCalled).isFalse();
    }

    @Test
    public void test_workshops_response_carries_tz_next_available_starts_and_echo_window() throws Exception {
        stub.starts = List.of(new AvailableStartView(LocalDate.of(2026, 8, 18), LocalTime.of(13, 15)),
                new AvailableStartView(LocalDate.of(2026, 8, 19), LocalTime.of(9, 0)));
        stub.echoAvailability = new AvailabilityWindow(LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 20),
                LocalTime.of(13, 0), null);

        mockMvc.perform(get("/api/search/workshops").param("serviceName", "Oil")
                        .param("from", "2026-08-18").param("to", "2026-08-20").param("timeFrom", "13:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tz").value("Europe/Warsaw"))
                .andExpect(jsonPath("$.content[0].nextAvailableStarts[0].date").value("2026-08-18"))
                .andExpect(jsonPath("$.content[0].nextAvailableStarts[0].startTime").value("13:15"))
                .andExpect(jsonPath("$.content[0].nextAvailableStarts[1].startTime").value("09:00"))
                .andExpect(jsonPath("$.echo.availability.from").value("2026-08-18"))
                .andExpect(jsonPath("$.echo.availability.to").value("2026-08-20"))
                .andExpect(jsonPath("$.echo.availability.timeFrom").value("13:00"))
                .andExpect(jsonPath("$.echo.availability.timeTo").doesNotExist());
    }

    @Test
    public void test_workshops_response_has_no_starts_and_null_echo_window_without_filter() throws Exception {
        mockMvc.perform(get("/api/search/workshops").param("q", "brake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tz").value("Europe/Warsaw"))
                .andExpect(jsonPath("$.content[0].nextAvailableStarts").doesNotExist())
                .andExpect(jsonPath("$.echo.availability").doesNotExist());
    }
}
