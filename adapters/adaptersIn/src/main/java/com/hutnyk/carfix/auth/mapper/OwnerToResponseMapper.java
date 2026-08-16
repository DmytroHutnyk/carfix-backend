package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.auth.dto.response.OwnerAccountResponse;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public class OwnerToResponseMapper {
    public static OwnerAccountResponse toResponse(Owner owner, UserCoreResponse user) {
        if (owner == null) {
            return null;
        }

        return new OwnerAccountResponse(
                owner.getUser().getRole(),
                user,
                owner.getBusinessName(),
                owner.getVatIn(),
                owner.getRegon()
        );
    }
}
