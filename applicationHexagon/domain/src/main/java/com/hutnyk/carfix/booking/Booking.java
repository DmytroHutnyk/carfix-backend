package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNoShowNotAllowedException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.scheduling.SegmentPlan;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.service.Service;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hutnyk.carfix.util.Validator.*;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Booking {

    public static final Duration SAFE_CANCELLATION_NOTICE = Duration.ofHours(24);

    @EqualsAndHashCode.Include
    private final BookingId id;
    private final LocalDate date;
    @With(AccessLevel.PRIVATE)
    private final BookingStatus status;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BranchId branchId;
    private final CarProfileId carProfileId;
    private final List<BookingSegment> segments;

    @Builder
    private Booking(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            LocalTime startTime,
            LocalTime endTime,
            BranchId branchId,
            CarProfileId carProfileId,
            List<BookingSegment> segments) {
        this.id = notNull(id, "id");
        this.date = notNull(date, "date");
        this.status = notNull(status, "status");
        validTimeRange(startTime, endTime, "time");
        this.startTime = startTime;
        this.endTime = endTime;
        this.branchId = notNull(branchId, "branchId");
        this.carProfileId = notNull(carProfileId, "carProfileId");
        this.segments = notNull(segments, "segments").stream()
                .sorted(Comparator.comparing(BookingSegment::startTime))
                .toList();
    }

    /**
     * Assembles an existing booking from persistence (no creation-time checks).
     */
    public static Booking of(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            LocalTime startTime,
            LocalTime endTime,
            BranchId branchId,
            CarProfileId carProfileId,
            List<BookingSegment> segments) {
        return Booking.builder()
                .id(id)
                .date(date)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .branchId(branchId)
                .carProfileId(carProfileId)
                .segments(segments)
                .build();
    }

    /**
     * Creates a brand-new booking from a feasible visit plan: the span is the plan's span, every
     * segment snapshots the service's current price, and the start must not be before {@code now},
     * the branch's wall-clock now.
     */
    public static Booking schedule(
            BookingId id,
            BranchId branchId,
            CarProfileId carProfileId,
            VisitPlan plan,
            List<Service> services,
            LocalDateTime now) {
        notNull(plan, "plan");
        if (plan.segments().isEmpty()) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.VALUE_OUT_OF_RANGE, "segments", plan.segments());
        }
        Map<Integer, Service> byId = notNull(services, "services").stream()
                .collect(Collectors.toMap(Service::getId, Function.identity()));
        List<BookingSegment> segments = plan.segments().stream()
                .map(segment -> toSegment(segment, byId))
                .toList();
        LocalDate date = plan.start().toLocalDate();
        LocalTime startTime = plan.start().toLocalTime();
        if (plan.start().isBefore(notNull(now, "now"))) {
            throw new DomainObjectValidationException(ValidationErrorType.DATE_IN_PAST, "date", date);
        }
        return Booking.builder()
                .id(id)
                .date(date)
                .status(BookingStatus.SCHEDULED)
                .startTime(startTime)
                .endTime(plan.end().toLocalTime())
                .branchId(branchId)
                .carProfileId(carProfileId)
                .segments(segments)
                .build();
    }

    private static BookingSegment toSegment(SegmentPlan segment, Map<Integer, Service> byId) {
        Service service = byId.get(segment.serviceId());
        if (service == null) {
            throw new UnexpectedStateException(
                    "Visit plan references service " + segment.serviceId() + " outside the booked chain");
        }
        return BookingSegment.of(
                service.getId(),
                segment.time().lower().toLocalTime(),
                segment.time().upper().toLocalTime(),
                service.getPrice());
    }

    public BookingStatus effectiveStatus(LocalDateTime now) {
        return BookingLifecycle.effectiveStatus(
                status, date.atTime(startTime), date.atTime(endTime), notNull(now, "now"));
    }

    public Booking cancel(LocalDateTime now) {
        BookingStatus effective = effectiveStatus(now);
        if (effective != BookingStatus.SCHEDULED) {
            throw new BookingCancellationNotAllowedException(id.id(), effective);
        }
        return withStatus(BookingStatus.CANCELLED);
    }

    public Booking markNoShow(LocalDateTime now) {
        BookingStatus effective = effectiveStatus(now);
        if (effective == BookingStatus.NO_SHOW) {
            return this;
        }
        if (effective == BookingStatus.SCHEDULED || effective == BookingStatus.CANCELLED) {
            throw new BookingNoShowNotAllowedException(id.id(), effective);
        }
        return withStatus(BookingStatus.NO_SHOW);
    }

    public Instant safeCancelUntil(ZoneId branchZone) {
        return ZonedDateTime.of(date, startTime, branchZone)
                .minus(SAFE_CANCELLATION_NOTICE)
                .toInstant();
    }
}
