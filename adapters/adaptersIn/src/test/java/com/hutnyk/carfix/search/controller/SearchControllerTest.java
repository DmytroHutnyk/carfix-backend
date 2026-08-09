package com.hutnyk.carfix.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.search.SearchPortIn;
import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SearchControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static WorkshopResultView card(Double distanceKm) {
        return new WorkshopResultView(
                BRANCH_ID, "AutoFix Mokotow", "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), distanceKm,
                List.of(new MatchedServiceView(9, "Brake pads replacement",
                        new BigDecimal("150.00"), (short) 60, "Brakes")));
    }

    private static final class StubSearchPortIn implements SearchPortIn {
        SearchSuggestionsQuery receivedSuggestionsQuery;
        WorkshopSearchQuery receivedQuery;
        String receivedPrincipalEmail;
        boolean searchCalled;
        RuntimeException toThrow;
        Double distanceKm = 3.2;

        @Override
        public SearchSuggestionsView getSuggestions(SearchSuggestionsQuery query) {
            this.receivedSuggestionsQuery = query;
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
            return new WorkshopSearchPage(List.of(card(distanceKm)), 0, 20, 1, 1);
        }
    }

    private final StubSearchPortIn stub = new StubSearchPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SearchController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
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
        //when + then
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
        //when
        mockMvc.perform(get("/api/search/suggestions")
                        .param("q", "tire")
                        .param("city", "Warsaw")
                        .param("voivodeship", "Masovian Voivodeship")
                        .param("country", "Poland"))
                .andExpect(status().isOk());

        //then
        assertThat(stub.receivedSuggestionsQuery.city()).isEqualTo("Warsaw");
        assertThat(stub.receivedSuggestionsQuery.voivodeship()).isEqualTo("Masovian Voivodeship");
        assertThat(stub.receivedSuggestionsQuery.country()).isEqualTo("Poland");
    }

    @Test
    public void test_suggestions_defaults_to_no_location() throws Exception {
        //when
        mockMvc.perform(get("/api/search/suggestions").param("q", "tire"))
                .andExpect(status().isOk());

        //then
        assertThat(stub.receivedSuggestionsQuery.city()).isNull();
        assertThat(stub.receivedSuggestionsQuery.voivodeship()).isNull();
        assertThat(stub.receivedSuggestionsQuery.country()).isNull();
    }

    @Test
    public void test_workshops_returns_page_with_matched_services() throws Exception {
        //when + then
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
                .andExpect(jsonPath("$.content[0].matchedServices[0].price").value(150.00))
                .andExpect(jsonPath("$.content[0].matchedServices[0].durationMinutes").value(60))
                .andExpect(jsonPath("$.content[0].matchedServices[0].categoryName").value("Brakes"));
    }

    @Test
    public void test_workshops_without_coordinates_returns_null_distance() throws Exception {
        //given
        stub.distanceKm = null;

        //when + then
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
    public void test_workshops_applies_paging_defaults_and_anonymous_principal() throws Exception {
        //when
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw"))
                .andExpect(status().isOk());

        //then
        assertThat(stub.receivedQuery.page()).isZero();
        assertThat(stub.receivedQuery.size()).isEqualTo(20);
        assertThat(stub.receivedPrincipalEmail).isNull();
    }

    @Test
    public void test_workshops_passes_all_filters_and_authenticated_principal() throws Exception {
        //given
        authenticate();
        UUID carProfileId = UUID.randomUUID();

        //when
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

        //then
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
        //given
        stub.toThrow = new InvalidSearchFilterException("exactly one filter");

        //when + then
        mockMvc.perform(get("/api/search/workshops").param("city", "Warsaw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SEARCH_FILTER"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    public void test_workshops_rejects_oversized_page_before_the_port() throws Exception {
        //when + then
        mockMvc.perform(get("/api/search/workshops")
                        .param("q", "brake")
                        .param("city", "Warsaw")
                        .param("size", "99"))
                .andExpect(status().isBadRequest());

        assertThat(stub.searchCalled).isFalse();
    }

    @Test
    public void test_workshops_rejects_latitude_out_of_range_before_the_port() throws Exception {
        //when + then
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
        //when + then
        mockMvc.perform(get("/api/search/suggestions"))
                .andExpect(status().isBadRequest());

        assertThat(stub.receivedSuggestionsQuery).isNull();
    }
}
