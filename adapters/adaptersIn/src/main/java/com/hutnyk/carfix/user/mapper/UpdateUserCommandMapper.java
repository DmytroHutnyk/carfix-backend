package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.in.user.commands.LocationCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.dto.request.LocationRequest;
import com.hutnyk.carfix.user.dto.request.UpdateUserRequest;

public class UpdateUserCommandMapper {
    public static UpdateUserCommand toCommand(UpdateUserRequest request) {
        if (request == null) {
            return null;
        }

        return new UpdateUserCommand(
                request.name(),
                request.surname(),
                request.dateOfBirth(),
                toLocationCommand(request.preferredLocation())
        );
    }

    private static LocationCommand toLocationCommand(LocationRequest request) {
        if (request == null) {
            return null;
        }

        return new LocationCommand(
                request.city(),
                request.region(),
                request.countryIso(),
                request.latitude(),
                request.longitude()
        );
    }
}
