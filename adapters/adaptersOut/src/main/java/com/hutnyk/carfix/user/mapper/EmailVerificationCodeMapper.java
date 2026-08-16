package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.user.EmailVerificationCode;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.entity.EmailVerificationCodeEntity;

public class EmailVerificationCodeMapper {

    public static EmailVerificationCodeEntity toEntity(EmailVerificationCode code) {
        if (code == null) {
            return null;
        }
        return new EmailVerificationCodeEntity(
                code.getUserId().id(),
                code.getCodeHash(),
                code.getIssuedAt(),
                code.getExpiresAt(),
                code.getAttempts()
        );
    }

    public static EmailVerificationCodeEntity updateEntity(EmailVerificationCodeEntity entity, EmailVerificationCode code) {
        entity.setCodeHash(code.getCodeHash());
        entity.setIssuedAt(code.getIssuedAt());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setAttempts(code.getAttempts());
        return entity;
    }

    public static EmailVerificationCode toDomain(EmailVerificationCodeEntity entity) {
        if (entity == null) {
            return null;
        }
        return EmailVerificationCode.of(
                UserId.of(entity.getUserId()),
                entity.getCodeHash(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getAttempts()
        );
    }
}
