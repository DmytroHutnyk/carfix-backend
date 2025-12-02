package com.hutnyk.carfix.mapper.impl;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.response.LoginUserResponse;
import com.hutnyk.carfix.mapper.interfaces.LoginUserMapper;
import com.hutnyk.carfix.user.User;
import org.springframework.stereotype.Service;

@Service
public class LoginUserMapperImpl implements LoginUserMapper {

    @Override
    public LoginUserResponse customerToLoginUserResponse(Customer customer) {
        User user = customer.getUser();
        return new LoginUserResponse(
                user.getId().id().toString(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber().countryCode(),
                user.getPhoneNumber().phoneNumber(),
                user.getEmail(),
                user.getRole(),
                user.getDateOfBirth(),
                customer.getStatus()
        );
    }
}
