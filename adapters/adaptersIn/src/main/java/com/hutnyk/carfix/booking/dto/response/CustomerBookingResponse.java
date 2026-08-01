package com.hutnyk.carfix.booking.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CustomerBookingResponse(
        UUID bookingId,
        String reference,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        Instant safeCancelUntil,
        BookingBranchResponse branch,
        BookingVehicleResponse vehicle,
        List<BookingServiceResponse> services,
        BigDecimal totalPrice
) {}
