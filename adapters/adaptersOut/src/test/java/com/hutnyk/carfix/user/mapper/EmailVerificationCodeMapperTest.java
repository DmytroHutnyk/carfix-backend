package com.hutnyk.carfix.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.user.EmailVerificationCode;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.entity.EmailVerificationCodeEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class EmailVerificationCodeMapperTest {

    private static final UserId USER_ID = UserId.genId();
    private static final Instant NOW = Instant.parse("2026-08-16T10:00:00Z");

    @Test
    public void test_toEntity_and_toDomain_round_trip_every_field() {
        EmailVerificationCode code = EmailVerificationCode.of(USER_ID, "abc123hash", NOW, NOW.plusSeconds(900), 2);
        EmailVerificationCodeEntity entity = EmailVerificationCodeMapper.toEntity(code);
        EmailVerificationCode back = EmailVerificationCodeMapper.toDomain(entity);
        assertThat(entity.getUserId()).isEqualTo(USER_ID.id());
        assertThat(entity.getCodeHash()).isEqualTo("abc123hash");
        assertThat(entity.getIssuedAt()).isEqualTo(NOW);
        assertThat(entity.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        assertThat(entity.getAttempts()).isEqualTo(2);
        assertThat(back.getUserId()).isEqualTo(USER_ID);
        assertThat(back.getCodeHash()).isEqualTo("abc123hash");
        assertThat(back.getIssuedAt()).isEqualTo(NOW);
        assertThat(back.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        assertThat(back.getAttempts()).isEqualTo(2);
    }

    @Test
    public void test_updateEntity_overwrites_hash_timestamps_and_attempts_but_never_the_id() {
        EmailVerificationCodeEntity entity = EmailVerificationCodeMapper.toEntity(
                EmailVerificationCode.of(USER_ID, "oldhash", NOW, NOW.plusSeconds(900), 4));
        EmailVerificationCode fresh = EmailVerificationCode.of(USER_ID, "newhash", NOW.plusSeconds(120), NOW.plusSeconds(1020), 0);
        EmailVerificationCodeEntity result = EmailVerificationCodeMapper.updateEntity(entity, fresh);
        assertThat(result).isSameAs(entity);
        assertThat(entity.getUserId()).isEqualTo(USER_ID.id());
        assertThat(entity.getCodeHash()).isEqualTo("newhash");
        assertThat(entity.getIssuedAt()).isEqualTo(NOW.plusSeconds(120));
        assertThat(entity.getExpiresAt()).isEqualTo(NOW.plusSeconds(1020));
        assertThat(entity.getAttempts()).isZero();
    }

    @Test
    public void test_null_guards() {
        assertThat(EmailVerificationCodeMapper.toEntity(null)).isNull();
        assertThat(EmailVerificationCodeMapper.toDomain(null)).isNull();
    }
}
