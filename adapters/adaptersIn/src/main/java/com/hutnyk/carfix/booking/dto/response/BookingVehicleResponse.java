package com.hutnyk.carfix.booking.dto.response;

import java.util.UUID;

public record BookingVehicleResponse(
        UUID carProfileId,
        String name,
        String brandName,
        String modelName,

        //Nullable
        String plates
) {}
