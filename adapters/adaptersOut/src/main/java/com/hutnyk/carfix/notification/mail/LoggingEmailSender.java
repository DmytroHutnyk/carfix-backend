package com.hutnyk.carfix.notification.mail;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class LoggingEmailSender extends AfterCommitEmailSender {

    public LoggingEmailSender(Executor executor) {
        super(executor);
    }

    @Override
    protected void deliver(EmailMessage message) {
        log.info("[mail disabled — app.mail.enabled=false] To: {} | Subject: {}\n{}",
                message.to(), message.subject(), message.textBody());
    }
}
