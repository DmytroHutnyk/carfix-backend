package com.hutnyk.carfix.user;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

/**
 * One-time code a user must echo back to prove they own the email address. Only the SHA-256 hash of the code
 * is kept; the plain code exists in the email alone.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EmailVerificationCode {

    public static final int LENGTH = 6;
    public static final Duration TTL = Duration.ofMinutes(15);
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    public static final int MAX_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_PATTERN = "[0-9]{" + LENGTH + "}";

    @EqualsAndHashCode.Include
    private final UserId userId;
    private final String codeHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    @With(AccessLevel.PRIVATE)
    private final int attempts;

    @Builder
    private EmailVerificationCode(UserId userId, String codeHash, Instant issuedAt, Instant expiresAt, int attempts) {
        this.userId = Validator.notNull(userId, "userId");
        this.codeHash = Validator.notBlank(codeHash, "codeHash");
        this.issuedAt = Validator.notNull(issuedAt, "issuedAt");
        this.expiresAt = Validator.notNull(expiresAt, "expiresAt");
        this.attempts = validateAttempts(attempts);
    }

    /**
     * A freshly generated code for a user, valid for {@link #TTL} from {@code now}.
     */
    public static EmailVerificationCode issue(UserId userId, String plainCode, Instant now) {
        Validator.notNull(now, "now");
        return EmailVerificationCode.builder()
                .userId(userId)
                .codeHash(hash(validateFormat(plainCode)))
                .issuedAt(now)
                .expiresAt(now.plus(TTL))
                .attempts(0)
                .build();
    }

    /**
     * Rehydration from persistence — trusts the stored hash and timestamps.
     */
    public static EmailVerificationCode of(UserId userId, String codeHash, Instant issuedAt, Instant expiresAt, int attempts) {
        return EmailVerificationCode.builder()
                .userId(userId)
                .codeHash(codeHash)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .attempts(attempts)
                .build();
    }

    public static String generate() {
        return String.format("%0" + LENGTH + "d", RANDOM.nextInt((int) Math.pow(10, LENGTH)));
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public long secondsUntilResend(Instant now) {
        long millisLeft = Duration.between(now, issuedAt.plus(RESEND_COOLDOWN)).toMillis();
        return millisLeft <= 0 ? 0 : (millisLeft + 999) / 1000;
    }

    public boolean canResend(Instant now) {
        return secondsUntilResend(now) == 0;
    }

    public boolean matches(String plainCode) {
        if (plainCode == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(plainCode).getBytes(StandardCharsets.UTF_8),
                codeHash.getBytes(StandardCharsets.UTF_8));
    }

    public EmailVerificationCode registerFailedAttempt() {
        return withAttempts(attempts + 1);
    }

    public boolean attemptsExhausted() {
        return attempts >= MAX_ATTEMPTS;
    }

    public int attemptsLeft() {
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    private static String validateFormat(String plainCode) {
        if (plainCode == null || !plainCode.matches(CODE_PATTERN)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_CODE_FORMAT, "code");
        }
        return plainCode;
    }

    private static int validateAttempts(int attempts) {
        if (attempts < 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "attempts", attempts);
        }
        return attempts;
    }

    private static String hash(String plainCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(plainCode.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
