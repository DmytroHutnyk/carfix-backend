package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.in.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.dto.request.UpdateUserRequest;

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
