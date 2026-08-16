package com.hutnyk.carfix.service.repository;

import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, Integer> {
    List<ServiceCategoryEntity> findAllByOrderByNameAsc();
}
