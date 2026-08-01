package com.hutnyk.carfix.dto.response;

import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.user.UserRole;

public record CustomerAccountResponse(
        UserRole role,
        UserCoreResponse user,
        CustomerStatus customerStatus
) implements AccountResponse {
}
