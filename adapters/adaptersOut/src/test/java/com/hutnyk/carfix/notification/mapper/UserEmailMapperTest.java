package com.hutnyk.carfix.notification.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.notification.mail.EmailMessage;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

public class UserEmailMapperTest {

    private static User user(String name) {
        return User.builder()
                .id(UserId.genId())
                .name(name)
                .surname("Doe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(null)
                .addressId(null)
                .emailVerifiedAt(null)
                .build();
    }

    @Test
    public void test_verification_email_is_addressed_to_the_user_and_carries_the_code_in_subject_and_both_bodies() {
        //when
        EmailMessage message = UserEmailMapper.verificationCode(user("John"), "123456");
        //then
        assertThat(message.to()).isEqualTo("john@example.com");
        assertThat(message.subject()).isEqualTo("Your CarFix verification code: 123456");
        assertThat(message.textBody()).contains("Hi John,").contains("123456").contains("15 minutes");
        assertThat(message.htmlBody()).contains("Hi John,").contains("123456").contains("15 minutes")
                .startsWith("<!doctype html>");
    }

    @Test
    public void test_html_body_escapes_the_user_name() {
        //when
        EmailMessage message = UserEmailMapper.verificationCode(user("<b>Bob</b>"), "123456");
        //then
        assertThat(message.htmlBody()).contains("&lt;b&gt;Bob&lt;/b&gt;").doesNotContain("<b>Bob</b>");
        assertThat(message.textBody()).contains("<b>Bob</b>");
    }
}
