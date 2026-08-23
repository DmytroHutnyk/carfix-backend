package com.hutnyk.carfix.address.repository;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, Integer> {
    Optional<RegionEntity> findFirstByNameAndCountryEntityIso(String name, CountryIso countryIso);
}
