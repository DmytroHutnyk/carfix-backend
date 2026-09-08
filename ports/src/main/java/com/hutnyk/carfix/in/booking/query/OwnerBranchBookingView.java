package com.hutnyk.carfix.in.booking.query;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OwnerBranchBookingView(
        String reference,
        String status,
        LocalTime start,
        LocalTime end,
        OwnerBranchBookingCustomerView customer,
        OwnerBranchBookingCarView car,
        List<OwnerBranchBookingServiceView> services,
        String bay,
        List<OwnerBranchBookingEmployeeView> employees,
        List<String> equipment,
        int totalDurationMinutes,
        LocalDateTime createdAt
) {}
