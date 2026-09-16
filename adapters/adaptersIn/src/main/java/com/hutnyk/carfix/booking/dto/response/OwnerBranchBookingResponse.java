package com.hutnyk.carfix.booking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OwnerBranchBookingResponse(
        String reference,
        String status,
        LocalDate date,
        LocalTime start,
        LocalTime end,
        OwnerBranchBookingCustomerResponse customer,
        OwnerBranchBookingCarResponse car,
        List<OwnerBranchBookingServiceResponse> services,
        String bay,
        List<OwnerBranchBookingEmployeeResponse> employees,
        List<String> equipment,
        int totalDurationMinutes,
        LocalDateTime createdAt
) {}
