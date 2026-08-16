package com.hutnyk.carfix.address.repository;

import com.hutnyk.carfix.address.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Integer> {
    Optional<CityEntity> findFirstByNameAndRegionEntityId(String name, Integer regionId);
}
