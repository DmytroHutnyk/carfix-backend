package com.hutnyk.carfix.address.repository;

import com.hutnyk.carfix.address.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer> {

    @Query("""
            SELECT a FROM AddressEntity a
            JOIN FETCH a.cityEntity c
            JOIN FETCH c.regionEntity r
            JOIN FETCH r.countryEntity
            WHERE a.id = :addressId
            """)
    Optional<AddressEntity> findWithLocationById(@Param("addressId") Integer addressId);
}
