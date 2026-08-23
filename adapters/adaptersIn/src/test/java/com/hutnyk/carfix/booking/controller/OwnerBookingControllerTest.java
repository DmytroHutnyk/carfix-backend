package com.hutnyk.carfix.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.exception.BookingNoShowNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.booking.OwnerBookingPortIn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

public class OwnerBookingControllerTest {

    private static final String EMAIL = "boss@speedcare.pl";
    private static final UUID BOOKING_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

    private static final class StubOwnerBookingPortIn implements OwnerBookingPortIn {
        String receivedEmail;
        UUID receivedBookingId;
        RuntimeException toThrow;

        @Override
        public void markNoShow(String ownerEmail, UUID bookingId) {
            this.receivedEmail = ownerEmail;
            this.receivedBookingId = bookingId;
            if (toThrow != null) throw toThrow;
        }
    }

    private final StubOwnerBookingPortIn stub = new StubOwnerBookingPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerBookingController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
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
    public void markNoShowAnswers204AndPassesThePrincipalAndId() throws Exception {
        mockMvc.perform(post("/api/owner/bookings/{id}/no-show", BOOKING_ID))
                .andExpect(status().isNoContent());

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBookingId).isEqualTo(BOOKING_ID);
    }

    @Test
    public void markNoShowOfAForeignOrUnknownBookingIs404WithCode() throws Exception {
        stub.toThrow = new BookingNotFoundException(BOOKING_ID);

        mockMvc.perform(post("/api/owner/bookings/{id}/no-show", BOOKING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOKING_NOT_FOUND"));
    }

    @Test
    public void markNoShowRefusedByTheDomainIs409WithCode() throws Exception {
        stub.toThrow = new BookingNoShowNotAllowedException(BOOKING_ID, BookingStatus.SCHEDULED);

        mockMvc.perform(post("/api/owner/bookings/{id}/no-show", BOOKING_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOKING_NO_SHOW_NOT_ALLOWED"));
    }
}
