package com.hutnyk.carfix.auth.controller;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.auth.dto.request.RegisterUserRequest;
import com.hutnyk.carfix.auth.dto.response.CustomerAccountResponse;
import com.hutnyk.carfix.in.customer.CustomerPortIn;
import com.hutnyk.carfix.in.customer.commands.RegisterUserCommand;
import com.hutnyk.carfix.auth.mapper.CustomerToResponseMapper;
import com.hutnyk.carfix.auth.mapper.RegisterUserCommandMapper;
import com.hutnyk.carfix.user.UserResponseAssembler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private final CustomerPortIn customerPortIn;
    private final RegisterUserCommandMapper registerUserCommandMapper;
    private final UserResponseAssembler userResponseAssembler;

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy authenticationStrategy;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    @PostMapping("/register")
    public ResponseEntity<CustomerAccountResponse> registerCustomer(@Valid @RequestBody RegisterUserRequest requestData,
                                                                    HttpServletRequest request, HttpServletResponse response) {
        RegisterUserCommand command = registerUserCommandMapper.toCommand(requestData);

        Customer registeredUser = customerPortIn.registerCustomer(command);

        UsernamePasswordAuthenticationToken passwordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(requestData.email(), requestData.password());
        Authentication authenticatedToken = authenticationManager.authenticate(passwordAuthenticationToken);

        authenticationStrategy.onAuthentication(authenticatedToken, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticatedToken);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CustomerToResponseMapper.toResponse(registeredUser, userResponseAssembler.toCoreResponse(registeredUser.getUser())));
    }
}
