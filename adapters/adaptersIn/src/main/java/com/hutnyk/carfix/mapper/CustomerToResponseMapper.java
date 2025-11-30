package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.dto.response.RegisterCustomerResponse;
import com.hutnyk.carfix.user.User;

public class CustomerToResponseMapper {
    public static RegisterCustomerResponse toResponse(Customer customer){
        User user = customer.getUser();

        return new RegisterCustomerResponse(
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
