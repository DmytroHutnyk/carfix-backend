package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity getUserByEmail(String email);
    boolean existsByPhoneCountryCodeAndPhoneNumber(String phoneCountryCode, String phoneNumber);
    boolean existsByEmail(String email);
}
