package com.hutnyk.carfix.owner.repository;

import com.hutnyk.carfix.owner.entity.OwnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {

    @Query("""
            SELECT o FROM OwnerEntity o
            JOIN FETCH o.userEntity u
            WHERE u.email = :email
            """)
    Optional<OwnerEntity> findByEmail(@Param("email") String email);
}
