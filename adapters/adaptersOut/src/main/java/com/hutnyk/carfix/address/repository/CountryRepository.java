package com.hutnyk.carfix.address.repository;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryEntity, CountryIso> {
}
