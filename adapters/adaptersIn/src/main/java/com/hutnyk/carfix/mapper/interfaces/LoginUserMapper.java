package com.hutnyk.carfix.mapper.interfaces;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.response.CustomerAccountResponse;

public interface LoginUserMapper {
    CustomerAccountResponse customerToAccountResponse(Customer customer);
}
