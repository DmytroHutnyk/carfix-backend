package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.dto.request.UpdateUserRequest;
import com.hutnyk.carfix.in.commands.UpdateUserCommand;

public class UpdateUserCommandMapper {
    public static UpdateUserCommand toCommand(UpdateUserRequest request) {
        if (request == null) {
            return null;
        }

        return new UpdateUserCommand(
                request.name(),
                request.surname(),
                request.dateOfBirth()
        );
    }
}
