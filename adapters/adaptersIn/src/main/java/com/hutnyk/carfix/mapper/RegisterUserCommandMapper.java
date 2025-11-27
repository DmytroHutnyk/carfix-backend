package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.in.commands.RegisterUserCommand;
import com.hutnyk.carfix.dto.request.RegisterUserRequest;

public interface RegisterUserCommandMapper {
    RegisterUserCommand toCommand(RegisterUserRequest request);
}

