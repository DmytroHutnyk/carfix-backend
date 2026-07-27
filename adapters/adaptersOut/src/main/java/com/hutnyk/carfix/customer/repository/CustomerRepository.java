package com.hutnyk.carfix.customer.repository;

import com.hutnyk.carfix.customer.entity.CustomerEntity;
import com.hutnyk.carfix.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    CustomerEntity getCustomerEntityByUserEntity(UserEntity userEntity);
}
