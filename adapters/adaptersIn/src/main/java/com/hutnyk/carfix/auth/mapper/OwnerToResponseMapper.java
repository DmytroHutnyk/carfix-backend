package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.auth.dto.response.OwnerAccountResponse;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.mapper.UserToResponseMapper;

public final class OwnerToResponseMapper {

    private OwnerToResponseMapper() {}

    public static OwnerAccountResponse toResponse(Owner owner) {
        if (owner == null) {
            return null;
        }
        User user = owner.getUser();
        return new OwnerAccountResponse(
                user.getRole(),
                UserToResponseMapper.toCoreResponse(user),
                owner.getBusinessName(),
                owner.getVatIn(),
                owner.getRegon()
        );
    }
}
