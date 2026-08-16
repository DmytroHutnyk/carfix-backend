package com.hutnyk.carfix.auth.dto.response;

import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public record OwnerAccountResponse(
        UserRole role,
        UserCoreResponse user,
        String businessName,
        String vatIn,
        String regon
) implements AccountResponse {
}
