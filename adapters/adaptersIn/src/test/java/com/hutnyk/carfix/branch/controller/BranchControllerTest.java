package com.hutnyk.carfix.branch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.branch.BranchPortIn;
import com.hutnyk.carfix.in.branch.query.BranchBrandView;
import com.hutnyk.carfix.in.branch.query.BranchOpeningHoursView;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchServiceCategoryView;
import com.hutnyk.carfix.in.branch.query.BranchServiceView;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class BranchControllerTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID REVIEW_ID = UUID.randomUUID();

    private static BranchView view() {
        return new BranchView(
                BRANCH_ID, "AutoFix Mokotow", "+48221234567", "contact@autofix.pl",
                "Professional automotive service center.", "Free cancellation up to 24 hours.",
                new BigDecimal("4.7"), 236,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), null, "Europe/Warsaw",
                List.of(new BranchBrandView(1, "BMW")),
                List.of(new BranchOpeningHoursView(
                        DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), OpeningHoursMode.OPEN)),
                List.of(new BranchServiceCategoryView(3, "Brake Services", List.of(
                        new BranchServiceView(9, "Brake inspection", null,
                                (short) 30, new BigDecimal("120.00"))))));
    }

    private static final class StubBranchPortIn implements BranchPortIn {
        BranchReviewsQuery receivedQuery;
        RuntimeException toThrow;
        boolean getReviewsCalled;

        @Override
        public BranchView getBranch(UUID branchId) {
            if (toThrow != null) {
                throw toThrow;
            }
            return view();
        }

        @Override
        public BranchReviewsPage getReviews(BranchReviewsQuery query) {
            this.getReviewsCalled = true;
            this.receivedQuery = query;
            if (toThrow != null) {
                throw toThrow;
            }
            return new BranchReviewsPage(
                    List.of(new BranchReviewView(REVIEW_ID, 5, "Excellent service!",
                            Instant.parse("2026-01-15T18:30:00Z"), "Jan", "Kowalski")),
                    query.page(), query.size(), 1, 1);
        }
    }

    private final StubBranchPortIn stub = new StubBranchPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new BranchController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @Test
    public void test_getBranch_returns_full_shape() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(BRANCH_ID.toString()))
                .andExpect(jsonPath("$.name").value("AutoFix Mokotow"))
                .andExpect(jsonPath("$.rating").value(4.7))
                .andExpect(jsonPath("$.cancellationPolicy").value("Free cancellation up to 24 hours."))
                .andExpect(jsonPath("$.brands[0].name").value("BMW"))
                .andExpect(jsonPath("$.openingHours[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.openingHours[0].startTime").value("09:00"))
                .andExpect(jsonPath("$.openingHours[0].mode").value("OPEN"))
                .andExpect(jsonPath("$.serviceCategories[0].name").value("Brake Services"))
                .andExpect(jsonPath("$.serviceCategories[0].services[0].price").value(120.00));
    }

    @Test
    public void test_getBranch_maps_not_found_to_404() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(get("/api/branches/" + BRANCH_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    public void test_getReviews_returns_page_and_passes_raw_params() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/reviews").param("sort", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].starsNumber").value(5))
                .andExpect(jsonPath("$.content[0].customerName").value("Jan"))
                .andExpect(jsonPath("$.content[0].customerSurname").value("Kowalski"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-01-15T18:30:00Z"))
                .andExpect(jsonPath("$.totalElements").value(1));

        assertThat(stub.receivedQuery.branchId()).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedQuery.sort()).isEqualTo("newest");
        assertThat(stub.receivedQuery.page()).isEqualTo(0);
        assertThat(stub.receivedQuery.size()).isEqualTo(10);
    }

    @Test
    public void test_getReviews_rejects_oversized_page_before_the_port() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/reviews").param("size", "99"))
                .andExpect(status().isBadRequest());

        assertThat(stub.getReviewsCalled).isFalse();
    }

    @Test
    public void test_getReviews_maps_invalid_sort_to_400() throws Exception {
        stub.toThrow = new InvalidReviewsSortException("bogus");

        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/reviews").param("sort", "bogus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REVIEWS_SORT"));
    }
}
