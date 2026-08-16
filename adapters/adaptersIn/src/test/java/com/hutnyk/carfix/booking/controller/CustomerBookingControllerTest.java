package com.hutnyk.carfix.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.booking.exception.CarProfileAlreadyBookedException;
import com.hutnyk.carfix.booking.exception.InvalidBookingRequestException;
import com.hutnyk.carfix.booking.exception.SlotNotAvailableException;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.booking.BookingPortIn;
import com.hutnyk.carfix.in.booking.commands.CreateBookingCommand;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CustomerBookingControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final UUID BOOKING_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID CAR_PROFILE_ID = UUID.randomUUID();

    private static final String CREATE_BODY = "{"
            + "\"branchId\":\"" + BRANCH_ID + "\","
            + "\"carProfileId\":\"" + CAR_PROFILE_ID + "\","
            + "\"serviceIds\":[11,27],"
            + "\"date\":\"2030-06-12\","
            + "\"startTime\":\"10:00\"}";

    private static BookingView view(BookingStatus status) {
        return new BookingView(
                BOOKING_ID,
                LocalDate.of(2030, 6, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                status,
                Instant.parse("2030-06-11T08:00:00Z"),
                BRANCH_ID,
                "SpeedCare Wola",
                "+48123456789",
                "wola@speedcare.pl",
                "Górczewska",
                "110",
                "Warszawa",
                CAR_PROFILE_ID,
                "Weekend Car",
                "BMW",
                "X5",
                "KR 67890",
                List.of(
                        new BookingServiceView("Air filter replacement", new BigDecimal("160.00")),
                        new BookingServiceView("Diagnostics", new BigDecimal("150.00"))),
                new BigDecimal("310.00"));
    }

    private static final class StubBookingPortIn implements BookingPortIn {
        String receivedEmail;
        UUID receivedBookingId;
        CreateBookingCommand receivedCommand;
        RuntimeException toThrow;

        @Override
        public List<BookingView> getMyBookings(String customerEmail) {
            this.receivedEmail = customerEmail;
            return List.of(view(BookingStatus.SCHEDULED));
        }

        @Override
        public BookingView createBooking(String customerEmail, CreateBookingCommand command) {
            this.receivedEmail = customerEmail;
            this.receivedCommand = command;
            if (toThrow != null) throw toThrow;
            return view(BookingStatus.SCHEDULED);
        }

        @Override
        public BookingView cancelBooking(String customerEmail, UUID bookingId) {
            this.receivedEmail = customerEmail;
            this.receivedBookingId = bookingId;
            if (toThrow != null) throw toThrow;
            return view(BookingStatus.CANCELLED);
        }
    }

    private final StubBookingPortIn stub = new StubBookingPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CustomerBookingController(stub))
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
    public void getMyBookingsReturnsFullCardShape() throws Exception {
        mockMvc.perform(get("/api/customer/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$[0].reference").value("BK-A1B2C3D4"))
                .andExpect(jsonPath("$[0].date").value("2030-06-12"))
                .andExpect(jsonPath("$[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("11:30:00"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].safeCancelUntil").value("2030-06-11T08:00:00Z"))
                .andExpect(jsonPath("$[0].branch.name").value("SpeedCare Wola"))
                .andExpect(jsonPath("$[0].branch.phoneNumber").value("+48123456789"))
                .andExpect(jsonPath("$[0].branch.city").value("Warszawa"))
                .andExpect(jsonPath("$[0].vehicle.brandName").value("BMW"))
                .andExpect(jsonPath("$[0].vehicle.plates").value("KR 67890"))
                .andExpect(jsonPath("$[0].services[0].name").value("Air filter replacement"))
                .andExpect(jsonPath("$[0].services[0].price").value(160.00))
                .andExpect(jsonPath("$[0].totalPrice").value(310.00));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
    }

    @Test
    public void cancelReturnsUpdatedBooking() throws Exception {
        mockMvc.perform(post("/api/customer/bookings/{id}/cancel", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(stub.receivedBookingId).isEqualTo(BOOKING_ID);
    }

    @Test
    public void cancelUnknownBookingIs404WithCode() throws Exception {
        stub.toThrow = new BookingNotFoundException(BOOKING_ID);

        mockMvc.perform(post("/api/customer/bookings/{id}/cancel", BOOKING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOKING_NOT_FOUND"));
    }

    @Test
    public void cancelNonScheduledBookingIs409WithCode() throws Exception {
        stub.toThrow = new BookingCancellationNotAllowedException(BOOKING_ID, BookingStatus.COMPLETED);

        mockMvc.perform(post("/api/customer/bookings/{id}/cancel", BOOKING_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOKING_CANCELLATION_NOT_ALLOWED"));
    }

    @Test
    public void createReturns201WithTheBookingCardAndPassesTheCommand() throws Exception {
        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.reference").value("BK-A1B2C3D4"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.services[0].name").value("Air filter replacement"))
                .andExpect(jsonPath("$.totalPrice").value(310.00));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedCommand.branchId()).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedCommand.carProfileId()).isEqualTo(CAR_PROFILE_ID);
        assertThat(stub.receivedCommand.serviceIds()).containsExactly(11, 27);
        assertThat(stub.receivedCommand.date()).isEqualTo(LocalDate.of(2030, 6, 12));
        assertThat(stub.receivedCommand.startTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    public void createRejectsMissingFieldsWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branchId\":\"" + BRANCH_ID + "\",\"serviceIds\":[],\"startTime\":\"10:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.carProfileId").exists())
                .andExpect(jsonPath("$.errors.serviceIds").exists())
                .andExpect(jsonPath("$.errors.date").exists());

        assertThat(stub.receivedCommand).isNull();
    }

    @Test
    public void createRejectsMoreThanThreeServicesBeforeReachingTheService() throws Exception {
        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("[11,27]", "[1,2,3,4]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.serviceIds").exists());

        assertThat(stub.receivedCommand).isNull();
    }

    @Test
    public void createMapsOffGridStartTo400WithTheField() throws Exception {
        stub.toThrow = new InvalidBookingRequestException("startTime", "startTime must be on the 15-minute grid");

        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("10:00", "10:20")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BOOKING_REQUEST"))
                .andExpect(jsonPath("$.errors.startTime").exists());
    }

    @Test
    public void createMapsSlotNotAvailableTo409() throws Exception {
        stub.toThrow = new SlotNotAvailableException(LocalDate.of(2030, 6, 12), LocalTime.of(10, 0));

        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_NOT_AVAILABLE"))
                .andExpect(jsonPath("$.detail").value("Slot 2030-06-12 10:00 is no longer available"));
    }

    @Test
    public void createMapsCarProfileAlreadyBookedTo409() throws Exception {
        stub.toThrow = new CarProfileAlreadyBookedException(
                CarProfileId.of(CAR_PROFILE_ID), LocalDate.of(2030, 6, 13), LocalTime.of(9, 0), LocalTime.of(10, 0));

        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CAR_PROFILE_ALREADY_BOOKED"))
                .andExpect(jsonPath("$.errors.carProfileId").exists());
    }

    @Test
    public void createMapsForeignCarProfileTo404() throws Exception {
        stub.toThrow = new CarProfileNotFoundException(CAR_PROFILE_ID);

        mockMvc.perform(post("/api/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CAR_PROFILE_NOT_FOUND"));
    }
}
