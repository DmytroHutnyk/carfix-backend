package com.hutnyk.carfix.out.user;

import com.hutnyk.carfix.user.EmailVerificationCode;
import com.hutnyk.carfix.user.UserId;

import java.util.Optional;

public interface EmailVerificationCodePortOut {

    Optional<EmailVerificationCode> findByUserId(UserId userId);

    EmailVerificationCode insert(EmailVerificationCode code);

    EmailVerificationCode update(EmailVerificationCode code);

    void deleteByUserId(UserId userId);
}
