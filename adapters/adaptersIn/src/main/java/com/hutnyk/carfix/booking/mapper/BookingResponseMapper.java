package com.hutnyk.carfix.booking.mapper;

import com.hutnyk.carfix.booking.dto.response.BookingBranchResponse;
import com.hutnyk.carfix.booking.dto.response.BookingServiceResponse;
import com.hutnyk.carfix.booking.dto.response.BookingVehicleResponse;
import com.hutnyk.carfix.booking.dto.response.CustomerBookingResponse;
import com.hutnyk.carfix.in.booking.query.BookingView;

import java.util.Locale;
import java.util.UUID;

public class BookingResponseMapper {

    public static CustomerBookingResponse toResponse(BookingView v) {
        if (v == null) return null;
        return new CustomerBookingResponse(
                v.id(),
                reference(v.id()),
                v.date(),
                v.startTime(),
                v.endTime(),
                v.status().name(),
                v.safeCancelUntil(),
                new BookingBranchResponse(
                        v.branchId(),
                        v.branchName(),
                        v.branchPhoneNumber(),
                        v.branchEmail(),
                        v.streetName(),
                        v.buildingNumber(),
                        v.city()),
                new BookingVehicleResponse(
                        v.carProfileId(),
                        v.carProfileName(),
                        v.brandName(),
                        v.modelName(),
                        v.plates()),
                v.services().stream()
                        .map(s -> new BookingServiceResponse(s.name(), s.price()))
                        .toList(),
                v.totalPrice()
        );
    }

    private static String reference(UUID bookingId) {
        return "BK-" + bookingId.toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
