package com.hutnyk.carfix.in.booking.query;

import com.hutnyk.carfix.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BookingView(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BookingStatus status,
        Instant safeCancelUntil,
        UUID branchId,
        String branchName,
        String branchPhoneNumber,
        String branchEmail,
        String streetName,
        String buildingNumber,
        String city,
        UUID carProfileId,
        String carProfileName,
        String brandName,
        String modelName,

        //Nullable
        String plates,
        List<BookingServiceView> services,
        BigDecimal totalPrice
) {}
