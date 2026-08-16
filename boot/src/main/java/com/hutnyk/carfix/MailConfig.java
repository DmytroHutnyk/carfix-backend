package com.hutnyk.carfix;

import com.hutnyk.carfix.notification.mail.EmailSender;
import com.hutnyk.carfix.notification.mail.LoggingEmailSender;
import com.hutnyk.carfix.notification.mail.SmtpEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    /* Log-only by default; app.mail.enabled=true switches to SMTP. */
    @Bean
    public EmailSender emailSender(@Value("${app.mail.enabled}") boolean enabled,
                                   @Value("${app.mail.from}") String from,
                                   JavaMailSender javaMailSender,
                                   TaskExecutor taskExecutor) {
        return enabled
                ? new SmtpEmailSender(javaMailSender, from, taskExecutor)
                : new LoggingEmailSender(taskExecutor);
    }
}
