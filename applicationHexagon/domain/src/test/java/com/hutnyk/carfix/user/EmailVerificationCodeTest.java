package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class EmailVerificationCodeTest {

    private static final UserId USER_ID = UserId.genId();
    private static final Instant NOW = Instant.parse("2026-08-16T10:00:00Z");

    @Test
    public void test_generate_returns_six_digits() {
        //when
        String code = EmailVerificationCode.generate();
        //then
        assertThat(code).matches("[0-9]{6}");
    }

    @Test
    public void test_issue_stores_a_hash_not_the_plain_code_and_sets_ttl_and_zero_attempts() {
        //when
        EmailVerificationCode code = EmailVerificationCode.issue(USER_ID, "123456", NOW);
        //then
        assertThat(code.getUserId()).isEqualTo(USER_ID);
        assertThat(code.getCodeHash()).isNotEqualTo("123456").hasSize(64);
        assertThat(code.getIssuedAt()).isEqualTo(NOW);
        assertThat(code.getExpiresAt()).isEqualTo(NOW.plus(EmailVerificationCode.TTL));
        assertThat(code.getAttempts()).isZero();
    }

    @Test
    public void test_issue_rejects_a_code_that_is_not_six_digits() {
        //when + then
        assertThatThrownBy(() -> EmailVerificationCode.issue(USER_ID, "12ab56", NOW))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThatThrownBy(() -> EmailVerificationCode.issue(USER_ID, "1234567", NOW))
                .isInstanceOf(DomainObjectValidationException.class);
    }

    @Test
    public void test_matches_only_the_plain_code_it_was_issued_with() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(USER_ID, "123456", NOW);
        //when + then
        assertThat(code.matches("123456")).isTrue();
        assertThat(code.matches("654321")).isFalse();
        assertThat(code.matches(null)).isFalse();
    }

    @Test
    public void test_expiry_boundary_is_inclusive_at_expires_at() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(USER_ID, "123456", NOW);
        //when + then
        assertThat(code.isExpired(NOW.plus(EmailVerificationCode.TTL).minusSeconds(1))).isFalse();
        assertThat(code.isExpired(NOW.plus(EmailVerificationCode.TTL))).isTrue();
    }

    @Test
    public void test_resend_cooldown_counts_down_in_whole_seconds_and_opens_after_the_cooldown() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(USER_ID, "123456", NOW);
        //when + then
        assertThat(code.secondsUntilResend(NOW)).isEqualTo(EmailVerificationCode.RESEND_COOLDOWN.toSeconds());
        assertThat(code.secondsUntilResend(NOW.plusMillis(59_500))).isEqualTo(1);
        assertThat(code.canResend(NOW.plusSeconds(59))).isFalse();
        assertThat(code.canResend(NOW.plus(EmailVerificationCode.RESEND_COOLDOWN))).isTrue();
        assertThat(code.secondsUntilResend(NOW.plus(Duration.ofMinutes(5)))).isZero();
    }

    @Test
    public void test_failed_attempts_accumulate_until_exhausted() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(USER_ID, "123456", NOW);
        //when
        for (int i = 0; i < EmailVerificationCode.MAX_ATTEMPTS - 1; i++) {
            code = code.registerFailedAttempt();
        }
        //then
        assertThat(code.getAttempts()).isEqualTo(EmailVerificationCode.MAX_ATTEMPTS - 1);
        assertThat(code.attemptsLeft()).isEqualTo(1);
        assertThat(code.attemptsExhausted()).isFalse();
        EmailVerificationCode exhausted = code.registerFailedAttempt();
        assertThat(exhausted.attemptsExhausted()).isTrue();
        assertThat(exhausted.attemptsLeft()).isZero();
        assertThat(exhausted.matches("123456")).isTrue();
    }

    @Test
    public void test_of_rehydrates_without_recomputing_anything() {
        //when
        EmailVerificationCode code = EmailVerificationCode.of(USER_ID, "abc123", NOW, NOW.plusSeconds(10), 2);
        //then
        assertThat(code.getCodeHash()).isEqualTo("abc123");
        assertThat(code.getExpiresAt()).isEqualTo(NOW.plusSeconds(10));
        assertThat(code.getAttempts()).isEqualTo(2);
    }

    @Test
    public void test_of_rejects_negative_attempts() {
        //when + then
        assertThatThrownBy(() -> EmailVerificationCode.of(USER_ID, "abc123", NOW, NOW.plusSeconds(10), -1))
                .isInstanceOf(DomainObjectValidationException.class);
    }
}
