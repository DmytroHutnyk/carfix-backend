package com.hutnyk.carfix.notification.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.notification.mail.EmailMessage;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UserNotificationAdapterOutTest {

    @Test
    public void test_sendEmailVerificationCode_hands_the_rendered_message_to_the_email_sender() {
        List<EmailMessage> sent = new ArrayList<>();
        UserNotificationAdapterOut adapter = new UserNotificationAdapterOut(sent::add);
        User user = User.builder()
                .id(UserId.genId())
                .name("John")
                .surname("Doe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(null)
                .addressId(null)
                .emailVerifiedAt(null)
                .build();
        adapter.sendEmailVerificationCode(user, "654321");
        assertThat(sent).hasSize(1);
        assertThat(sent.getFirst().to()).isEqualTo("john@example.com");
        assertThat(sent.getFirst().subject()).contains("654321");
    }
}
