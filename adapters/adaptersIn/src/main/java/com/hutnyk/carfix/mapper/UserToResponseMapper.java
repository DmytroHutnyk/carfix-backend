package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.dto.response.UserResponse;
import com.hutnyk.carfix.user.User;

public class UserToResponseMapper {
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId().id().toString(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber().countryCode(),
                user.getPhoneNumber().phoneNumber(),
                user.getEmail(),
                user.getRole(),
                user.getDateOfBirth()
        );
    }
}
