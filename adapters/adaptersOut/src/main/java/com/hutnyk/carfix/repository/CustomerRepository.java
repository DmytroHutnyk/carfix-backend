package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.entity.user.CustomerEntity;
import com.hutnyk.carfix.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    CustomerEntity getCustomerEntityByUserEntity(UserEntity userEntity);
}
