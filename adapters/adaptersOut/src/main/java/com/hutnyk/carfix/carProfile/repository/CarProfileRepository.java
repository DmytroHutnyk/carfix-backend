package com.hutnyk.carfix.carProfile.repository;

import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarProfileRepository extends JpaRepository<CarProfileEntity, UUID> {

    @Query("SELECT cp FROM car_profiles cp " +
           "JOIN FETCH cp.customerEntity ce " +
           "JOIN FETCH cp.modelVersionEntity mve " +
           "JOIN FETCH mve.carModelEntity cme " +
           "JOIN FETCH cme.carBrandEntity " +
           "WHERE ce.id = :customerId")
    List<CarProfileEntity> findAllByCustomerIdWithDetails(@Param("customerId") UUID customerId);

    @Query("SELECT cp FROM car_profiles cp " +
           "JOIN FETCH cp.customerEntity ce " +
           "JOIN FETCH cp.modelVersionEntity mve " +
           "JOIN FETCH mve.carModelEntity cme " +
           "JOIN FETCH cme.carBrandEntity " +
           "WHERE cp.id = :id AND ce.id = :customerId")
    Optional<CarProfileEntity> findByIdAndCustomerIdWithDetails(@Param("id") UUID id, @Param("customerId") UUID customerId);

    boolean existsByIdAndCustomerEntityId(UUID id, UUID customerId);

    void deleteAllByCustomerEntityId(UUID customerId);
}
