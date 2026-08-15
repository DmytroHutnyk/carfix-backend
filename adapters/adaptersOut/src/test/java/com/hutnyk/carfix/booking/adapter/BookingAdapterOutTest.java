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

        assertThat(BookingAdapterOut.isLostSlotRace(hibernate)).isTrue();
        assertThat(BookingAdapterOut.isLostSlotRace(spring)).isTrue();
    }

    @Test
    public void ignoresOtherSqlStatesAndNonSqlFailures() {
        Throwable unique = new PersistenceException(new ConstraintViolationException(
                "duplicate key", sqlState("23505"), "bookings_services_pk"));

        assertThat(BookingAdapterOut.isLostSlotRace(unique)).isFalse();
        assertThat(BookingAdapterOut.isLostSlotRace(new IllegalStateException("boom"))).isFalse();
        assertThat(BookingAdapterOut.isLostSlotRace(null)).isFalse();
    }

    @Test
    public void terminatesOnACauseChainThatLoopsBackOnItself() {
        Throwable first = new IllegalStateException("first");
        Throwable second = new IllegalStateException("second", first);
        first.initCause(second);

        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThat(BookingAdapterOut.isLostSlotRace(first)).isFalse());
    }

    @Test
    public void treatsDeadlockAndSerializationFailuresAsALostRace() {
        assertThat(BookingAdapterOut.isLostSlotRace(new PersistenceException(sqlState("40P01")))).isTrue();
        assertThat(BookingAdapterOut.isLostSlotRace(new PersistenceException(sqlState("40001")))).isTrue();
        assertThat(BookingAdapterOut.isLostSlotRace(new PersistenceException(sqlState("23505")))).isFalse();
    }

    @Test
    public void survivesASqlExceptionThatCarriesNoSqlState() {
        assertThat(BookingAdapterOut.isLostSlotRace(new PersistenceException(new SQLException("connection reset"))))
                .isFalse();
    }
}
