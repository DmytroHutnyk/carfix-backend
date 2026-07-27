package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.auth.dto.response.CustomerAccountResponse;

public interface LoginUserMapper {
    CustomerAccountResponse customerToAccountResponse(Customer customer);
}
