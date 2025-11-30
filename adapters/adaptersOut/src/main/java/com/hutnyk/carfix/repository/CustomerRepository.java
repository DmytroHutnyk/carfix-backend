package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.entity.user.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
}
