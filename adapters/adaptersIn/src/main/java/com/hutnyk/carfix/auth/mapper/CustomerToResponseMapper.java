package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.auth.dto.response.CustomerAccountResponse;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public class CustomerToResponseMapper {
    public static CustomerAccountResponse toResponse(Customer customer, UserCoreResponse user) {
        if (customer == null) {
            return null;
        }

        return new CustomerAccountResponse(
                customer.getUser().getRole(),
                user,
                customer.getStatus()
        );
    }
}
