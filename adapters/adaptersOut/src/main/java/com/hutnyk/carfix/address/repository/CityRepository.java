package com.hutnyk.carfix.address.repository;

import com.hutnyk.carfix.address.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Integer> {
    Optional<CityEntity> findFirstByNameAndRegionEntityId(String name, Integer regionId);

    @Query("""
            SELECT c FROM CityEntity c
            JOIN FETCH c.regionEntity r
            JOIN FETCH r.countryEntity
            WHERE c.id = :cityId
            """)
    Optional<CityEntity> findWithLocationById(@Param("cityId") Integer cityId);
}
