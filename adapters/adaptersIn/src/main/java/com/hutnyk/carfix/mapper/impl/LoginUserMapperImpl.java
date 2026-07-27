package com.hutnyk.carfix.mapper.impl;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.response.CustomerAccountResponse;
import com.hutnyk.carfix.mapper.UserToResponseMapper;
import com.hutnyk.carfix.mapper.interfaces.LoginUserMapper;
import com.hutnyk.carfix.user.User;
import org.springframework.stereotype.Service;

@Service
public class LoginUserMapperImpl implements LoginUserMapper {

    @Override
    public CustomerAccountResponse customerToAccountResponse(Customer customer) {
        User user = customer.getUser();
        return new CustomerAccountResponse(
                user.getRole(),
                UserToResponseMapper.toCoreResponse(user),
                customer.getStatus()
        );
    }
}
