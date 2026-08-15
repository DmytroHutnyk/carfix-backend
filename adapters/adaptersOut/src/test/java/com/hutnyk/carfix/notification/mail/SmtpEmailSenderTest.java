package com.hutnyk.carfix.notification.mail;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class SmtpEmailSenderTest {

    private static final class CapturingMailSender extends JavaMailSenderImpl {
        MimeMessage sent;

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            this.sent = mimeMessage;
        }
    }

    @Test
    public void test_deliver_builds_a_multipart_message_with_from_to_subject_and_both_bodies() throws Exception {
        //given
        CapturingMailSender mailSender = new CapturingMailSender();
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, "CarFix <no-reply@carfix.local>", Runnable::run);
        //when
        sender.send(new EmailMessage("john@example.com", "Hello John", "plain body", "<p>html body</p>"));
        //then
        MimeMessage sent = mailSender.sent;
        assertThat(sent).isNotNull();
        assertThat(sent.getAllRecipients()).extracting(Address::toString).containsExactly("john@example.com");
        assertThat(sent.getFrom()).extracting(Address::toString).containsExactly("CarFix <no-reply@carfix.local>");
        assertThat(sent.getSubject()).isEqualTo("Hello John");
        assertThat(sent.getContent()).isInstanceOf(MimeMultipart.class);
    }
}
