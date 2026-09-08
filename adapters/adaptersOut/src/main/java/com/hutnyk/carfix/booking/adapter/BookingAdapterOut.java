package com.hutnyk.carfix.booking.adapter;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingOccupancy;
import com.hutnyk.carfix.booking.BookingSegment;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentEntity;
import com.hutnyk.carfix.booking.exception.SlotNotAvailableException;
import com.hutnyk.carfix.booking.mapper.BookingMapper;
import com.hutnyk.carfix.booking.repository.BookingRepository;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.employee.mapper.EmployeeMapper;
import com.hutnyk.carfix.employee.repository.EmployeeBookingRepository;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import com.hutnyk.carfix.equipment.mapper.EquipmentMapper;
import com.hutnyk.carfix.equipment.repository.EquipmentBookingRepository;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import com.hutnyk.carfix.serviceBay.mapper.ServiceBayMapper;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayBookingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@PersistenceAdapter
public class BookingAdapterOut implements BookingPortOut {

    /* 23P01 exclusion_violation: the GiST constraint said no. 40P01 deadlock_detected / 40001
       serialization_failure: Postgres aborted us while two writers waited on each other's rows —
       equally "somebody else got there first", never a defect to 500 on. */
    private static final Set<String> LOST_RACE_SQL_STATES = Set.of("23P01", "40P01", "40001");
    private static final int MAX_CAUSE_DEPTH = 20;

    private final EntityManager entityManager;
    private final Clock clock;
    private final BookingRepository bookingRepository;
    private final ServiceBayBookingRepository serviceBayBookingRepository;
    private final EmployeeBookingRepository employeeBookingRepository;
    private final EquipmentBookingRepository equipmentBookingRepository;

    @Override
    public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
        Instant now = clock.instant();
        return bookingRepository.findAllByCustomerIdWithDetails(customerId).stream()
                .map(entity -> BookingMapper.toView(entity, now))
                .toList();
    }

    @Override
    public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
                .map(entity -> BookingMapper.toView(entity, clock.instant()));
    }

    @Override
    public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
                .map(BookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findByIdAndOwnerId(UUID bookingId, UUID ownerId) {
        return bookingRepository.findByIdAndOwnerIdWithSegments(bookingId, ownerId)
                .map(BookingMapper::toDomain);
    }

    @Override
    public void insert(Booking booking, BookingOccupancy occupancy) {
        /* getReference sets FKs without loading rows the write does not need. */
        BranchEntity branch = entityManager.getReference(BranchEntity.class, booking.getBranchId().id());
        CarProfileEntity carProfile =
                entityManager.getReference(CarProfileEntity.class, booking.getCarProfileId().id());
        BookingEntity bookingEntity = BookingMapper.toEntity(booking, branch, carProfile);
        try {
            entityManager.persist(bookingEntity);
            for (BookingSegment segment : booking.getSegments()) {
                ServiceEntity service = entityManager.getReference(ServiceEntity.class, segment.serviceId());
                BookingSegmentEntity segmentEntity =
                        BookingMapper.toSegmentEntity(segment, bookingEntity, service);
                entityManager.persist(segmentEntity);
                /* Keep the inverse side in sync: a read in this same persistence context gets the
                   cached booking back, and Hibernate never re-initializes an initialized collection. */
                bookingEntity.getSegments().add(segmentEntity);
            }
            /* Booking and segments must be in the DB before the identity-generated occupancy rows
               reference them; the flush also surfaces any segment-level constraint failure first. */
            entityManager.flush();
            for (ServiceBayBooking bay : occupancy.bays()) {
                ServiceBayEntity ref = entityManager.getReference(ServiceBayEntity.class, bay.getServiceBayId());
                entityManager.persist(ServiceBayMapper.toEntity(bay, ref, bookingEntity));
            }
            for (EmployeeBooking employee : occupancy.employees()) {
                EmployeeEntity ref = entityManager.getReference(EmployeeEntity.class, employee.getEmployeeId().id());
                entityManager.persist(EmployeeMapper.toEntity(employee, ref, bookingEntity));
            }
            for (EquipmentBooking unit : occupancy.equipment()) {
                EquipmentEntity ref = entityManager.getReference(EquipmentEntity.class, unit.getEquipmentId());
                entityManager.persist(EquipmentMapper.toEntity(unit, ref, bookingEntity));
            }
            entityManager.flush();
        } catch (RuntimeException e) {
            if (isLostSlotRace(e)) {
                /* An expected race, not a defect: warn without a stack trace, but name the constraint
                   so a systematic collision is still visible in the log. */
                log.warn("Booking {} for {} {} lost the slot race: {}",
                        booking.getId().id(), booking.getDate(), booking.getStartTime(), violationDetail(e));
                throw new SlotNotAvailableException(booking.getDate(), booking.getStartTime());
            }
            throw e;
        }
    }

    @Override
    public Booking update(Booking booking) {
        BookingEntity entity = bookingRepository.findById(booking.getId().id())
                .orElseThrow(() -> new UnexpectedStateException(
                        "Booking row missing on update: " + booking.getId().id()));
        BookingMapper.updateEntity(entity, booking);
        return BookingMapper.toDomain(bookingRepository.save(entity));
    }

    @Override
    public void freeOccupancy(BookingId bookingId) {
        UUID id = bookingId.id();
        serviceBayBookingRepository.deleteAllByBookingEntityId(id);
        employeeBookingRepository.deleteAllByBookingEntityId(id);
        equipmentBookingRepository.deleteAllByBookingEntityId(id);
    }

    @Override
    public void deleteAllByCustomerId(UUID customerId) {
        List<UUID> bookingIds = bookingRepository.findIdsByCustomerId(customerId);
        if (bookingIds.isEmpty()) {
            return;
        }
        for (UUID id : bookingIds) {
            serviceBayBookingRepository.deleteAllByBookingEntityId(id);
            employeeBookingRepository.deleteAllByBookingEntityId(id);
            equipmentBookingRepository.deleteAllByBookingEntityId(id);
        }
        bookingRepository.deleteSegmentsByBookingIds(bookingIds);
        bookingRepository.deleteByIds(bookingIds);
    }

    @Override
    public boolean existsActiveOverlapping(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end) {
        return bookingRepository.existsByCarProfileOverlapping(
                carProfileId.id(), date, start, end, EnumSet.of(BookingStatus.CANCELLED, BookingStatus.NO_SHOW));
    }

    /* Postgres reports a lost race as one of a few SQLSTATEs. Hibernate wraps it in a
       ConstraintViolationException, Spring may wrap that again — walk the chain to the SQLException. */
    static boolean isLostSlotRace(Throwable t) {
        for (Throwable c : causeChain(t)) {
            /* Set.of rejects a null lookup with an NPE, and a SQLState is nullable — an unrelated
               driver failure must not blow up the handler that is inspecting it. */
            if (c instanceof SQLException sql && sql.getSQLState() != null
                    && LOST_RACE_SQL_STATES.contains(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    /* The constraint Hibernate names, plus the driver's own message — the two things that say which
       resource collided. */
    private static String violationDetail(Throwable t) {
        String constraint = null;
        for (Throwable c : causeChain(t)) {
            if (c instanceof ConstraintViolationException hibernate && hibernate.getConstraintName() != null) {
                constraint = hibernate.getConstraintName();
            }
            if (c instanceof SQLException sql) {
                return (constraint == null ? "" : constraint + ": ")
                        + "SQLSTATE " + sql.getSQLState() + " " + sql.getMessage();
            }
        }
        return String.valueOf(t);
    }

    /* Capped rather than cycle-checked: a chain that loops back on itself, directly or through
       another link, must not spin the walk forever. */
    private static List<Throwable> causeChain(Throwable t) {
        List<Throwable> chain = new ArrayList<>();
        for (Throwable c = t; c != null && chain.size() < MAX_CAUSE_DEPTH; c = c.getCause()) {
            chain.add(c);
        }
        return chain;
    }
}
