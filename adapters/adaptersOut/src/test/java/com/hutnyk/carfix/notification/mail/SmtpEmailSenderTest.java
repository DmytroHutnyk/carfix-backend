package com.hutnyk.carfix.notification.mail;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmtpEmailSenderTest {

    private static final EmailMessage MESSAGE =
            new EmailMessage("john@example.com", "Hello John", "plain body", "<p>html body</p>");

    private static final class CapturingMailSender extends JavaMailSenderImpl {
        MimeMessage sent;

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            try {
                mimeMessage.saveChanges();
            } catch (MessagingException e) {
                throw new IllegalStateException(e);
            }
            this.sent = mimeMessage;
        }
    }

    private static SmtpEmailSender senderFor(CapturingMailSender mailSender) {
        return new SmtpEmailSender(mailSender, "CarFix <no-reply@carfix.local>", Runnable::run);
    }

    private static Map<String, String> bodiesByMimeType(MimeMultipart multipart) throws Exception {
        Map<String, String> bodies = new LinkedHashMap<>();
        collectLeaves(multipart, bodies);
        return bodies;
    }

    private static void collectLeaves(MimeMultipart multipart, Map<String, String> bodies) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            Object content = part.getContent();
            if (content instanceof MimeMultipart nested) {
                collectLeaves(nested, bodies);
            } else if (part.isMimeType("text/plain")) {
                bodies.put("text/plain", String.valueOf(content));
            } else if (part.isMimeType("text/html")) {
                bodies.put("text/html", String.valueOf(content));
            }
        }
    }

    @Test
    public void test_deliver_builds_a_multipart_message_with_from_to_and_subject() throws Exception {
        CapturingMailSender mailSender = new CapturingMailSender();
        senderFor(mailSender).send(MESSAGE);
        MimeMessage sent = mailSender.sent;
        assertThat(sent).isNotNull();
        assertThat(sent.getAllRecipients()).extracting(Address::toString).containsExactly("john@example.com");
        assertThat(sent.getFrom()).extracting(Address::toString).containsExactly("CarFix <no-reply@carfix.local>");
        assertThat(sent.getSubject()).isEqualTo("Hello John");
        assertThat(sent.getContent()).isInstanceOf(MimeMultipart.class);
    }

    @Test
    public void test_deliver_puts_the_text_body_in_the_plain_part_and_the_html_body_in_the_html_part() throws Exception {
        CapturingMailSender mailSender = new CapturingMailSender();
        senderFor(mailSender).send(MESSAGE);
        Map<String, String> bodies = bodiesByMimeType((MimeMultipart) mailSender.sent.getContent());
        assertThat(bodies)
                .containsEntry("text/plain", "plain body")
                .containsEntry("text/html", "<p>html body</p>");
    }
}
