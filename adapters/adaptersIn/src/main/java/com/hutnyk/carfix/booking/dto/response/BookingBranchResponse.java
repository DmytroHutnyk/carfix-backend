package com.hutnyk.carfix.booking.dto.response;

import java.util.UUID;

public record BookingBranchResponse(
        UUID branchId,
        String name,
        String phoneNumber,
        String email,
        String streetName,
        String buildingNumber,
        String city
) {}
