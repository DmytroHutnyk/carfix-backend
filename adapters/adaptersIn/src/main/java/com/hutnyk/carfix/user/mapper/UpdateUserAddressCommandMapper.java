package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.user.dto.request.UpdateUserAddressRequest;

public class UpdateUserAddressCommandMapper {
    public static UpdateUserAddressCommand toCommand(UpdateUserAddressRequest request) {
        if (request == null) {
            return null;
        }

        return new UpdateUserAddressCommand(
                request.streetName(),
                request.buildingNumber(),
                request.flatNumber(),
                request.postalCode(),
                request.city(),
                request.region(),
                request.countryIso(),
                request.latitude(),
                request.longitude(),
                request.googlePlaceId()
        );
    }
}
