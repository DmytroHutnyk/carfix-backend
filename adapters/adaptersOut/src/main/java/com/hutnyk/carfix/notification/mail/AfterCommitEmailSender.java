package com.hutnyk.carfix.notification.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;

/** Sends after commit and logs delivery failures without failing the originating use case. */
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
            log.error("Email could not be scheduled: to={}", message.to(), e);
        }
    }

    private void deliverSafely(EmailMessage message) {
        try {
            deliver(message);
        } catch (RuntimeException e) {
            log.error("Email delivery failed: to={}", message.to(), e);
        }
    }
}
