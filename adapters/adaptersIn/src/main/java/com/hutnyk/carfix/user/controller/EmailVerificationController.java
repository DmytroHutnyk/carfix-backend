package com.hutnyk.carfix.user.controller;

import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserResponseAssembler;
import com.hutnyk.carfix.user.dto.request.ConfirmEmailVerificationRequest;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/me/email-verification")
public class EmailVerificationController {

    private final UserPortIn userPortIn;
    private final UserResponseAssembler userResponseAssembler;

    @PostMapping
    public ResponseEntity<Void> requestVerification(@AuthenticationPrincipal UserDetails userDetails) {
        userPortIn.requestEmailVerification(userDetails.getUsername());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<UserCoreResponse> confirmVerification(@Valid @RequestBody ConfirmEmailVerificationRequest request,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userPortIn.verifyEmail(userDetails.getUsername(), request.code());
        return ResponseEntity.ok(userResponseAssembler.toCoreResponse(user));
    }
}
