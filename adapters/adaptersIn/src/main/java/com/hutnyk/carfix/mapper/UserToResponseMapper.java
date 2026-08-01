package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.dto.response.UserCoreResponse;
import com.hutnyk.carfix.user.User;

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
