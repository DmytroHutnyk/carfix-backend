package com.hutnyk.carfix.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.review.ReviewPortIn;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.ReviewId;
import com.hutnyk.carfix.review.exception.ReviewAlreadyExistsException;
import com.hutnyk.carfix.review.exception.ReviewNotAllowedException;
import com.hutnyk.carfix.review.exception.ReviewedBookingNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

public class CustomerReviewControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final UUID BOOKING_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

    private static final class StubReviewPortIn implements ReviewPortIn {
        String receivedEmail;
        AddReviewCommand receivedCommand;
        RuntimeException toThrow;

        @Override
        public Review addReview(String customerEmail, AddReviewCommand command) {
            this.receivedEmail = customerEmail;
            this.receivedCommand = command;
            if (toThrow != null) {
                throw toThrow;
            }
            return Review.of(ReviewId.of(UUID.fromString("11111111-2222-3333-4444-555555555555")),
                    command.starsNumber(), command.contents(), command.bookingId());
        }
    }

    private final StubReviewPortIn stub = new StubReviewPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CustomerReviewController(stub))
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
        UserDetails principal = User.withUsername(EMAIL).password("irrelevant").roles("CUSTOMER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void addReviewReturns201WithBodyAndPassesTheCommand() throws Exception {
        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Great service\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.starsNumber").value(4))
                .andExpect(jsonPath("$.contents").value("Great service"))
                .andExpect(jsonPath("$.bookingId").value(BOOKING_ID.toString()));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedCommand.bookingId()).isEqualTo(BookingId.of(BOOKING_ID));
        assertThat(stub.receivedCommand.starsNumber()).isEqualTo(4);
        assertThat(stub.receivedCommand.contents()).isEqualTo("Great service");
    }

    @Test
    public void addReviewWithoutCommentIsAccepted() throws Exception {
        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.starsNumber").value(5));

        assertThat(stub.receivedCommand.contents()).isNull();
    }

    @Test
    public void addReviewRejectsOutOfRangeRating() throws Exception {
        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":6,\"comment\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());

        assertThat(stub.receivedCommand).isNull();
    }

    @Test
    public void addReviewRejectsMissingRating() throws Exception {
        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());

        assertThat(stub.receivedCommand).isNull();
    }

    @Test
    public void addReviewForForeignOrUnknownBookingIs404() throws Exception {
        stub.toThrow = new ReviewedBookingNotFoundException(BookingId.of(BOOKING_ID));

        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"x\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REVIEWED_BOOKING_NOT_FOUND"));
    }

    @Test
    public void addReviewForNotCompletedBookingIs422() throws Exception {
        stub.toThrow = new ReviewNotAllowedException(BookingId.of(BOOKING_ID), BookingStatus.SCHEDULED);

        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"x\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REVIEW_NOT_ALLOWED"));
    }

    @Test
    public void addReviewForAlreadyReviewedBookingIs409() throws Exception {
        stub.toThrow = new ReviewAlreadyExistsException(BookingId.of(BOOKING_ID));

        mockMvc.perform(post("/api/customer/bookings/{bookingId}/review", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_EXISTS"));
    }
}
