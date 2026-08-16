package com.hutnyk.carfix.in.booking.commands;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateBookingCommand(
        UUID branchId,
        UUID carProfileId,
        List<Integer> serviceIds,
        LocalDate date,
        LocalTime startTime
) {}
