package com.hutnyk.carfix.branch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OwnerBranchControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID REVIEW_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

    private static OwnerBranchSummaryView summary() {
        return new OwnerBranchSummaryView(
                BRANCH_ID, "AutoSerwis Kowalski Mokotow", BranchStatus.ACTIVE,
                "Pulawska", "145", "Warsaw",
                new BigDecimal("4.7"), 3,
                true, 12, 7, 3, 5,
                List.of(new BranchReviewView(
                        REVIEW_ID, 5, "Great", Instant.parse("2026-08-10T10:00:00Z"), "Anna", "Nowak")));
    }

    private static final class StubOwnerBranchPortIn implements OwnerBranchPortIn {
        String receivedEmail;
        List<OwnerBranchSummaryView> toReturn = List.of(summary());
        RuntimeException toThrow;

        @Override
        public List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail) {
            this.receivedEmail = ownerEmail;
            if (toThrow != null) throw toThrow;
            return toReturn;
        }
    }

    private final StubOwnerBranchPortIn stub = new StubOwnerBranchPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerBranchController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @BeforeEach
    public void authenticate() {
        UserDetails principal = User.withUsername(EMAIL).password("irrelevant").roles("OWNER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getMyBranchesReturnsSummaryShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchId").value(BRANCH_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("AutoSerwis Kowalski Mokotow"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].streetName").value("Pulawska"))
                .andExpect(jsonPath("$[0].buildingNumber").value("145"))
                .andExpect(jsonPath("$[0].city").value("Warsaw"))
                .andExpect(jsonPath("$[0].rating").value(4.7))
                .andExpect(jsonPath("$[0].reviewCount").value(3))
                .andExpect(jsonPath("$[0].openNow").value(true))
                .andExpect(jsonPath("$[0].bookingsToday").value(12))
                .andExpect(jsonPath("$[0].completedToday").value(7))
                .andExpect(jsonPath("$[0].employeesOnDutyToday").value(3))
                .andExpect(jsonPath("$[0].employeesTotal").value(5))
                .andExpect(jsonPath("$[0].latestReviews[0].reviewId").value(REVIEW_ID.toString()))
                .andExpect(jsonPath("$[0].latestReviews[0].starsNumber").value(5))
                .andExpect(jsonPath("$[0].latestReviews[0].contents").value("Great"))
                .andExpect(jsonPath("$[0].latestReviews[0].createdAt").value("2026-08-10T10:00:00Z"))
                .andExpect(jsonPath("$[0].latestReviews[0].customerName").value("Anna"))
                .andExpect(jsonPath("$[0].latestReviews[0].customerSurname").value("Nowak"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
    }

    @Test
    public void getMyBranchesReturnsEmptyArrayForOwnerWithoutBranches() throws Exception {
        stub.toReturn = List.of();

        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getMyBranchesMissingPrincipalRowIs401WithCode() throws Exception {
        stub.toThrow = AuthenticatedUserMissingException.forEmail(EMAIL);

        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATED_USER_MISSING"))
                .andExpect(jsonPath("$.status").value(401));
    }
}
