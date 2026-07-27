package com.hutnyk.carfix.auth.dto.response;

import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public record CustomerAccountResponse(
        UserRole role,
        UserCoreResponse user,
        CustomerStatus customerStatus
) implements AccountResponse {
}
