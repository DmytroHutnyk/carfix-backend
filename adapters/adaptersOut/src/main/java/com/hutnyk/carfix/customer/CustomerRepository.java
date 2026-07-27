package com.hutnyk.carfix.customer;

import com.hutnyk.carfix.customer.CustomerEntity;
import com.hutnyk.carfix.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    CustomerEntity getCustomerEntityByUserEntity(UserEntity userEntity);
}
