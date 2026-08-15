package com.hutnyk.carfix.notification.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class AfterCommitEmailSenderTest {

    private static final EmailMessage MESSAGE =
            new EmailMessage("john@example.com", "Hello", "text body", "<p>html body</p>");

    private static final class RecordingSender extends AfterCommitEmailSender {
        final List<EmailMessage> delivered = new ArrayList<>();
        RuntimeException failure;

        RecordingSender(Executor executor) {
            super(executor);
        }

        @Override
        protected void deliver(EmailMessage message) {
            if (failure != null) {
                throw failure;
            }
            delivered.add(message);
        }
    }

    @AfterEach
    public void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void test_without_a_transaction_the_message_is_delivered_right_away_on_the_executor() {
        //given
        RecordingSender sender = new RecordingSender(Runnable::run);
        //when
        sender.send(MESSAGE);
        //then
        assertThat(sender.delivered).containsExactly(MESSAGE);
    }

    @Test
    public void test_inside_a_transaction_delivery_waits_for_after_commit() {
        //given
        RecordingSender sender = new RecordingSender(Runnable::run);
        TransactionSynchronizationManager.initSynchronization();
        //when
        sender.send(MESSAGE);
        //then
        assertThat(sender.delivered).isEmpty();
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        assertThat(sender.delivered).containsExactly(MESSAGE);
    }

    @Test
    public void test_a_delivery_failure_never_reaches_the_caller() {
        //given
        RecordingSender sender = new RecordingSender(Runnable::run);
        sender.failure = new IllegalStateException("smtp down");
        //when + then
        assertThatCode(() -> sender.send(MESSAGE)).doesNotThrowAnyException();
        assertThat(sender.delivered).isEmpty();
    }

    @Test
    public void test_a_rejected_execution_never_reaches_the_caller() {
        //given
        RecordingSender sender = new RecordingSender(command -> {
            throw new RejectedExecutionException("pool closed");
        });
        //when + then
        assertThatCode(() -> sender.send(MESSAGE)).doesNotThrowAnyException();
    }
}
