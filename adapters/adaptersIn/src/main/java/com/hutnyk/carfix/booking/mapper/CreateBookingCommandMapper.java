package com.hutnyk.carfix.booking.mapper;

import com.hutnyk.carfix.booking.dto.request.CreateBookingRequest;
import com.hutnyk.carfix.in.booking.commands.CreateBookingCommand;

public class CreateBookingCommandMapper {
    public static CreateBookingCommand toCommand(CreateBookingRequest request) {
        if (request == null) return null;
        return new CreateBookingCommand(
                request.branchId(),
                request.carProfileId(),
                request.serviceIds(),
                request.date(),
                request.startTime()
        );
    }
}
