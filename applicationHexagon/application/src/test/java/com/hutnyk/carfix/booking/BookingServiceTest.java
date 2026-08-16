package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.booking.exception.CarProfileAlreadyBookedException;
import com.hutnyk.carfix.booking.exception.InvalidBookingRequestException;
import com.hutnyk.carfix.booking.exception.SlotNotAvailableException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.booking.commands.CreateBookingCommand;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.exception.ServiceNotFoundException;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BookingServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId CUSTOMER_ID = UserId.genId();
    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final LocalDate TODAY = LocalDate.of(2030, 6, 12);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final LocalDate SATURDAY = LocalDate.of(2030, 6, 15);
    private static final Clock CLOCK = Clock.fixed(
            TODAY.atTime(10, 7).atZone(WARSAW).toInstant(), ZoneId.of("UTC"));
    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UUID CAR_PROFILE_ID = UUID.randomUUID();
    private static final UUID ANNA = UUID.randomUUID();
    private static final int LIFT = 1;
    private static final int PIT = 2;
    private static final int MECHANIC = 10;
    private static final int BAY_ID = 100;

    private static Customer customer() {
        return Customer.of(
                User.builder()
                        .id(CUSTOMER_ID)
                        .name("John")
                        .surname("Doe")
                        .phoneNumber(new PhoneNumber("+48", "123456789"))
                        .email(EMAIL)
                        .role(UserRole.CUSTOMER)
                        .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                        .dateOfBirth(LocalDate.of(1990, 5, 1))
                        .addressId(null)
                        .build(),
                CustomerStatus.ACTIVE);
    }

    private static Service service(int id, Set<Integer> bayTypes, String price) {
        return Service.of(id, "Service " + id, null, (short) 60, new BigDecimal(price),
                ServiceStatus.ACTIVE, BRANCH_ID, 1, bayTypes,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
    }

    private static List<OpeningHours> allWeek(LocalTime open, LocalTime close) {
        return Arrays.stream(DayOfWeek.values())
                .map(day -> OpeningHours.of(null, day, open, close, BRANCH_ID))
                .toList();
    }

    private static List<OpeningHours> monToFri(LocalTime open, LocalTime close) {
        return Arrays.stream(DayOfWeek.values())
                .filter(day -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY)
                .map(day -> OpeningHours.of(null, day, open, close, BRANCH_ID))
                .toList();
    }

    private static final class StubCustomerPortOut implements CustomerPortOut {
        @Override
        public Customer insertCustomer(Customer c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Customer loadCustomerByUsername(String email) {
            return customer();
        }
    }

    private static final class StubCarProfilePortOut implements CarProfilePortOut {
        boolean owned = true;
        UUID requestedCustomerId;

        @Override
        public boolean existsByIdAndCustomerId(UUID profileId, UUID customerId) {
            this.requestedCustomerId = customerId;
            return owned;
        }

        @Override
        public List<CarProfileView> findAllByCustomerId(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CarProfileView> findByIdAndCustomerId(UUID profileId, UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarProfile insert(CarProfile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CarProfile update(CarProfile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(UUID profileId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubBranchPortOut implements BranchPortOut {
        boolean exists = true;
        BranchId lastZoneBranchId;

        @Override
        public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
            this.lastZoneBranchId = branchId;
            return exists ? Optional.of(WARSAW) : Optional.empty();
        }

        @Override
        public boolean existsActiveById(BranchId branchId) {
            return exists;
        }

        @Override
        public void updateRating(BranchId branchId, BranchRating rating) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BranchView> findViewById(BranchId branchId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();
        boolean loaded;
        Collection<Integer> lastLoadByIds;

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
            this.loaded = true;
            this.lastLoadByIds = serviceIds;
            return toReturn;
        }
    }

    private static final class StubAvailabilityPortOut implements AvailabilityPortOut {
        List<ServiceBay> bays = new ArrayList<>();
        List<EmployeeCandidateView> employees = new ArrayList<>();
        List<Equipment> equipment = new ArrayList<>();
        List<ServiceBayAvailability> bayAvailability = new ArrayList<>();
        List<ServiceBayBooking> bayOccupancy = new ArrayList<>();
        List<EmployeeAvailability> employeeAvailability = new ArrayList<>();
        List<EmployeeBooking> employeeOccupancy = new ArrayList<>();
        List<EquipmentAvailability> equipmentAvailability = new ArrayList<>();
        List<EquipmentBooking> equipmentOccupancy = new ArrayList<>();
        List<OpeningHours> openingHours = new ArrayList<>(allWeek(LocalTime.of(6, 0), LocalTime.of(22, 0)));
        List<OpeningHoursException> openingHoursExceptions = new ArrayList<>();

        BranchId lastOpeningHoursBranchId;
        LocalDate lastExceptionsFrom;
        LocalDate lastExceptionsTo;
        BranchId lastBaysBranchId;
        BranchId lastEmployeesBranchId;
        BranchId lastEquipmentBranchId;
        Collection<Integer> lastBayAvailIds;
        LocalDate lastBayAvailFrom;
        LocalDate lastBayAvailTo;
        Collection<Integer> lastBayOccIds;
        LocalDate lastBayOccFrom;
        LocalDate lastBayOccTo;
        Collection<UUID> lastEmployeeAvailIds;
        LocalDate lastEmployeeAvailFrom;
        LocalDate lastEmployeeAvailTo;
        Collection<UUID> lastEmployeeOccIds;
        LocalDate lastEmployeeOccFrom;
        LocalDate lastEmployeeOccTo;
        Collection<Integer> lastEquipmentAvailIds;
        LocalDate lastEquipmentAvailFrom;
        LocalDate lastEquipmentAvailTo;
        Collection<Integer> lastEquipmentOccIds;
        LocalDate lastEquipmentOccFrom;
        LocalDate lastEquipmentOccTo;

        @Override public List<ServiceBay> loadActiveBays(BranchId branchId) {
            this.lastBaysBranchId = branchId;
            return bays;
        }

        @Override public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
            this.lastEmployeesBranchId = branchId;
            return employees;
        }

        @Override public List<Equipment> loadActiveEquipment(BranchId branchId) {
            this.lastEquipmentBranchId = branchId;
            return equipment;
        }

        @Override public List<ServiceBayAvailability> loadBayAvailability(Collection<Integer> ids, LocalDate from, LocalDate to) {
            this.lastBayAvailIds = ids;
            this.lastBayAvailFrom = from;
            this.lastBayAvailTo = to;
            return bayAvailability;
        }

        @Override public List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> ids, LocalDate from, LocalDate to) {
            this.lastBayOccIds = ids;
            this.lastBayOccFrom = from;
            this.lastBayOccTo = to;
            return bayOccupancy;
        }

        @Override public List<EmployeeAvailability> loadEmployeeAvailability(Collection<UUID> ids, LocalDate from, LocalDate to) {
            this.lastEmployeeAvailIds = ids;
            this.lastEmployeeAvailFrom = from;
            this.lastEmployeeAvailTo = to;
            return employeeAvailability;
        }

        @Override public List<EmployeeBooking> loadEmployeeOccupancy(Collection<UUID> ids, LocalDate from, LocalDate to) {
            this.lastEmployeeOccIds = ids;
            this.lastEmployeeOccFrom = from;
            this.lastEmployeeOccTo = to;
            return employeeOccupancy;
        }

        @Override public List<EquipmentAvailability> loadEquipmentAvailability(Collection<Integer> ids, LocalDate from, LocalDate to) {
            this.lastEquipmentAvailIds = ids;
            this.lastEquipmentAvailFrom = from;
            this.lastEquipmentAvailTo = to;
            return equipmentAvailability;
        }

        @Override public List<EquipmentBooking> loadEquipmentOccupancy(Collection<Integer> ids, LocalDate from, LocalDate to) {
            this.lastEquipmentOccIds = ids;
            this.lastEquipmentOccFrom = from;
            this.lastEquipmentOccTo = to;
            return equipmentOccupancy;
        }

        @Override public List<OpeningHours> loadOpeningHours(BranchId branchId) {
            this.lastOpeningHoursBranchId = branchId;
            return openingHours;
        }

        @Override public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
            this.lastExceptionsFrom = from;
            this.lastExceptionsTo = to;
            return openingHoursExceptions;
        }
    }

    private static Booking booking(BookingStatus status) {
        return Booking.of(
                BookingId.of(BOOKING_ID),
                LocalDate.of(2030, 6, 12),
                status,
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                BranchId.genId(),
                CarProfileId.genId(),
                List.of());
    }

    private static BookingView view(BookingStatus status) {
        return new BookingView(
                BOOKING_ID,
                LocalDate.of(2030, 6, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                status,
                Instant.parse("2030-06-11T08:00:00Z"),
                UUID.randomUUID(),
                "SpeedCare Wola",
                "+48123456789",
                "wola@speedcare.pl",
                "Górczewska",
                "110",
                "Warszawa",
                UUID.randomUUID(),
                "Weekend Car",
                "BMW",
                "X5",
                "KR 67890",
                List.of(new BookingServiceView("Diagnostics", new BigDecimal("150.00"))),
                new BigDecimal("150.00"));
    }

    private static final class StubBookingPortOut implements BookingPortOut {
        Booking stored;
        Booking updated;
        Booking inserted;
        BookingOccupancy insertedOccupancy;
        UUID requestedCustomerId;
        BookingId freedOccupancyFor;
        boolean carAlreadyBooked = false;
        CarProfileId overlapCarProfileId;
        LocalDate overlapDate;
        LocalTime overlapStart;
        LocalTime overlapEnd;

        @Override
        public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
            this.requestedCustomerId = customerId;
            return List.of(view(BookingStatus.SCHEDULED));
        }

        @Override
        public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
            this.requestedCustomerId = customerId;
            if (inserted != null && inserted.getId().id().equals(bookingId)) {
                return Optional.of(view(BookingStatus.SCHEDULED));
            }
            return stored == null ? Optional.empty() : Optional.of(view(BookingStatus.CANCELLED));
        }

        @Override
        public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
            this.requestedCustomerId = customerId;
            return Optional.ofNullable(stored);
        }

        @Override
        public void insert(Booking booking, BookingOccupancy occupancy) {
            this.inserted = booking;
            this.insertedOccupancy = occupancy;
        }

        @Override
        public Booking update(Booking booking) {
            this.updated = booking;
            return booking;
        }

        @Override
        public void freeOccupancy(BookingId bookingId) {
            this.freedOccupancyFor = bookingId;
        }

        @Override
        public boolean existsActiveOverlapping(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end) {
            this.overlapCarProfileId = carProfileId;
            this.overlapDate = date;
            this.overlapStart = start;
            this.overlapEnd = end;
            return carAlreadyBooked;
        }
    }

    private final StubBookingPortOut bookingPortOut = new StubBookingPortOut();
    private final StubCarProfilePortOut carProfilePortOut = new StubCarProfilePortOut();
    private final StubBranchPortOut branchPortOut = new StubBranchPortOut();
    private final StubServicePortOut servicePortOut = new StubServicePortOut();
    private final StubAvailabilityPortOut availabilityPortOut = new StubAvailabilityPortOut();
    private final BookingService service = new BookingService(new StubCustomerPortOut(), bookingPortOut,
            carProfilePortOut, branchPortOut, servicePortOut, availabilityPortOut, CLOCK);

    private static CreateBookingCommand command(List<Integer> serviceIds, LocalDate date, LocalTime start) {
        return new CreateBookingCommand(BRANCH_ID.id(), CAR_PROFILE_ID, serviceIds, date, start);
    }

    private void seedBookableTomorrow() {
        seedBookableOn(TOMORROW);
    }

    /** One lift bay and one mechanic, both free 09:00-12:00 on {@code date}. */
    private void seedBookableOn(LocalDate date) {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT), "150.00"));
        availabilityPortOut.bays.add(ServiceBay.of(BAY_ID, "Bay 1", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(date.atTime(9, 0), date.atTime(12, 0)), date, 1, BAY_ID));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(date.atTime(9, 0), date.atTime(12, 0)), date, 2, UserId.of(ANNA)));
    }

    @Test
    public void getMyBookingsResolvesCustomerAndReturnsViews() {
        List<BookingView> result = service.getMyBookings(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().branchName()).isEqualTo("SpeedCare Wola");
        assertThat(bookingPortOut.requestedCustomerId).isEqualTo(CUSTOMER_ID.id());
    }

    @Test
    public void createBookingInsertsSpanSegmentsAndOccupancyThenReturnsTheView() {
        seedBookableTomorrow();

        BookingView result = service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 15)));

        assertThat(result.status()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(carProfilePortOut.requestedCustomerId).isEqualTo(CUSTOMER_ID.id());
        assertThat(bookingPortOut.requestedCustomerId).isEqualTo(CUSTOMER_ID.id());

        Booking inserted = bookingPortOut.inserted;
        assertThat(inserted.getStatus()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(inserted.getDate()).isEqualTo(TOMORROW);
        assertThat(inserted.getStartTime()).isEqualTo(LocalTime.of(9, 15));
        assertThat(inserted.getEndTime()).isEqualTo(LocalTime.of(10, 15));
        assertThat(inserted.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(inserted.getCarProfileId()).isEqualTo(CarProfileId.of(CAR_PROFILE_ID));
        assertThat(inserted.getSegments()).containsExactly(
                BookingSegment.of(1, LocalTime.of(9, 15), LocalTime.of(10, 15), new BigDecimal("150.00")));

        BookingOccupancy occupancy = bookingPortOut.insertedOccupancy;
        assertThat(occupancy.bays()).extracting(ServiceBayBooking::getServiceBayId).containsExactly(BAY_ID);
        assertThat(occupancy.bays().getFirst().getBookedTime())
                .isEqualTo(TimeRange.of(TOMORROW.atTime(9, 15), TOMORROW.atTime(10, 15)));
        assertThat(occupancy.bays().getFirst().getBookingId()).isEqualTo(inserted.getId());
        assertThat(occupancy.employees()).extracting(EmployeeBooking::getEmployeeId).containsExactly(UserId.of(ANNA));
        assertThat(occupancy.equipment()).isEmpty();
    }

    @Test
    public void createBookingRejectsAForeignCarProfileBeforeTouchingSchedules() {
        seedBookableTomorrow();
        carProfilePortOut.owned = false;
        branchPortOut.exists = false;

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(CarProfileNotFoundException.class);
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingRejectsUnknownBranchAndForeignService() {
        seedBookableTomorrow();
        branchPortOut.exists = false;
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(BranchNotFoundException.class);

        branchPortOut.exists = true;
        servicePortOut.toReturn = List.of();
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(ServiceNotFoundException.class);
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingRejectsBadShapeWithFourHundred() {
        seedBookableTomorrow();

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 20))))
                .isInstanceOf(InvalidBookingRequestException.class);
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1, 2, 3, 4), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(InvalidBookingRequestException.class);
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1, 1), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(InvalidBookingRequestException.class);
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(InvalidBookingRequestException.class);
        assertThatExceptionOfType(InvalidBookingRequestException.class)
                .isThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), null, LocalTime.of(9, 0))))
                .extracting(InvalidBookingRequestException::getFieldName)
                .isEqualTo("date");
        assertThatExceptionOfType(InvalidBookingRequestException.class)
                .isThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, null)))
                .extracting(InvalidBookingRequestException::getFieldName)
                .isEqualTo("startTime");
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingConflictsWhenNoAssignmentExistsAtThatStart() {
        seedBookableTomorrow();
        availabilityPortOut.employeeOccupancy.add(EmployeeBooking.of(
                7, TimeRange.of(TOMORROW.atTime(9, 30), TOMORROW.atTime(10, 0)), TOMORROW, UserId.of(ANNA), BookingId.genId()));

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 15))))
                .isInstanceOf(SlotNotAvailableException.class);
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingRejectsAPastStartBeforeLoadingAnything() {
        seedBookableTomorrow();

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TODAY, LocalTime.of(9, 0))))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessageContaining("past");
        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TODAY.minusDays(1), LocalTime.of(9, 0))))
                .isInstanceOf(InvalidBookingRequestException.class);
        assertThat(servicePortOut.loaded).isFalse();
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingAcceptsAStartLaterToday() {
        seedBookableOn(TODAY);

        BookingView result = service.createBooking(EMAIL, command(List.of(1), TODAY, LocalTime.of(11, 0)));

        assertThat(result.status()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(bookingPortOut.inserted).isNotNull();
        assertThat(bookingPortOut.inserted.getDate()).isEqualTo(TODAY);
        assertThat(bookingPortOut.inserted.getStartTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(bookingPortOut.inserted.getEndTime()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    public void createBookingConflictsWhenServicesShareNoBayType() {
        seedBookableTomorrow();
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT), "150.00"), service(2, Set.of(PIT), "80.00"));

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1, 2), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(SlotNotAvailableException.class);
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingRejectsACarThatAlreadyHasAnOverlappingBooking() {
        seedBookableTomorrow();
        bookingPortOut.carAlreadyBooked = true;

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 15))))
                .isInstanceOf(CarProfileAlreadyBookedException.class);
        assertThat(bookingPortOut.inserted).isNull();
        assertThat(bookingPortOut.overlapCarProfileId).isEqualTo(CarProfileId.of(CAR_PROFILE_ID));
        assertThat(bookingPortOut.overlapDate).isEqualTo(TOMORROW);
        assertThat(bookingPortOut.overlapStart).isEqualTo(LocalTime.of(9, 15));
        assertThat(bookingPortOut.overlapEnd).isEqualTo(LocalTime.of(10, 15));
    }

    @Test
    public void createBookingChecksTheCarOnlyAfterAPlanExists() {
        seedBookableTomorrow();
        bookingPortOut.carAlreadyBooked = true;

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(13, 0))))
                .isInstanceOf(SlotNotAvailableException.class);
        assertThat(bookingPortOut.overlapCarProfileId).isNull();
    }

    @Test
    public void cancelBookingPersistsCancelledStateAndReturnsFreshView() {
        bookingPortOut.stored = booking(BookingStatus.SCHEDULED);

        BookingView result = service.cancelBooking(EMAIL, BOOKING_ID);

        assertThat(bookingPortOut.updated.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(bookingPortOut.updated.getId().id()).isEqualTo(BOOKING_ID);
        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(bookingPortOut.freedOccupancyFor).isEqualTo(BookingId.of(BOOKING_ID));
    }

    @Test
    public void cancelBookingUnknownOrForeignIdThrowsNotFound() {
        bookingPortOut.stored = null;

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class);
        assertThat(bookingPortOut.updated).isNull();
        assertThat(bookingPortOut.freedOccupancyFor).isNull();
    }

    @Test
    public void cancelBookingNonScheduledPropagatesDomainRefusalWithoutPersisting() {
        bookingPortOut.stored = booking(BookingStatus.COMPLETED);

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingCancellationNotAllowedException.class);
        assertThat(bookingPortOut.updated).isNull();
        assertThat(bookingPortOut.freedOccupancyFor).isNull();
    }

    @Test
    public void createBookingLoadsExactlyTheBookingDayForTheChainResources() {
        seedBookableTomorrow();

        service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 15)));

        assertThat(branchPortOut.lastZoneBranchId).isEqualTo(BRANCH_ID);
        assertThat(servicePortOut.lastLoadByIds).containsExactly(1);
        assertThat(availabilityPortOut.lastBaysBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastEmployeesBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastEquipmentBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastBayAvailIds).containsExactly(BAY_ID);
        assertThat(availabilityPortOut.lastBayAvailFrom).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastBayAvailTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastBayOccIds).containsExactly(BAY_ID);
        assertThat(availabilityPortOut.lastEmployeeAvailIds).containsExactly(ANNA);
        assertThat(availabilityPortOut.lastEmployeeAvailFrom).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastEmployeeAvailTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastEmployeeOccIds).containsExactly(ANNA);
        assertThat(availabilityPortOut.lastEquipmentAvailIds).isEmpty();
    }

    @Test
    public void createBookingWithTwoServicesWritesTwoSegmentsAndOccupancyPerSegment() {
        seedBookableTomorrow();
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT), "150.00"), service(2, Set.of(LIFT), "80.00"));

        service.createBooking(EMAIL, command(List.of(1, 2), TOMORROW, LocalTime.of(9, 0)));

        Booking booked = bookingPortOut.inserted;
        assertThat(booked.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(booked.getEndTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(booked.getSegments()).extracting(BookingSegment::serviceId, BookingSegment::startTime, BookingSegment::endTime)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        org.assertj.core.groups.Tuple.tuple(2, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        BookingOccupancy occupancy = bookingPortOut.insertedOccupancy;
        assertThat(occupancy.bays()).hasSize(1);
        assertThat(occupancy.bays().getFirst().getBookedTime())
                .isEqualTo(TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(11, 0)));
        assertThat(occupancy.employees()).extracting(e -> e.getBookedTime().lower())
                .containsExactly(TOMORROW.atTime(9, 0), TOMORROW.atTime(10, 0));
        assertThat(occupancy.employees()).allSatisfy(e -> assertThat(e.getEmployeeId()).isEqualTo(UserId.of(ANNA)));
        assertThat(occupancy.equipment()).isEmpty();
    }

    @Test
    public void createBookingRejectsAStartOutsideOpeningHours() {
        seedBookableTomorrow();
        availabilityPortOut.openingHours = new ArrayList<>(allWeek(LocalTime.of(10, 0), LocalTime.of(12, 0)));

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(9, 0))))
                .isInstanceOf(SlotNotAvailableException.class);
        assertThat(bookingPortOut.inserted).isNull();
    }

    @Test
    public void createBookingInsideOpeningHoursSucceeds() {
        seedBookableTomorrow();
        availabilityPortOut.openingHours = new ArrayList<>(allWeek(LocalTime.of(10, 0), LocalTime.of(12, 0)));

        BookingView result = service.createBooking(EMAIL, command(List.of(1), TOMORROW, LocalTime.of(10, 0)));

        assertThat(result.status()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(bookingPortOut.inserted.getDate()).isEqualTo(TOMORROW);
        assertThat(bookingPortOut.inserted.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(bookingPortOut.inserted.getEndTime()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    public void createBookingRejectsAClosedDay() {
        seedBookableOn(SATURDAY);
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));

        assertThatThrownBy(() -> service.createBooking(EMAIL, command(List.of(1), SATURDAY, LocalTime.of(9, 0))))
                .isInstanceOf(SlotNotAvailableException.class);
        assertThat(bookingPortOut.inserted).isNull();
        assertThat(availabilityPortOut.lastOpeningHoursBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastExceptionsFrom).isEqualTo(SATURDAY);
        assertThat(availabilityPortOut.lastExceptionsTo).isEqualTo(SATURDAY);
        assertThat(availabilityPortOut.lastBaysBranchId).isNull();
        assertThat(availabilityPortOut.lastBayAvailIds).isNull();
    }
}
