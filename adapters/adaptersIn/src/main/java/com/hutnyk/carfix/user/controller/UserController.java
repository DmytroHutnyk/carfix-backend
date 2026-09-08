package com.hutnyk.carfix.user.controller;

import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserResponseAssembler;
import com.hutnyk.carfix.user.dto.request.UpdateUserAddressRequest;
import com.hutnyk.carfix.user.dto.request.UpdateUserRequest;
import com.hutnyk.carfix.user.dto.response.AddressResponse;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;
import com.hutnyk.carfix.user.mapper.UpdateUserAddressCommandMapper;
import com.hutnyk.carfix.user.mapper.UpdateUserCommandMapper;
import com.hutnyk.carfix.user.mapper.UserToResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserPortIn userPortIn;
    private final UserResponseAssembler userResponseAssembler;

    @PutMapping("/me")
    public ResponseEntity<UserCoreResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        UpdateUserCommand command = UpdateUserCommandMapper.toCommand(request);
        User updatedUser = userPortIn.updateUser(userDetails.getUsername(), command);

        return ResponseEntity.status(HttpStatus.OK).body(userResponseAssembler.toCoreResponse(updatedUser));
    }

    @PutMapping("/me/address")
    public ResponseEntity<AddressResponse> updateCurrentUserAddress(@Valid @RequestBody UpdateUserAddressRequest request,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        UpdateUserAddressCommand command = UpdateUserAddressCommandMapper.toCommand(request);
        AddressView address = userPortIn.updateAddress(userDetails.getUsername(), command);

        return ResponseEntity.status(HttpStatus.OK).body(UserToResponseMapper.toAddressResponse(address));
    }

    @DeleteMapping("/me/address")
    public ResponseEntity<Void> deleteCurrentUserAddress(@AuthenticationPrincipal UserDetails userDetails) {
        userPortIn.deleteAddress(userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                                  HttpServletRequest request) {
        userPortIn.deleteAccount(userDetails.getUsername());

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}
