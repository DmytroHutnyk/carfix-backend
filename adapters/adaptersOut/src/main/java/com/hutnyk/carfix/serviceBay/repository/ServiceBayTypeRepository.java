package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBayTypeRepository extends JpaRepository<ServiceBayTypeEntity, Integer> {
}
