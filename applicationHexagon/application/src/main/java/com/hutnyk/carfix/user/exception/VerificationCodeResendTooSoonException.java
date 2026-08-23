package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.BusinessRuleViolationException;

public class VerificationCodeResendTooSoonException extends BusinessRuleViolationException {

    public VerificationCodeResendTooSoonException(long secondsLeft) {
        super(UserErrorCode.VERIFICATION_CODE_RESEND_TOO_SOON,
                "A verification code was sent recently. You can request a new one in " + secondsLeft + " seconds");
    }
}
