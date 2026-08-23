package com.hutnyk.carfix.user.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.out.user.EmailVerificationCodePortOut;
import com.hutnyk.carfix.user.EmailVerificationCode;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.entity.EmailVerificationCodeEntity;
import com.hutnyk.carfix.user.mapper.EmailVerificationCodeMapper;
import com.hutnyk.carfix.user.repository.EmailVerificationCodeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class EmailVerificationCodeAdapterOut implements EmailVerificationCodePortOut {

    private final EmailVerificationCodeRepository repository;
    private final EntityManager entityManager;

    @Override
    public Optional<EmailVerificationCode> findByUserId(UserId userId) {
        return repository.findById(userId.id()).map(EmailVerificationCodeMapper::toDomain);
    }

    @Override
    public EmailVerificationCode insert(EmailVerificationCode code) {
        EmailVerificationCodeEntity entity = EmailVerificationCodeMapper.toEntity(code);
        entityManager.persist(entity);
        return EmailVerificationCodeMapper.toDomain(entity);
    }

    @Override
    public EmailVerificationCode update(EmailVerificationCode code) {
        EmailVerificationCodeEntity entity = repository.findById(code.getUserId().id())
                .orElseThrow(() -> new UnexpectedStateException(
                        "Verification code not found for user: " + code.getUserId().id()));
        return EmailVerificationCodeMapper.toDomain(
                repository.save(EmailVerificationCodeMapper.updateEntity(entity, code)));
    }

    @Override
    public void deleteByUserId(UserId userId) {
        repository.deleteById(userId.id());
    }
}
