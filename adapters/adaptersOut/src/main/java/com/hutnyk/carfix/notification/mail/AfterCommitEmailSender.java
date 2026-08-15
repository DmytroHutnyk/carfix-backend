package com.hutnyk.carfix.notification.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;

/**
 * Delivery contract shared by every {@link EmailSender}: the message is handed to the executor only after the
 * surrounding transaction commits (immediately when there is none), and a failed delivery is logged, never
 * propagated — an outage of the mail server must not fail the use case that produced the email.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AfterCommitEmailSender implements EmailSender {

    private final Executor executor;

    @Override
    public final void send(EmailMessage message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch(message);
                }
            });
        } else {
            dispatch(message);
        }
    }

    protected abstract void deliver(EmailMessage message);

    private void dispatch(EmailMessage message) {
        try {
            executor.execute(() -> deliverSafely(message));
        } catch (RuntimeException e) {
            log.error("Email could not be scheduled: to={}, subject={}", message.to(), message.subject(), e);
        }
    }

    private void deliverSafely(EmailMessage message) {
        try {
            deliver(message);
        } catch (RuntimeException e) {
            log.error("Email delivery failed: to={}, subject={}", message.to(), message.subject(), e);
        }
    }
}
