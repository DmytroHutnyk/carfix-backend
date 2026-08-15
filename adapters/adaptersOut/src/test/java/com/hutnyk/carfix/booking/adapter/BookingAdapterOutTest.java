package com.hutnyk.carfix.booking.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Duration;

public class BookingAdapterOutTest {

    private static SQLException sqlState(String state) {
        return new SQLException("conflicting key value violates exclusion constraint", state);
    }

    @Test
    public void detectsExclusionViolationThroughHibernateAndSpringWrappers() {
        Throwable hibernate = new PersistenceException(new ConstraintViolationException(
                "could not execute statement", sqlState("23P01"), "gist_booked_time_exclusion_service_bays_bookings"));
        Throwable spring = new DataIntegrityViolationException("could not execute statement", new ConstraintViolationException(
                "could not execute statement", sqlState("23P01"), "gist_time_exclusion_employees_bookings"));

        assertThat(BookingAdapterOut.isExclusionViolation(hibernate)).isTrue();
        assertThat(BookingAdapterOut.isExclusionViolation(spring)).isTrue();
    }

    @Test
    public void ignoresOtherSqlStatesAndNonSqlFailures() {
        Throwable unique = new PersistenceException(new ConstraintViolationException(
                "duplicate key", sqlState("23505"), "bookings_services_pk"));

        assertThat(BookingAdapterOut.isExclusionViolation(unique)).isFalse();
        assertThat(BookingAdapterOut.isExclusionViolation(new IllegalStateException("boom"))).isFalse();
        assertThat(BookingAdapterOut.isExclusionViolation(null)).isFalse();
    }

    @Test
    public void terminatesOnACauseChainThatLoopsBackOnItself() {
        Throwable first = new IllegalStateException("first");
        Throwable second = new IllegalStateException("second", first);
        first.initCause(second);

        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThat(BookingAdapterOut.isExclusionViolation(first)).isFalse());
    }
}
