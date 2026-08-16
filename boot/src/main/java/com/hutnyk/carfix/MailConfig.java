package com.hutnyk.carfix;

import com.hutnyk.carfix.notification.mail.EmailSender;
import com.hutnyk.carfix.notification.mail.LoggingEmailSender;
import com.hutnyk.carfix.notification.mail.SmtpEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    /**
     * Real SMTP delivery — {@code app.mail.enabled=true}; connection settings come from {@code spring.mail.*}.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
    public EmailSender smtpEmailSender(JavaMailSender javaMailSender,
                                       @Value("${app.mail.from}") String from,
                                       TaskExecutor taskExecutor) {
        return new SmtpEmailSender(javaMailSender, from, taskExecutor);
    }

    /**
     * Default for local development: every email is written to the log instead of being sent.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
    public EmailSender loggingEmailSender(TaskExecutor taskExecutor) {
        return new LoggingEmailSender(taskExecutor);
    }
}
