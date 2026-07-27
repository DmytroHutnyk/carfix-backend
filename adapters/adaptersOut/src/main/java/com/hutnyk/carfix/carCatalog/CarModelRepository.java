package com.hutnyk.carfix.carCatalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModelEntity, Integer> {
    List<CarModelEntity> findByCarBrandEntityId(Integer brandId);
}
