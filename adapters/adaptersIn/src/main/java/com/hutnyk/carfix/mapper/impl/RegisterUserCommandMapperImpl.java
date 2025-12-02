package com.hutnyk.carfix.mapper.impl;

import com.hutnyk.carfix.in.commands.RegisterUserCommand;
import com.hutnyk.carfix.dto.request.RegisterUserRequest;
import com.hutnyk.carfix.mapper.interfaces.RegisterUserCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserCommandMapperImpl implements RegisterUserCommandMapper {

    private final PasswordEncoder passwordEncoder;

    public RegisterUserCommand toCommand(RegisterUserRequest request) {

        return new RegisterUserCommand(
                request.name(),
                request.surname(),
                request.phoneCountryCode(),
                request.phoneNumber(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
    }
}

