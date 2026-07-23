package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.entity.carProfile.CarBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarBrandRepository extends JpaRepository<CarBrandEntity, Integer> {}
