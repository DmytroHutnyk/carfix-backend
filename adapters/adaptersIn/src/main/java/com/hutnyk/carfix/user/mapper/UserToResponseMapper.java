package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public class UserToResponseMapper {
    public static UserCoreResponse toCoreResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserCoreResponse(
                user.getId().id().toString(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber().countryCode(),
                user.getPhoneNumber().phoneNumber(),
                user.getEmail(),
                user.getDateOfBirth()
        );
    }
}
