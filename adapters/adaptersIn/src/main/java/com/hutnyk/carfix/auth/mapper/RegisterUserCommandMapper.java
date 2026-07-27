package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.in.customer.commands.RegisterUserCommand;
import com.hutnyk.carfix.auth.dto.request.RegisterUserRequest;

public interface RegisterUserCommandMapper {
    RegisterUserCommand toCommand(RegisterUserRequest request);
}

