package com.hutnyk.carfix.notification.mail;

import java.util.Objects;

public record EmailMessage(String to, String subject, String textBody, String htmlBody) {

    public EmailMessage {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(textBody, "textBody");
        Objects.requireNonNull(htmlBody, "htmlBody");
    }
}
