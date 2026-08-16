package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

public class UserServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId EXISTING_ID = UserId.genId();
    private static final PhoneNumber EXISTING_PHONE = new PhoneNumber("+48", "123456789");
    private static final PasswordHash EXISTING_HASH = PasswordHash.of("$2a$10$storedhashvalue");
    private static final Instant NOW = Instant.parse("2026-08-16T10:00:00Z");
    private static final Instant VERIFIED_EARLIER = Instant.parse("2026-08-01T09:00:00Z");

    private static User existingUser(Instant emailVerifiedAt) {
        return User.builder()
                .id(EXISTING_ID)
                .name("Old")
                .surname("Name")
                .phoneNumber(EXISTING_PHONE)
                .email(EMAIL)
                .role(UserRole.CUSTOMER)
                .passwordHash(EXISTING_HASH)
                .dateOfBirth(LocalDate.of(1990, 5, 1))
                .addressId(7)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    private static final class StubUserPortOut implements UserPortOut {
        User stored = existingUser(null);
        User updated;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.ofNullable(stored);
        }

        @Override
        public boolean existsByEmail(String email) {
            return stored != null;
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            return stored != null;
        }

        @Override
        public User update(User user) {
            this.updated = user;
            this.stored = user;
            return user;
        }
    }

    private static final class StubCodePortOut implements EmailVerificationCodePortOut {
        EmailVerificationCode stored;
        EmailVerificationCode inserted;
        EmailVerificationCode updated;
        UserId deletedFor;

        @Override
        public Optional<EmailVerificationCode> findByUserId(UserId userId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public EmailVerificationCode insert(EmailVerificationCode code) {
            this.inserted = code;
            this.stored = code;
            return code;
        }

        @Override
        public EmailVerificationCode update(EmailVerificationCode code) {
            this.updated = code;
            this.stored = code;
            return code;
        }

        @Override
        public void deleteByUserId(UserId userId) {
            this.deletedFor = userId;
            this.stored = null;
        }
    }

    private static final class RecordingNotifier implements UserNotificationPortOut {
        User recipient;
        String plainCode;
        int calls;

        @Override
        public void sendEmailVerificationCode(User user, String plainCode) {
            this.recipient = user;
            this.plainCode = plainCode;
            this.calls++;
        }
    }

    private final StubUserPortOut portOut = new StubUserPortOut();
    private final StubCodePortOut codePortOut = new StubCodePortOut();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final UserService service = new UserService(portOut, codePortOut, notifier, clock);

    private UserService serviceWithNoUser() {
        StubUserPortOut empty = new StubUserPortOut();
        empty.stored = null;
        return new UserService(empty, codePortOut, notifier, clock);
    }

    // ---------- updateUser (existing behaviour) ----------

    @Test
    public void test_updateUser_takes_editable_fields_from_the_command() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", LocalDate.of(2000, 1, 15));
        //when
        User result = service.updateUser(EMAIL, command);
        //then
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSurname()).isEqualTo("Surname");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
    }

    @Test
    public void test_updateUser_preserves_credentials_identity_and_verification_from_the_loaded_user() {
        //given
        portOut.stored = existingUser(VERIFIED_EARLIER);
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null);
        //when
        service.updateUser(EMAIL, command);
        //then
        assertThat(portOut.updated.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(portOut.updated.getEmail()).isEqualTo(EMAIL);
        assertThat(portOut.updated.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(portOut.updated.getPhoneNumber()).isEqualTo(EXISTING_PHONE);
        assertThat(portOut.updated.getId()).isEqualTo(EXISTING_ID);
        assertThat(portOut.updated.getAddressId()).isEqualTo(7);
        assertThat(portOut.updated.getEmailVerifiedAt()).isEqualTo(VERIFIED_EARLIER);
    }

    @Test
    public void test_updating_a_user_whose_record_vanished_fails_as_authentication() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("John", "Doe", LocalDate.of(1990, 5, 1));
        //when + then
        assertThatThrownBy(() -> serviceWithNoUser().updateUser("gone@example.com", command))
                .isInstanceOf(AuthenticatedUserMissingException.class)
                .hasMessage("Authenticated user not found: gone@example.com");
    }

    @Test
    public void test_updateUser_null_dateOfBirth_clears_it_instead_of_keeping_the_old_value() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null);
        //when
        service.updateUser(EMAIL, command);
        //then
        assertThat(portOut.updated.getDateOfBirth()).isNull();
    }

    // ---------- requestEmailVerification ----------

    @Test
    public void test_request_issues_a_hashed_code_and_emails_the_plain_code_to_the_user() {
        //when
        service.requestEmailVerification(EMAIL);
        //then
        assertThat(codePortOut.inserted).isNotNull();
        assertThat(codePortOut.updated).isNull();
        assertThat(codePortOut.inserted.getUserId()).isEqualTo(EXISTING_ID);
        assertThat(codePortOut.inserted.getIssuedAt()).isEqualTo(NOW);
        assertThat(codePortOut.inserted.getExpiresAt()).isEqualTo(NOW.plus(EmailVerificationCode.TTL));
        assertThat(notifier.calls).isEqualTo(1);
        assertThat(notifier.recipient.getEmail()).isEqualTo(EMAIL);
        assertThat(notifier.plainCode).matches("[0-9]{6}");
        assertThat(codePortOut.inserted.getCodeHash()).isNotEqualTo(notifier.plainCode);
        assertThat(codePortOut.inserted.matches(notifier.plainCode)).isTrue();
    }

    @Test
    public void test_request_for_an_already_verified_email_is_a_conflict_and_sends_nothing() {
        //given
        portOut.stored = existingUser(VERIFIED_EARLIER);
        //when + then
        assertThatThrownBy(() -> service.requestEmailVerification(EMAIL))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
        assertThat(codePortOut.inserted).isNull();
        assertThat(notifier.calls).isZero();
    }

    @Test
    public void test_request_within_the_cooldown_is_refused_with_the_seconds_left() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "111111", NOW.minusSeconds(15));
        //when + then
        assertThatThrownBy(() -> service.requestEmailVerification(EMAIL))
                .isInstanceOf(VerificationCodeResendTooSoonException.class)
                .hasMessage("A verification code was sent recently. You can request a new one in 45 seconds");
        assertThat(codePortOut.updated).isNull();
        assertThat(codePortOut.inserted).isNull();
        assertThat(notifier.calls).isZero();
    }

    @Test
    public void test_request_after_the_cooldown_replaces_the_existing_code_through_update() {
        //given
        EmailVerificationCode old = EmailVerificationCode.issue(EXISTING_ID, "111111", NOW.minus(EmailVerificationCode.RESEND_COOLDOWN))
                .registerFailedAttempt();
        codePortOut.stored = old;
        //when
        service.requestEmailVerification(EMAIL);
        //then
        assertThat(codePortOut.inserted).isNull();
        assertThat(codePortOut.updated).isNotNull();
        assertThat(codePortOut.updated.getAttempts()).isZero();
        assertThat(codePortOut.updated.getIssuedAt()).isEqualTo(NOW);
        assertThat(codePortOut.updated.matches("111111")).isFalse();
        assertThat(codePortOut.updated.matches(notifier.plainCode)).isTrue();
    }

    @Test
    public void test_request_for_a_vanished_user_fails_as_authentication() {
        //when + then
        assertThatThrownBy(() -> serviceWithNoUser().requestEmailVerification("gone@example.com"))
                .isInstanceOf(AuthenticatedUserMissingException.class);
    }

    // ---------- verifyEmail ----------

    @Test
    public void test_verify_with_the_right_code_marks_the_user_verified_now_and_consumes_the_code() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when
        User result = service.verifyEmail(EMAIL, "123456");
        //then
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(portOut.updated.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(portOut.updated.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(codePortOut.deletedFor).isEqualTo(EXISTING_ID);
    }

    @Test
    public void test_verify_with_a_wrong_code_records_the_attempt_and_reports_attempts_left() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "000000"))
                .isInstanceOf(VerificationCodeInvalidException.class)
                .hasMessage("Incorrect verification code. 4 attempt(s) left");
        assertThat(codePortOut.updated.getAttempts()).isEqualTo(1);
        assertThat(portOut.updated).isNull();
        assertThat(codePortOut.deletedFor).isNull();
    }

    @Test
    public void test_the_last_wrong_attempt_exhausts_the_code() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        for (int i = 0; i < EmailVerificationCode.MAX_ATTEMPTS - 1; i++) {
            code = code.registerFailedAttempt();
        }
        codePortOut.stored = code;
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "000000"))
                .isInstanceOf(VerificationCodeAttemptsExceededException.class);
        assertThat(codePortOut.updated.getAttempts()).isEqualTo(EmailVerificationCode.MAX_ATTEMPTS);
        assertThat(portOut.updated).isNull();
    }

    @Test
    public void test_an_exhausted_code_is_refused_even_with_the_right_digits() {
        //given
        EmailVerificationCode code = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        for (int i = 0; i < EmailVerificationCode.MAX_ATTEMPTS; i++) {
            code = code.registerFailedAttempt();
        }
        codePortOut.stored = code;
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeAttemptsExceededException.class);
        assertThat(portOut.updated).isNull();
    }

    @Test
    public void test_an_expired_code_is_refused_even_with_the_right_digits() {
        //given
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minus(EmailVerificationCode.TTL));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeExpiredException.class);
        assertThat(portOut.updated).isNull();
        assertThat(codePortOut.updated).isNull();
    }

    @Test
    public void test_verify_without_an_active_code_is_not_found() {
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(VerificationCodeNotFoundException.class);
        assertThat(portOut.updated).isNull();
    }

    @Test
    public void test_verify_for_an_already_verified_email_is_a_conflict() {
        //given
        portOut.stored = existingUser(VERIFIED_EARLIER);
        codePortOut.stored = EmailVerificationCode.issue(EXISTING_ID, "123456", NOW.minusSeconds(60));
        //when + then
        assertThatThrownBy(() -> service.verifyEmail(EMAIL, "123456"))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
        assertThat(portOut.updated).isNull();
    }
}
