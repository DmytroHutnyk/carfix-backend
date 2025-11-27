package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.carProfile.CarProfileEntity;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.user.CustomerEntity;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserEntity;

import java.util.Set;

public class CustomerMapper {
    public static CustomerEntity toEntity(Customer customer, UserEntity userEntity, Set<CarProfileEntity> carProfileEntities){
        if(customer == null || userEntity == null){
            return null;
        }

        return new CustomerEntity(userEntity.getId(), userEntity, customer.getStatus(), carProfileEntities);
    }

    public static Customer toDomain(CustomerEntity customerEntity, User user){
        if(customerEntity == null){
            return null;
        }

        return Customer.of(user, customerEntity.getCustomerStatus());

    }
}
