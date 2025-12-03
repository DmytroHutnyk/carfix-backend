package com.hutnyk.carfix.mapper.interfaces;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.response.LoginUserResponse;

public interface LoginUserMapper {
    LoginUserResponse customerToLoginUserResponse(Customer customer);
}
