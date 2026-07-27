package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModelEntity, Integer> {
    List<CarModelEntity> findByCarBrandEntityId(Integer brandId);
}
