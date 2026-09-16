package com.hutnyk.carfix.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.booking.exception.InvalidBranchBookingQueryException;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.booking.OwnerBranchBookingPortIn;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingCarView;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingCustomerView;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingEmployeeView;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingServiceView;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingView;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class OwnerBranchBookingControllerTest {

    private static final String EMAIL = "boss@speedcare.pl";
    private static final UUID BRANCH_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

    private static OwnerBranchBookingView sampleView() {
        return new OwnerBranchBookingView(
                "BK-A1B2C3D4",
                "COMPLETED",
                LocalDate.of(2026, 9, 8),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                new OwnerBranchBookingCustomerView("John Doe", "+48123456789", "john@example.com"),
                new OwnerBranchBookingCarView("BMW", "X5", "KR 67890"),
                List.of(new OwnerBranchBookingServiceView("Diagnostics", 90, new BigDecimal("150.00"))),
                "Bay 1",
                List.of(new OwnerBranchBookingEmployeeView("Anna Kowalska", "Mechanic")),
                List.of("Lift", "Scanner"),
                90,
                LocalDateTime.of(2026, 9, 7, 12, 0, 0));
    }

    private static final class StubPortIn implements OwnerBranchBookingPortIn {
        String receivedEmail;
        UUID receivedBranchId;
        LocalDate receivedFrom;
        LocalDate receivedTo;
        List<OwnerBranchBookingView> toReturn = List.of(sampleView());
        RuntimeException toThrow;

        @Override
        public List<OwnerBranchBookingView> getBranchBookings(String ownerEmail, UUID branchId, LocalDate from, LocalDate to) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedFrom = from;
            this.receivedTo = to;
            if (toThrow != null) {
                throw toThrow;
            }
            return toReturn;
        }
    }

    private final StubPortIn stub = new StubPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerBranchBookingController(stub))
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
    public void returnsRangeBookingsInTheWireShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/{branchId}/bookings", BRANCH_ID)
                        .param("from", "2026-09-08").param("to", "2026-09-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("BK-A1B2C3D4"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].date").value("2026-09-08"))
                .andExpect(jsonPath("$[0].start").value("09:00:00"))
                .andExpect(jsonPath("$[0].end").value("10:30:00"))
                .andExpect(jsonPath("$[0].customer.name").value("John Doe"))
                .andExpect(jsonPath("$[0].customer.phone").value("+48123456789"))
                .andExpect(jsonPath("$[0].customer.email").value("john@example.com"))
                .andExpect(jsonPath("$[0].car.brand").value("BMW"))
                .andExpect(jsonPath("$[0].car.model").value("X5"))
                .andExpect(jsonPath("$[0].car.plate").value("KR 67890"))
                .andExpect(jsonPath("$[0].services[0].name").value("Diagnostics"))
                .andExpect(jsonPath("$[0].services[0].durationMinutes").value(90))
                .andExpect(jsonPath("$[0].services[0].price").value(150.00))
                .andExpect(jsonPath("$[0].bay").value("Bay 1"))
                .andExpect(jsonPath("$[0].employees[0].name").value("Anna Kowalska"))
                .andExpect(jsonPath("$[0].employees[0].role").value("Mechanic"))
                .andExpect(jsonPath("$[0].equipment[0]").value("Lift"))
                .andExpect(jsonPath("$[0].totalDurationMinutes").value(90))
                .andExpect(jsonPath("$[0].createdAt").value("2026-09-07T12:00:00"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedFrom).isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(stub.receivedTo).isEqualTo(LocalDate.of(2026, 9, 14));
    }

    @Test
    public void fromOnlyDefaultsToEqualsFrom() throws Exception {
        mockMvc.perform(get("/api/owner/branches/{branchId}/bookings", BRANCH_ID).param("from", "2026-09-08"))
                .andExpect(status().isOk());

        assertThat(stub.receivedFrom).isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(stub.receivedTo).isEqualTo(LocalDate.of(2026, 9, 8));
    }

    @Test
    public void toBeforeFromIs400() throws Exception {
        stub.toThrow = new InvalidBranchBookingQueryException("to must not be before from");

        mockMvc.perform(get("/api/owner/branches/{branchId}/bookings", BRANCH_ID)
                        .param("from", "2026-09-08").param("to", "2026-09-07"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BRANCH_BOOKING_QUERY"));
    }

    @Test
    public void spanTooLargeIs400() throws Exception {
        stub.toThrow = new InvalidBranchBookingQueryException("date range must be at most 92 days");

        mockMvc.perform(get("/api/owner/branches/{branchId}/bookings", BRANCH_ID)
                        .param("from", "2026-01-01").param("to", "2026-04-03"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BRANCH_BOOKING_QUERY"));
    }

    @Test
    public void foreignOrUnknownBranchIs404() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(get("/api/owner/branches/{branchId}/bookings", BRANCH_ID).param("from", "2026-09-08"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
    }
}
