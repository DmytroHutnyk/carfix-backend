package com.hutnyk.carfix.user.repository;

import com.hutnyk.carfix.user.entity.EmailVerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCodeEntity, UUID> {
}
