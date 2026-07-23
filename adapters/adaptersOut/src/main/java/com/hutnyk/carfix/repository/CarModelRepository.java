package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.entity.carProfile.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModelEntity, Integer> {
    List<CarModelEntity> findByCarBrandEntityId(Integer brandId);
}
