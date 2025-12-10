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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/auth")
public class AuthController {

    private final CustomerPortIn customerPortIn;
    private final RegisterUserCommandMapper registerUserCommandMapper;
    private final LoginUserMapper loginUserMapper;

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy authenticationStrategy;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();


    @PostMapping("/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@Valid @RequestBody RegisterUserRequest request) {

        RegisterUserCommand command = registerUserCommandMapper.toCommand(request);

        //even though User command is passed, customer is created as User is the only thing we need
        Customer registeredUser = customerPortIn.registerCustomer(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerToResponseMapper.toResponse(registeredUser));
    }

    /**Authenticates the user.
     * <p>Validates user credentials and sets <code>Authentication</code> to <code>SecurityContext</code>,
     * that is then set to <code>SecurityContextHolder</code> through <code>SecurityContextHolderStrategy</code>.
     * <p>Manually enables {@link SessionAuthenticationStrategy} to protect against session fixation attack.
     * <p><code>SecurityContext</code> manually persisted to <code>SecurityContextRepository</code>.
     *
     * @param loginUserRequest
     * @param request
     * @param response
     * @return Information about user if authentication succeeded
     */

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> loginUser(@Valid @RequestBody LoginUserRequest loginUserRequest,
                                                       HttpServletRequest request, HttpServletResponse response){

        UsernamePasswordAuthenticationToken passwordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authenticatedToken = authenticationManager.authenticate(passwordAuthenticationToken);

        authenticationStrategy.onAuthentication(authenticatedToken, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticatedToken);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Optional<Customer> customer = customerPortIn.loadByCustomerUsername(loginUserRequest.email());
        LoginUserResponse responseData = loginUserMapper.customerToLoginUserResponse(customer.orElseThrow(
                () -> new CustomerNotFoundException("Customer with such email does not exist: " + loginUserRequest.email())
        ));

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    /**
     * Method for validation of JSESSIONID sent by browser, if session is still valid returns 200 OK and freshly fetched user.
     * <p>Otherwise 401
     * @param userDetails
     * @return
     */

    @GetMapping("/me")
    public ResponseEntity<LoginUserResponse> validateCookie(@AuthenticationPrincipal UserDetails userDetails){
        Optional<Customer> customer = customerPortIn.loadByCustomerUsername(userDetails.getUsername());

        LoginUserResponse responseData = loginUserMapper.customerToLoginUserResponse(customer.orElseThrow(
                () -> new CustomerNotFoundException("Customer with such email does not exist: " + userDetails.getUsername())
        ));

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
