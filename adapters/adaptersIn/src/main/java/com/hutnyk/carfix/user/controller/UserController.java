package com.hutnyk.carfix.user.controller;

import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.dto.request.UpdateUserRequest;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;
import com.hutnyk.carfix.user.mapper.UpdateUserCommandMapper;
import com.hutnyk.carfix.user.mapper.UserToResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserPortIn userPortIn;

    @PutMapping("/me")
    public ResponseEntity<UserCoreResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        UpdateUserCommand command = UpdateUserCommandMapper.toCommand(request);
        User updatedUser = userPortIn.updateUser(userDetails.getUsername(), command);

        return ResponseEntity.status(HttpStatus.OK).body(UserToResponseMapper.toCoreResponse(updatedUser));
    }
}
