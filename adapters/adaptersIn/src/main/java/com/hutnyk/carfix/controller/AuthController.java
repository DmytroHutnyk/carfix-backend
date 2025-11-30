package com.hutnyk.carfix.controller;

import com.hutnyk.carfix.in.commands.RegisterUserCommand;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.request.RegisterUserRequest;
import com.hutnyk.carfix.dto.response.RegisterCustomerResponse;
import com.hutnyk.carfix.in.CustomerPortIn;
import com.hutnyk.carfix.mapper.CustomerToResponseMapper;
import com.hutnyk.carfix.mapper.RegisterUserCommandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/auth")
public class AuthController {

    private final CustomerPortIn customerPortIn;
    private final RegisterUserCommandMapper registerUserCommandMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@Valid @RequestBody RegisterUserRequest request) {

        RegisterUserCommand command = registerUserCommandMapper.toCommand(request);

        //even though User command is passed, customer is created as User is the only thing we need
        Customer registeredUser = customerPortIn.registerCustomer(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerToResponseMapper.toResponse(registeredUser));
    }
}
