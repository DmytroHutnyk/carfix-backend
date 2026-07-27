package com.hutnyk.carfix.auth.controller;

import com.hutnyk.carfix.auth.AccountResponseAssembler;
import com.hutnyk.carfix.auth.dto.request.LoginUserRequest;
import com.hutnyk.carfix.auth.dto.response.AccountResponse;
import com.hutnyk.carfix.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountResponseAssembler accountAssembler;

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy authenticationStrategy;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    @PostMapping("/login")
    public ResponseEntity<AccountResponse> loginUser(@Valid @RequestBody LoginUserRequest requestData,
                                                     HttpServletRequest request, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken passwordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(requestData.email(), requestData.password());
        Authentication authenticatedToken = authenticationManager.authenticate(passwordAuthenticationToken);

        authenticationStrategy.onAuthentication(authenticatedToken, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticatedToken);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        CustomUserDetails principal = (CustomUserDetails) authenticatedToken.getPrincipal();
        AccountResponse body = accountAssembler.assemble(principal.getUsername(), principal.getRole());

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        securityContextHolderStrategy.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> validateCookie(@AuthenticationPrincipal CustomUserDetails principal) {
        AccountResponse body = accountAssembler.assemble(principal.getUsername(), principal.getRole());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }
}
