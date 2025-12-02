package com.hutnyk.carfix.controller;

import com.hutnyk.carfix.dto.request.LoginUserRequest;
import com.hutnyk.carfix.dto.response.LoginUserResponse;
import com.hutnyk.carfix.exception.CustomerNotFoundException;
import com.hutnyk.carfix.in.commands.RegisterUserCommand;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.request.RegisterUserRequest;
import com.hutnyk.carfix.dto.response.RegisterCustomerResponse;
import com.hutnyk.carfix.in.CustomerPortIn;
import com.hutnyk.carfix.mapper.CustomerToResponseMapper;
import com.hutnyk.carfix.mapper.interfaces.LoginUserMapper;
import com.hutnyk.carfix.mapper.interfaces.RegisterUserCommandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/auth")
public class AuthController {

    private final CustomerPortIn customerPortIn;
    private final RegisterUserCommandMapper registerUserCommandMapper;
    private final LoginUserMapper loginUserMapper;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@Valid @RequestBody RegisterUserRequest request) {

        RegisterUserCommand command = registerUserCommandMapper.toCommand(request);

        //even though User command is passed, customer is created as User is the only thing we need
        Customer registeredUser = customerPortIn.registerCustomer(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerToResponseMapper.toResponse(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> loginUser(@Valid @RequestBody LoginUserRequest loginUserRequest){

        UsernamePasswordAuthenticationToken passwordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());

        Authentication authenticatedToken = authenticationManager.authenticate(passwordAuthenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authenticatedToken);

        Optional<Customer> customer = customerPortIn.loadByCustomerUsername(loginUserRequest.email());

        LoginUserResponse response = loginUserMapper.customerToLoginUserResponse(customer.orElseThrow(
                () -> new CustomerNotFoundException("Customer with such email does not exist: " + loginUserRequest.email())
        ));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}
