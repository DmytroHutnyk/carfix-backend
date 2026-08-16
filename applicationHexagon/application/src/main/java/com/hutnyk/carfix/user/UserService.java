package com.hutnyk.carfix.user;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.user.EmailVerificationCodePortOut;
import com.hutnyk.carfix.out.user.UserNotificationPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.user.exception.VerificationCodeAttemptsExceededException;
import com.hutnyk.carfix.user.exception.VerificationCodeExpiredException;
import com.hutnyk.carfix.user.exception.VerificationCodeInvalidException;
import com.hutnyk.carfix.user.exception.VerificationCodeNotFoundException;
import com.hutnyk.carfix.user.exception.VerificationCodeResendTooSoonException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@ApplicationService
@RequiredArgsConstructor
public class UserService implements UserPortIn {

    private final UserPortOut userPortOut;
    private final EmailVerificationCodePortOut verificationCodePortOut;
    private final UserNotificationPortOut userNotificationPortOut;
    private final Clock clock;

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userPortOut.loadUserByEmail(email);
    }

    @Override
    public User updateUser(String email, UpdateUserCommand command) {
        User existing = loadOrThrow(email);
        User user = User.of(
                existing.getId(),
                command.name(),
                command.surname(),
                existing.getPhoneNumber(),
                existing.getEmail(),
                existing.getRole(),
                existing.getPasswordHash(),
                command.dateOfBirth(),
                existing.getAddressId(),
                existing.getEmailVerifiedAt()
        );
        return userPortOut.update(user);
    }

    @Override
    public void requestEmailVerification(String email) {
        User user = loadOrThrow(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }
        Instant now = clock.instant();
        Optional<EmailVerificationCode> existing = verificationCodePortOut.findByUserId(user.getId());
        existing.filter(code -> !code.canResend(now)).ifPresent(code -> {
            throw new VerificationCodeResendTooSoonException(code.secondsUntilResend(now));
        });
        String plainCode = EmailVerificationCode.generate();
        EmailVerificationCode fresh = EmailVerificationCode.issue(user.getId(), plainCode, now);
        if (existing.isPresent()) {
            verificationCodePortOut.update(fresh);
        } else {
            verificationCodePortOut.insert(fresh);
        }
        userNotificationPortOut.sendEmailVerificationCode(user, plainCode);
    }

    /**
     * A wrong code must still count: the attempt increment is committed even though the use case ends in one of
     * these two exceptions.
     */
    @Override
    @Transactional(noRollbackFor = {VerificationCodeInvalidException.class, VerificationCodeAttemptsExceededException.class})
    public User verifyEmail(String email, String code) {
        User user = loadOrThrow(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }
        Instant now = clock.instant();
        EmailVerificationCode active = verificationCodePortOut.findByUserId(user.getId())
                .orElseThrow(VerificationCodeNotFoundException::new);
        if (active.isExpired(now)) {
            throw new VerificationCodeExpiredException();
        }
        if (active.attemptsExhausted()) {
            throw new VerificationCodeAttemptsExceededException();
        }
        if (!active.matches(code)) {
            EmailVerificationCode failed = verificationCodePortOut.update(active.registerFailedAttempt());
            if (failed.attemptsExhausted()) {
                throw new VerificationCodeAttemptsExceededException();
            }
            throw new VerificationCodeInvalidException(failed.attemptsLeft());
        }
        verificationCodePortOut.deleteByUserId(user.getId());
        return userPortOut.update(user.verifyEmail(now));
    }

    private User loadOrThrow(String email) {
        return userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(email));
    }
}
