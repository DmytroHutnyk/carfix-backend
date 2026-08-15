package com.hutnyk.carfix.booking.adapter;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingOccupancy;
import com.hutnyk.carfix.booking.BookingSegment;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentEntity;
import com.hutnyk.carfix.booking.exception.SlotNotAvailableException;
import com.hutnyk.carfix.booking.mapper.BookingMapper;
import com.hutnyk.carfix.booking.repository.BookingRepository;
import com.hutnyk.carfix.branch.entity.BranchEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@PersistenceAdapter
public class BookingAdapterOut implements BookingPortOut {

    private static final String EXCLUSION_VIOLATION_SQL_STATE = "23P01";
    private static final int MAX_CAUSE_DEPTH = 20;

    private final EntityManager entityManager;
    private final BookingRepository bookingRepository;
    private final ServiceBayBookingRepository serviceBayBookingRepository;
    private final EmployeeBookingRepository employeeBookingRepository;
    private final EquipmentBookingRepository equipmentBookingRepository;

    @Override
    public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
        return bookingRepository.findAllByCustomerIdWithDetails(customerId).stream()
                .map(BookingMapper::toView)
                .toList();
    }

    @Override
    public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
                .map(BookingMapper::toView);
    }

    @Override
    public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
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
            if (isExclusionViolation(e)) {
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

    /* Postgres reports an EXCLUDE violation as SQLSTATE 23P01. Hibernate wraps it in a
       ConstraintViolationException, Spring may wrap that again — walk the chain to the SQLException. */
    static boolean isExclusionViolation(Throwable t) {
        for (Throwable c : causeChain(t)) {
            if (c instanceof SQLException sql && EXCLUSION_VIOLATION_SQL_STATE.equals(sql.getSQLState())) {
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
                return constraint == null ? sql.getMessage() : constraint + ": " + sql.getMessage();
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
