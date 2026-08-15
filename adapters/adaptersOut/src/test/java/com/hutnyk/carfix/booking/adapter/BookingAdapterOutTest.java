package com.hutnyk.carfix.booking.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.employee.repository.EmployeeBookingRepository;
import com.hutnyk.carfix.equipment.repository.EquipmentBookingRepository;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayBookingRepository;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private static <T> T recordingRepository(Class<T> type, List<String> calls) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if ("deleteAllByBookingEntityId".equals(method.getName())) {
                calls.add(type.getSimpleName() + ":" + args[0]);
                return null;
            }
            if ("toString".equals(method.getName())) return type.getSimpleName();
            if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
            if ("equals".equals(method.getName())) return proxy == args[0];
            throw new UnsupportedOperationException(method.getName());
        }));
    }

    @Test
    public void freeOccupancyDeletesFromAllThreeOccupancyTablesByBookingId() {
        List<String> calls = new ArrayList<>();
        BookingAdapterOut adapter = new BookingAdapterOut(null, null,
                recordingRepository(ServiceBayBookingRepository.class, calls),
                recordingRepository(EmployeeBookingRepository.class, calls),
                recordingRepository(EquipmentBookingRepository.class, calls));
        UUID id = UUID.randomUUID();

        adapter.freeOccupancy(BookingId.of(id));

        assertThat(calls).containsExactlyInAnyOrder(
                "ServiceBayBookingRepository:" + id,
                "EmployeeBookingRepository:" + id,
                "EquipmentBookingRepository:" + id);
    }
}
