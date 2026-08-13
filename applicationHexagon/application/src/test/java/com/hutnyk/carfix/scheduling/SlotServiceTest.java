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
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.scheduling.query.SlotView;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SlotServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final Clock CLOCK = Clock.fixed(
            TODAY.atTime(10, 7).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final int MECHANIC = 10;
    private static final int LIFT = 1;
    private static final int JACK_TYPE = 20;
    private static final int BAY_ID = 100;
    private static final int JACK_ID = 500;

    private static Service service(int id, Set<Integer> bayTypes) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BRANCH_ID, 1, bayTypes,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
    }

    private static Service serviceNeedingJack(int id) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BRANCH_ID, 1, Set.of(LIFT),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))),
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE))));
    }

    private static class StubBranchPortOut implements BranchPortOut {
        boolean exists = true;

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

    private static class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
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
        boolean resourcesLoaded = false;
        boolean calendarsLoaded = false;

        @Override
        public List<ServiceBay> loadActiveBays(BranchId branchId) {
            resourcesLoaded = true;
            return bays;
        }

        @Override
        public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
            return employees;
        }

        @Override
        public List<Equipment> loadActiveEquipment(BranchId branchId) {
            return equipment;
        }

        @Override
        public List<ServiceBayAvailability> loadBayAvailability(
                Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            calendarsLoaded = true;
            return bayAvailability;
        }

        @Override
        public List<ServiceBayBooking> loadBayOccupancy(
                Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            return bayOccupancy;
        }

        @Override
        public List<EmployeeAvailability> loadEmployeeAvailability(
                Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeAvailability;
        }

        @Override
        public List<EmployeeBooking> loadEmployeeOccupancy(
                Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeOccupancy;
        }

        @Override
        public List<EquipmentAvailability> loadEquipmentAvailability(
                Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return equipmentAvailability;
        }

        @Override
        public List<EquipmentBooking> loadEquipmentOccupancy(
                Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return List.of();
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
    void test_suspended_bay_type_mismatch_filtered_out() {
        seedHappyPath();
        availabilityPortOut.bays.clear();
        availabilityPortOut.bays.add(ServiceBay.of(100, "Wrong type", ServiceBayStatus.ACTIVE, null, LIFT + 1, BRANCH_ID));
        BranchSlotsView view = slotService.getSlots(query(List.of(1), TODAY, TOMORROW));
        assertThat(view.chainable()).isTrue();
        assertThat(view.days()).allSatisfy(day -> assertThat(day.slots()).isEmpty());
    }
}
