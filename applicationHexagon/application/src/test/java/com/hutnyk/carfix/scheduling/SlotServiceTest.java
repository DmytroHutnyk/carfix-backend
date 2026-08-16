package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;
import com.hutnyk.carfix.in.scheduling.query.DaySlotsView;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.scheduling.query.SlotView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.exception.InvalidSlotQueryException;
import com.hutnyk.carfix.scheduling.exception.ServiceNotFoundException;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.user.UserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SlotServiceTest {

    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final ZoneId KYIV = ZoneId.of("Europe/Kyiv");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final Clock CLOCK = Clock.fixed(
            TODAY.atTime(10, 7).atZone(WARSAW).toInstant(), ZoneId.systemDefault());
    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final UUID SENIOR_ID = UUID.randomUUID();
    private static final int MECHANIC = 10;
    private static final int SENIOR = 11;
    private static final int LIFT = 1;
    private static final int JACK_TYPE = 20;
    private static final int BAY_ID = 100;
    private static final int JACK_ID = 500;

    private static Service service(int id, Set<Integer> bayTypes) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BRANCH_ID, 1, bayTypes,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
    }

    private static Service seniorService(int id, Set<Integer> bayTypes) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BRANCH_ID, 1, bayTypes,
                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of());
    }

    private static Service serviceNeedingJack(int id) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BRANCH_ID, 1, Set.of(LIFT),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))),
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE))));
    }

    private static List<OpeningHours> allWeek(LocalTime open, LocalTime close) {
        return Arrays.stream(DayOfWeek.values())
                .map(day -> OpeningHours.of(null, day, open, close, BRANCH_ID))
                .toList();
    }

    private static class StubBranchPortOut implements BranchPortOut {
        boolean exists = true;
        ZoneId zone = WARSAW;
        BranchId lastZoneBranchId;

        @Override
        public boolean existsActiveById(BranchId branchId) {
            return exists;
        }

        @Override
        public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
            lastZoneBranchId = branchId;
            return exists ? Optional.of(zone) : Optional.empty();
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

    private static class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();
        Collection<Integer> lastLoadByIds;

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
            lastLoadByIds = serviceIds;
            return toReturn;
        }
    }

    private static class StubAvailabilityPortOut implements AvailabilityPortOut {
        List<ServiceBay> bays = new ArrayList<>();
        List<EmployeeCandidateView> employees = new ArrayList<>();
        List<Equipment> equipment = new ArrayList<>();
        List<ServiceBayAvailability> bayAvailability = new ArrayList<>();
        List<ServiceBayBooking> bayOccupancy = new ArrayList<>();
        List<EmployeeAvailability> employeeAvailability = new ArrayList<>();
        List<EmployeeBooking> employeeOccupancy = new ArrayList<>();
        List<EquipmentAvailability> equipmentAvailability = new ArrayList<>();
        List<EquipmentBooking> equipmentOccupancy = new ArrayList<>();
        boolean resourcesLoaded = false;
        boolean calendarsLoaded = false;
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
        List<OpeningHours> openingHours = new ArrayList<>(allWeek(LocalTime.of(6, 0), LocalTime.of(22, 0)));
        List<OpeningHoursException> openingHoursExceptions = new ArrayList<>();
        boolean openingHoursLoaded = false;
        BranchId lastBaysBranchId;
        BranchId lastEmployeesBranchId;
        BranchId lastEquipmentBranchId;
        BranchId lastOpeningHoursBranchId;
        LocalDate lastExceptionsFrom;
        LocalDate lastExceptionsTo;

        @Override
        public List<ServiceBay> loadActiveBays(BranchId branchId) {
            resourcesLoaded = true;
            lastBaysBranchId = branchId;
            return bays;
        }

        @Override
        public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
            lastEmployeesBranchId = branchId;
            return employees;
        }

        @Override
        public List<Equipment> loadActiveEquipment(BranchId branchId) {
            lastEquipmentBranchId = branchId;
            return equipment;
        }

        @Override
        public List<ServiceBayAvailability> loadBayAvailability(
                Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            calendarsLoaded = true;
            lastBayAvailIds = bayIds;
            lastBayAvailFrom = from;
            lastBayAvailTo = to;
            return bayAvailability;
        }

        @Override
        public List<ServiceBayBooking> loadBayOccupancy(
                Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            lastBayOccIds = bayIds;
            lastBayOccFrom = from;
            lastBayOccTo = to;
            return bayOccupancy;
        }

        @Override
        public List<EmployeeAvailability> loadEmployeeAvailability(
                Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            lastEmployeeAvailIds = employeeIds;
            lastEmployeeAvailFrom = from;
            lastEmployeeAvailTo = to;
            return employeeAvailability;
        }

        @Override
        public List<EmployeeBooking> loadEmployeeOccupancy(
                Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            lastEmployeeOccIds = employeeIds;
            lastEmployeeOccFrom = from;
            lastEmployeeOccTo = to;
            return employeeOccupancy;
        }

        @Override
        public List<EquipmentAvailability> loadEquipmentAvailability(
                Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            lastEquipmentAvailIds = equipmentIds;
            lastEquipmentAvailFrom = from;
            lastEquipmentAvailTo = to;
            return equipmentAvailability;
        }

        @Override
        public List<EquipmentBooking> loadEquipmentOccupancy(
                Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            lastEquipmentOccIds = equipmentIds;
            lastEquipmentOccFrom = from;
            lastEquipmentOccTo = to;
            return equipmentOccupancy;
        }

        @Override
        public List<OpeningHours> loadOpeningHours(BranchId branchId) {
            openingHoursLoaded = true;
            lastOpeningHoursBranchId = branchId;
            return openingHours;
        }

        @Override
        public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
            lastExceptionsFrom = from;
            lastExceptionsTo = to;
            return openingHoursExceptions;
        }
    }

    private final StubBranchPortOut branchPortOut = new StubBranchPortOut();
    private final StubServicePortOut servicePortOut = new StubServicePortOut();
    private final StubAvailabilityPortOut availabilityPortOut = new StubAvailabilityPortOut();
    private final SlotService slotService =
            new SlotService(branchPortOut, servicePortOut, availabilityPortOut, CLOCK);

    private BranchSlotsQuery query(List<Integer> serviceIds, LocalDate from, LocalDate to) {
        return new BranchSlotsQuery(BRANCH_ID.id(), serviceIds, from, to);
    }

    private void seedHappyPath() {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)));
        availabilityPortOut.bays.add(ServiceBay.of(100, "Bay 1", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(EMPLOYEE_ID, Set.of(MECHANIC)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 1, 100));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 2,
                com.hutnyk.carfix.user.UserId.of(EMPLOYEE_ID)));
    }

    private void seedJack(TimeRange availableTomorrow) {
        servicePortOut.toReturn = List.of(serviceNeedingJack(1));
        availabilityPortOut.equipment.add(Equipment.of(
                JACK_ID, "Trolley jack", null, EquipmentStatus.ACTIVE, JACK_TYPE, BRANCH_ID));
        availabilityPortOut.equipmentAvailability.add(EquipmentAvailability.of(
                1, availableTomorrow, TOMORROW, null, JACK_ID));
    }

    @Test
    void test_more_than_three_services_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1, 2, 3, 4), TODAY, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_duplicate_service_ids_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1, 1), TODAY, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_from_after_to_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TOMORROW, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_range_over_seven_days_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, TODAY.plusDays(7))))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_from_in_past_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY.minusDays(1), TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_unknown_branch_404() {
        branchPortOut.exists = false;
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, TODAY)))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    void test_missing_service_404() {
        servicePortOut.toReturn = List.of();
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, TODAY)))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void test_service_of_other_branch_404() {
        servicePortOut.toReturn = List.of(Service.of(1, "Foreign", null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BranchId.genId(), 1, Set.of(LIFT),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()));
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, TODAY)))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void test_suspended_service_404() {
        servicePortOut.toReturn = List.of(Service.of(1, "Suspended", null, (short) 60, BigDecimal.TEN,
                ServiceStatus.SUSPENDED, BRANCH_ID, 1, Set.of(LIFT),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()));
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, TODAY)))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void test_no_common_bay_type_returns_unchainable_without_loading_resources() {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)), service(2, Set.of(LIFT + 1)));
        BranchSlotsView view = slotService.getSlots(query(List.of(1, 2), TODAY, TOMORROW));
        assertThat(view.chainable()).isFalse();
        assertThat(view.days()).hasSize(2);
        assertThat(view.days()).allSatisfy(day -> assertThat(day.slots()).isEmpty());
        assertThat(availabilityPortOut.resourcesLoaded).isFalse();
    }

    @Test
    void test_no_qualified_employee_returns_empty_days_without_calendar_loads() {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)));
        availabilityPortOut.bays.add(ServiceBay.of(100, "Bay 1", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(EMPLOYEE_ID, Set.of(MECHANIC + 99)));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TODAY));
        assertThat(view.chainable()).isTrue();
        assertThat(view.days().getFirst().slots()).isEmpty();
        assertThat(availabilityPortOut.calendarsLoaded).isFalse();
    }

    @Test
    void test_happy_path_maps_plans_to_day_grouped_slot_views() {
        seedHappyPath();
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.chainable()).isTrue();
        assertThat(view.days()).extracting(d -> d.date()).containsExactly(TODAY, TOMORROW);
        assertThat(view.days().getFirst().slots()).isEmpty();
        List<SlotView> slots = view.days().getLast().slots();
        assertThat(slots.getFirst()).isEqualTo(new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        assertThat(slots.getLast()).isEqualTo(new SlotView(LocalTime.of(11, 0), LocalTime.of(12, 0)));
        assertThat(slots).hasSize(9);
        assertThat(availabilityPortOut.lastBayAvailFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastBayAvailTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastBayAvailIds).containsExactly(BAY_ID);
        assertThat(availabilityPortOut.lastBayOccFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastBayOccTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastBayOccIds).containsExactly(BAY_ID);
        assertThat(availabilityPortOut.lastEmployeeAvailFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastEmployeeAvailTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastEmployeeAvailIds).containsExactly(EMPLOYEE_ID);
        assertThat(availabilityPortOut.lastEmployeeOccFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastEmployeeOccTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastEmployeeOccIds).containsExactly(EMPLOYEE_ID);
    }

    @Test
    void test_availability_outside_the_queried_range_contributes_no_day_and_no_slot() {
        seedHappyPath();
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                9, TimeRange.of(TOMORROW.plusDays(10).atTime(9, 0), TOMORROW.plusDays(10).atTime(12, 0)),
                TOMORROW.plusDays(10), 1, BAY_ID));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.days()).extracting(d -> d.date()).containsExactly(TODAY, TOMORROW);
        assertThat(view.days().getLast().slots()).hasSize(9);
    }

    @Test
    void test_two_chained_services_sharing_a_bay_type_yield_combined_slots() {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)), seniorService(2, Set.of(LIFT)));
        availabilityPortOut.bays.add(ServiceBay.of(BAY_ID, "Bay 1", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(EMPLOYEE_ID, Set.of(MECHANIC)));
        availabilityPortOut.employees.add(new EmployeeCandidateView(SENIOR_ID, Set.of(SENIOR)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 1, BAY_ID));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 2,
                UserId.of(EMPLOYEE_ID)));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                2, TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)), TOMORROW, 3,
                UserId.of(SENIOR_ID)));

        BranchSlotsView view = slotService.getSlots(query(List.of(1, 2), TODAY, TOMORROW));

        assertThat(view.chainable()).isTrue();
        List<SlotView> slots = view.days().getLast().slots();
        assertThat(slots).hasSize(5);
        assertThat(slots.getFirst()).isEqualTo(new SlotView(LocalTime.of(9, 0), LocalTime.of(11, 0)));
        assertThat(slots.getLast()).isEqualTo(new SlotView(LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void test_equipment_occupancy_carves_out_overlapping_slots() {
        seedHappyPath();
        seedJack(TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)));
        availabilityPortOut.equipmentOccupancy.add(EquipmentBooking.of(
                1, TimeRange.of(TOMORROW.atTime(10, 0), TOMORROW.atTime(11, 0)),
                TOMORROW, JACK_ID, BookingId.genId()));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.days().getLast().slots()).containsExactly(
                new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new SlotView(LocalTime.of(11, 0), LocalTime.of(12, 0)));
        assertThat(availabilityPortOut.lastEquipmentAvailIds).containsExactly(JACK_ID);
        assertThat(availabilityPortOut.lastEquipmentAvailFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastEquipmentAvailTo).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastEquipmentOccIds).containsExactly(JACK_ID);
        assertThat(availabilityPortOut.lastEquipmentOccFrom).isEqualTo(TODAY);
        assertThat(availabilityPortOut.lastEquipmentOccTo).isEqualTo(TOMORROW);
    }

    @Test
    void test_null_from_or_to_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), null, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), TODAY, null)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_today_slots_start_at_now_rounded_up_to_grid() {
        seedHappyPath();
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                2, TimeRange.of(TODAY.atTime(9, 0), TODAY.atTime(12, 0)), TODAY, 1, 100));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                2, TimeRange.of(TODAY.atTime(9, 0), TODAY.atTime(12, 0)), TODAY, 2,
                com.hutnyk.carfix.user.UserId.of(EMPLOYEE_ID)));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TODAY));
        assertThat(view.days().getFirst().slots().getFirst().startTime())
                .isEqualTo(LocalTime.of(10, 15));
    }

    @Test
    void test_from_is_past_in_the_branch_zone_but_not_in_warsaw() {
        Instant boundary = TODAY.atTime(23, 30).atZone(WARSAW).toInstant();
        assertThat(boundary.atZone(WARSAW).toLocalDate()).isEqualTo(TODAY);
        assertThat(boundary.atZone(KYIV).toLocalDate()).isEqualTo(TOMORROW);
        SlotService service = new SlotService(branchPortOut, servicePortOut, availabilityPortOut,
                Clock.fixed(boundary, ZoneId.systemDefault()));
        seedHappyPath();

        branchPortOut.zone = KYIV;
        assertThatThrownBy(() -> service.getSlots(query(List.of(1), TODAY, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);

        branchPortOut.zone = WARSAW;
        assertThat(service.getSlots(query(List.of(1), TODAY, TODAY)).days())
                .extracting(DaySlotsView::date).containsExactly(TODAY);
    }

    @Test
    void test_today_clamp_uses_the_branch_local_now() {
        assertThat(CLOCK.instant().atZone(WARSAW).toLocalTime()).isEqualTo(LocalTime.of(10, 7));
        assertThat(CLOCK.instant().atZone(KYIV).toLocalTime()).isEqualTo(LocalTime.of(11, 7));
        branchPortOut.zone = KYIV;
        seedHappyPath();
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                2, TimeRange.of(TODAY.atTime(9, 0), TODAY.atTime(13, 0)), TODAY, 1, BAY_ID));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                2, TimeRange.of(TODAY.atTime(9, 0), TODAY.atTime(13, 0)), TODAY, 2, UserId.of(EMPLOYEE_ID)));

        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TODAY));

        assertThat(view.days().getFirst().slots().getFirst().startTime()).isEqualTo(LocalTime.of(11, 15));
    }

    @Test
    void test_seven_day_range_is_accepted() {
        seedHappyPath();
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TODAY.plusDays(6)));
        assertThat(view.days()).hasSize(7);
        assertThat(view.days().getFirst().date()).isEqualTo(TODAY);
        assertThat(view.days().getLast().date()).isEqualTo(TODAY.plusDays(6));
    }

    @Test
    void test_bay_occupancy_carves_out_overlapping_slots() {
        seedHappyPath();
        availabilityPortOut.bayOccupancy.add(ServiceBayBooking.of(
                1, TimeRange.of(TOMORROW.atTime(10, 0), TOMORROW.atTime(11, 0)),
                TOMORROW, BAY_ID, BookingId.genId()));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.days().getLast().slots()).containsExactly(
                new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new SlotView(LocalTime.of(11, 0), LocalTime.of(12, 0)));
    }

    @Test
    void test_employee_occupancy_carves_out_overlapping_slots() {
        seedHappyPath();
        availabilityPortOut.employeeOccupancy.add(EmployeeBooking.of(
                1, TimeRange.of(TOMORROW.atTime(10, 0), TOMORROW.atTime(11, 0)),
                TOMORROW, UserId.of(EMPLOYEE_ID), BookingId.genId()));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.days().getLast().slots()).containsExactly(
                new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new SlotView(LocalTime.of(11, 0), LocalTime.of(12, 0)));
    }

    @Test
    void test_equipment_requirement_met_by_available_unit_yields_slots() {
        seedHappyPath();
        seedJack(TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0)));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        List<SlotView> slots = view.days().getLast().slots();
        assertThat(slots).hasSize(9);
        assertThat(slots.getFirst()).isEqualTo(new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        assertThat(slots.getLast()).isEqualTo(new SlotView(LocalTime.of(11, 0), LocalTime.of(12, 0)));
    }

    @Test
    void test_equipment_availability_narrows_slots_to_its_window() {
        seedHappyPath();
        seedJack(TimeRange.of(TOMORROW.atTime(9, 0), TOMORROW.atTime(10, 0)));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.days().getLast().slots()).containsExactly(
                new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    void test_bay_of_non_matching_type_yields_no_slots() {
        seedHappyPath();
        availabilityPortOut.bays.clear();
        availabilityPortOut.bays.add(ServiceBay.of(100, "Wrong type", ServiceBayStatus.ACTIVE, null, LIFT + 1, BRANCH_ID));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.chainable()).isTrue();
        assertThat(view.days()).allSatisfy(day -> assertThat(day.slots()).isEmpty());
    }

    private static List<OpeningHours> monToFri(LocalTime open, LocalTime close) {
        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
                .stream()
                .map(day -> OpeningHours.of(null, day, open, close, BRANCH_ID))
                .toList();
    }

    private void seedDay(LocalDate date, LocalTime from, LocalTime to) {
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                10, TimeRange.of(date.atTime(from), date.atTime(to)), date, 1, BAY_ID));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                11, TimeRange.of(date.atTime(from), date.atTime(to)), date, 2, UserId.of(EMPLOYEE_ID)));
    }

    private static List<LocalTime> starts(BranchSlotsView view, LocalDate date) {
        return view.days().stream()
                .filter(d -> d.date().equals(date))
                .findFirst().orElseThrow()
                .slots().stream().map(SlotView::startTime).toList();
    }

    @Test
    void test_week_of_closed_days_returns_empty_days_without_loading_anything_else() {
        //given
        seedHappyPath();
        LocalDate saturday = TOMORROW.plusDays(1);
        LocalDate sunday = TOMORROW.plusDays(2);
        seedDay(saturday, LocalTime.of(9, 0), LocalTime.of(12, 0));
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), saturday, sunday));
        //then
        assertThat(view.chainable()).isTrue();
        assertThat(view.days()).extracting(DaySlotsView::date).containsExactly(saturday, sunday);
        assertThat(view.days()).allSatisfy(day -> assertThat(day.slots()).isEmpty());
        assertThat(availabilityPortOut.openingHoursLoaded).isTrue();
        assertThat(availabilityPortOut.resourcesLoaded).isFalse();
        assertThat(availabilityPortOut.calendarsLoaded).isFalse();
    }

    @Test
    void test_closed_days_inside_the_range_are_empty_while_open_days_keep_their_slots() {
        //given
        seedHappyPath();
        LocalDate saturday = TOMORROW.plusDays(1);
        seedDay(saturday, LocalTime.of(9, 0), LocalTime.of(12, 0));
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TOMORROW, saturday));
        //then
        assertThat(starts(view, TOMORROW)).startsWith(LocalTime.of(9, 0)).endsWith(LocalTime.of(11, 0));
        assertThat(starts(view, saturday)).isEmpty();
    }

    @Test
    void test_closed_exception_empties_a_weekly_open_day() {
        //given
        seedHappyPath();
        availabilityPortOut.openingHoursExceptions.add(OpeningHoursException.of(
                null, TOMORROW, null, null, false, "inventory", BRANCH_ID));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TOMORROW, TOMORROW.plusDays(1)));
        //then
        assertThat(starts(view, TOMORROW)).isEmpty();
        assertThat(availabilityPortOut.lastExceptionsFrom).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.lastExceptionsTo).isEqualTo(TOMORROW.plusDays(1));
    }

    @Test
    void test_open_exception_opens_a_weekly_closed_day() {
        //given
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)));
        availabilityPortOut.bays.add(ServiceBay.of(BAY_ID, "Bay 1", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(EMPLOYEE_ID, Set.of(MECHANIC)));
        LocalDate saturday = TOMORROW.plusDays(1);
        seedDay(saturday, LocalTime.of(9, 0), LocalTime.of(12, 0));
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        availabilityPortOut.openingHoursExceptions.add(OpeningHoursException.of(
                null, saturday, LocalTime.of(9, 0), LocalTime.of(12, 0), true, null, BRANCH_ID));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), saturday, saturday));
        //then
        assertThat(starts(view, saturday)).hasSize(9)
                .startsWith(LocalTime.of(9, 0)).endsWith(LocalTime.of(11, 0));
    }

    @Test
    void test_opening_hours_clip_resource_availability() {
        //given
        seedHappyPath();
        availabilityPortOut.openingHours = new ArrayList<>(
                allWeek(LocalTime.of(10, 0), LocalTime.of(11, 30)));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TOMORROW, TOMORROW));
        //then
        assertThat(starts(view, TOMORROW))
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(10, 15), LocalTime.of(10, 30));
    }

    @Test
    void test_visit_cannot_span_a_closed_break_between_two_opening_windows() {
        //given
        seedHappyPath();
        availabilityPortOut.openingHours = new ArrayList<>();
        availabilityPortOut.openingHours.add(OpeningHours.of(null, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), BRANCH_ID));
        availabilityPortOut.openingHours.add(OpeningHours.of(null, DayOfWeek.FRIDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), BRANCH_ID));
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TOMORROW, TOMORROW));
        //then
        assertThat(starts(view, TOMORROW)).containsExactly(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void test_unchainable_pair_is_reported_before_the_calendar_is_consulted() {
        //given
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)), service(2, Set.of(LIFT + 1)));
        availabilityPortOut.openingHours = new ArrayList<>();
        //when
        BranchSlotsView view = slotService.getSlots(query(List.of(1, 2), TOMORROW, TOMORROW));
        //then
        assertThat(view.chainable()).isFalse();
        assertThat(availabilityPortOut.openingHoursLoaded).isFalse();
    }

    @Test
    void test_from_more_than_a_year_ahead_rejected() {
        //given
        seedHappyPath();
        LocalDate limit = TODAY.plusYears(1);
        //when //then
        assertThat(slotService.getSlots(query(List.of(1), limit, limit)).days()).hasSize(1);
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), limit.plusDays(1), limit.plusDays(1))))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_extreme_dates_are_a_400_not_an_overflow() {
        //given
        seedHappyPath();
        //when //then
        assertThatThrownBy(() -> slotService.getSlots(query(List.of(1), LocalDate.MAX, LocalDate.MAX)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_null_service_id_rejected() {
        assertThatThrownBy(() -> slotService.getSlots(query(java.util.Arrays.asList(1, null), TODAY, TODAY)))
                .isInstanceOf(InvalidSlotQueryException.class);
    }

    @Test
    void test_response_echoes_the_branch_zone_on_every_path() {
        //given
        branchPortOut.zone = KYIV;
        seedHappyPath();
        //when //then
        assertThat(slotService.getSlots(query(List.of(1), TOMORROW, TOMORROW)).tz()).isEqualTo("Europe/Kyiv");
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)), service(2, Set.of(LIFT + 1)));
        assertThat(slotService.getSlots(query(List.of(1, 2), TOMORROW, TOMORROW)).tz()).isEqualTo("Europe/Kyiv");
    }

    @Test
    void test_lookups_receive_the_query_ids() {
        //given
        seedHappyPath();
        //when
        slotService.getSlots(query(List.of(1), TOMORROW, TOMORROW));
        //then
        assertThat(branchPortOut.lastZoneBranchId).isEqualTo(BRANCH_ID);
        assertThat(servicePortOut.lastLoadByIds).containsExactly(1);
        assertThat(availabilityPortOut.lastBaysBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastEmployeesBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastEquipmentBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastOpeningHoursBranchId).isEqualTo(BRANCH_ID);
    }
}
