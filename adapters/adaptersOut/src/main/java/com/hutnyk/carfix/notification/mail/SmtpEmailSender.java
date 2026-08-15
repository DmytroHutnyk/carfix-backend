package com.hutnyk.carfix.notification.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public class SmtpEmailSender extends AfterCommitEmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, String from, Executor executor) {
        super(executor);
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    protected void deliver(EmailMessage message) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.textBody(), message.htmlBody());
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not build email to " + message.to(), e);
        }
        mailSender.send(mimeMessage);
    }
}
