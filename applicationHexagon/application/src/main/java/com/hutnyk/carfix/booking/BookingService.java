package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.booking.exception.CarProfileAlreadyBookedException;
import com.hutnyk.carfix.booking.exception.InvalidBookingRequestException;
import com.hutnyk.carfix.booking.exception.SlotNotAvailableException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.booking.BookingPortIn;
import com.hutnyk.carfix.in.booking.commands.CreateBookingCommand;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.BranchResources;
import com.hutnyk.carfix.scheduling.BranchScheduleLoader;
import com.hutnyk.carfix.scheduling.DaySchedules;
import com.hutnyk.carfix.scheduling.SlotCalculator;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.service.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationService
public class BookingService implements BookingPortIn {

    private final CustomerPortOut customerPortOut;
    private final BookingPortOut bookingPortOut;
    private final CarProfilePortOut carProfilePortOut;
    private final BranchPortOut branchPortOut;
    private final BranchScheduleLoader scheduleLoader;
    private final Clock clock;

    public BookingService(CustomerPortOut customerPortOut,
                          BookingPortOut bookingPortOut,
                          CarProfilePortOut carProfilePortOut,
                          BranchPortOut branchPortOut,
                          ServicePortOut servicePortOut,
                          AvailabilityPortOut availabilityPortOut,
                          Clock clock) {
        this.customerPortOut = customerPortOut;
        this.bookingPortOut = bookingPortOut;
        this.carProfilePortOut = carProfilePortOut;
        this.branchPortOut = branchPortOut;
        this.scheduleLoader = new BranchScheduleLoader(servicePortOut, availabilityPortOut);
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingView> getMyBookings(String customerEmail) {
        Customer customer = customerPortOut.loadCustomerByUsername(customerEmail);
        return bookingPortOut.findAllViewsByCustomerId(customer.getUser().getId().id());
    }

    @Override
    public BookingView createBooking(String customerEmail, CreateBookingCommand cmd) {
        validateShape(cmd);

        Customer customer = customerPortOut.loadCustomerByUsername(customerEmail);
        UUID customerId = customer.getUser().getId().id();
        if (!carProfilePortOut.existsByIdAndCustomerId(cmd.carProfileId(), customerId)) {
            throw new CarProfileNotFoundException(cmd.carProfileId());
        }

        BranchId branchId = BranchId.of(cmd.branchId());
        ZoneId branchZone = branchPortOut.findActiveBranchZone(branchId)
                .orElseThrow(() -> new BranchNotFoundException(cmd.branchId()));
        LocalDateTime now = LocalDateTime.now(clock.withZone(branchZone));

        LocalDateTime start = cmd.date().atTime(cmd.startTime());
        if (start.isBefore(now)) {
            throw new InvalidBookingRequestException("startTime", "start must not be in the past");
        }

        List<Service> services = scheduleLoader.loadServices(cmd.serviceIds(), branchId);
        VisitPlan plan = planVisit(branchId, services, start, now)
                .orElseThrow(() -> new SlotNotAvailableException(cmd.date(), cmd.startTime()));

        CarProfileId carProfileId = CarProfileId.of(cmd.carProfileId());
        if (bookingPortOut.existsActiveOverlapping(carProfileId, plan.start().toLocalDate(),
                plan.start().toLocalTime(), plan.end().toLocalTime())) {
            throw new CarProfileAlreadyBookedException(carProfileId, plan.start().toLocalDate(),
                    plan.start().toLocalTime(), plan.end().toLocalTime());
        }

        Booking booking = Booking.schedule(
                BookingId.genId(), branchId, carProfileId, plan, services, now);
        bookingPortOut.insert(booking, BookingOccupancy.of(booking.getId(), plan));

        return bookingPortOut.findViewByIdAndCustomerId(booking.getId().id(), customerId)
                .orElseThrow(() -> new UnexpectedStateException(
                        "Booking disappeared right after insert: " + booking.getId().id()));
    }

    @Override
    public BookingView cancelBooking(String customerEmail, UUID bookingId) {
        Customer customer = customerPortOut.loadCustomerByUsername(customerEmail);
        UUID customerId = customer.getUser().getId().id();

        Booking booking = bookingPortOut.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        bookingPortOut.update(booking.cancel());
        bookingPortOut.freeOccupancy(booking.getId());

        return bookingPortOut.findViewByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new UnexpectedStateException(
                        "Booking disappeared right after cancel: " + bookingId));
    }

    private Optional<VisitPlan> planVisit(BranchId branchId, List<Service> services,
                                          LocalDateTime start, LocalDateTime notBefore) {
        Set<Integer> commonBayTypes = BranchScheduleLoader.commonBayTypes(services);
        if (commonBayTypes.isEmpty()) {
            return Optional.empty();
        }
        BranchResources resources = scheduleLoader.loadResources(branchId, services, commonBayTypes);
        if (!resources.canServe()) {
            return Optional.empty();
        }
        LocalDate date = start.toLocalDate();
        DaySchedules day = scheduleLoader.loadSchedules(resources, date, date).get(date);
        return SlotCalculator.planVisit(services, day.bays(), day.employees(), day.equipment(), start, notBefore);
    }

    private static void validateShape(CreateBookingCommand cmd) {
        List<Integer> ids = cmd.serviceIds();
        if (ids == null || ids.isEmpty()) {
            throw new InvalidBookingRequestException("serviceIds", "at least one serviceId is required");
        }
        if (ids.size() > SlotCalculator.MAX_SERVICES_PER_VISIT) {
            throw new InvalidBookingRequestException("serviceIds",
                    "at most " + SlotCalculator.MAX_SERVICES_PER_VISIT + " services per visit");
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new InvalidBookingRequestException("serviceIds", "serviceIds must be distinct");
        }
        if (cmd.date() == null) {
            throw new InvalidBookingRequestException("date", "date is required");
        }
        if (cmd.startTime() == null) {
            throw new InvalidBookingRequestException("startTime", "startTime is required");
        }
        if (!SlotCalculator.isOnGrid(cmd.startTime())) {
            throw new InvalidBookingRequestException("startTime",
                    "startTime must be on the 15-minute grid");
        }
    }
}
