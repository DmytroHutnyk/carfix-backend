package com.hutnyk.carfix.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.AvailableStartView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SearchAvailabilityFilterTest {

    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final String PAGO_PAGO = "Pacific/Pago_Pago";
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final LocalDate DAY_AFTER = TODAY.plusDays(2);
    private static final Clock CLOCK = Clock.fixed(
            TODAY.atTime(10, 7).atZone(WARSAW).toInstant(), ZoneId.systemDefault());
    private static final UUID BRANCH_A = UUID.randomUUID();
    private static final UUID BRANCH_B = UUID.randomUUID();
    private static final UUID MECHANIC_A = UUID.randomUUID();
    private static final UUID MECHANIC_B = UUID.randomUUID();
    private static final int MECHANIC = 10;
    private static final int LIFT = 1;
    private static final int JACK_TYPE = 20;
    private static final int SERVICE_A = 1;
    private static final int SERVICE_B = 2;
    private static final int BAY_A = 100;
    private static final int BAY_B = 200;
    private static final int JACK_A = 500;

    private static Service service(int id, UUID branchId, List<EquipmentRequirement> equipment) {
        return Service.of(id, "Oil and filter change", null, (short) 60, BigDecimal.TEN,
                ServiceStatus.ACTIVE, BranchId.of(branchId), 1, Set.of(LIFT),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), equipment);
    }

    private static WorkshopResultView candidate(UUID branchId, int serviceId) {
        return candidate(branchId, serviceId, WARSAW.getId());
    }

    private static WorkshopResultView candidate(UUID branchId, int serviceId, String tz) {
        return new WorkshopResultView(branchId, "Branch " + branchId, "Street", "1", "Warsaw",
                new BigDecimal("52.2"), new BigDecimal("21.0"), null, null, null,
                List.of(new MatchedServiceView(serviceId, "Oil and filter change", BigDecimal.TEN, (short) 60, "Engine")),
                tz, null);
    }

    private static AvailabilityWindow window(LocalDate from, LocalDate to, LocalTime timeFrom, LocalTime timeTo) {
        return new AvailabilityWindow(from, to, timeFrom, timeTo);
    }

    private static TimeRange at(LocalDate date, int fromHour, int toHour) {
        return TimeRange.of(date.atTime(fromHour, 0), date.atTime(toHour, 0));
    }

    private static final class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();
        Collection<Integer> receivedIds;
        int calls;

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
            receivedIds = serviceIds;
            calls++;
            return toReturn;
        }
    
        @Override
        public Service insert(Service service) {
            throw new UnsupportedOperationException();
        }
}

    private static final class StubAvailabilityPortOut implements AvailabilityPortOut {
        List<ServiceBay> bays = new ArrayList<>();
        List<EmployeeCandidateView> employeesA = new ArrayList<>();
        List<EmployeeCandidateView> employeesB = new ArrayList<>();
        List<Equipment> equipment = new ArrayList<>();
        List<ServiceBayAvailability> bayAvailability = new ArrayList<>();
        List<ServiceBayBooking> bayOccupancy = new ArrayList<>();
        List<EmployeeAvailability> employeeAvailability = new ArrayList<>();
        List<EmployeeBooking> employeeOccupancy = new ArrayList<>();
        List<EquipmentAvailability> equipmentAvailability = new ArrayList<>();
        List<EquipmentBooking> equipmentOccupancy = new ArrayList<>();
        Collection<BranchId> receivedBayBranches;
        Collection<BranchId> receivedEmployeeBranches;
        Collection<BranchId> receivedEquipmentBranches;
        int bayBranchCalls;
        int bayAvailabilityCalls;
        Collection<Integer> receivedBayAvailabilityIds;
        LocalDate receivedFrom;
        LocalDate receivedTo;

        @Override
        public Map<BranchId, List<ServiceBay>> loadActiveBaysByBranch(Collection<BranchId> branchIds) {
            receivedBayBranches = branchIds;
            bayBranchCalls++;
            return bays.stream().collect(Collectors.groupingBy(ServiceBay::getBranchId));
        }

        @Override
        public Map<BranchId, List<EmployeeCandidateView>> loadActiveEmployeesByBranch(Collection<BranchId> branchIds) {
            receivedEmployeeBranches = branchIds;
            return Map.of(BranchId.of(BRANCH_A), employeesA, BranchId.of(BRANCH_B), employeesB);
        }

        @Override
        public Map<BranchId, List<Equipment>> loadActiveEquipmentByBranch(Collection<BranchId> branchIds) {
            receivedEquipmentBranches = branchIds;
            return equipment.stream().collect(Collectors.groupingBy(Equipment::getBranchId));
        }

        @Override
        public List<ServiceBayAvailability> loadBayAvailability(Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            bayAvailabilityCalls++;
            receivedBayAvailabilityIds = bayIds;
            receivedFrom = from;
            receivedTo = to;
            return bayAvailability.stream().filter(a -> bayIds.contains(a.getServiceBayId())).toList();
        }

        @Override
        public List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> bayIds, LocalDate from, LocalDate to) {
            return bayOccupancy;
        }

        @Override
        public List<EmployeeAvailability> loadEmployeeAvailability(Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeAvailability;
        }

        @Override
        public List<EmployeeBooking> loadEmployeeOccupancy(Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
            return employeeOccupancy;
        }

        @Override
        public List<EquipmentAvailability> loadEquipmentAvailability(Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return equipmentAvailability;
        }

        @Override
        public List<EquipmentBooking> loadEquipmentOccupancy(Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
            return equipmentOccupancy;
        }

        @Override
        public List<ServiceBay> loadActiveBays(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Equipment> loadActiveEquipment(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OpeningHours> loadOpeningHours(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
            throw new UnsupportedOperationException();
        }
    }

    private final StubServicePortOut servicePortOut = new StubServicePortOut();
    private final StubAvailabilityPortOut availabilityPortOut = new StubAvailabilityPortOut();
    private final SearchAvailabilityFilter filter =
            new SearchAvailabilityFilter(servicePortOut, availabilityPortOut, CLOCK);

    /* Branch A: one lift bay, one mechanic, service A — bay + mechanic free tomorrow 09:00–12:00 */
    private void seedBranchA() {
        servicePortOut.toReturn = List.of(service(SERVICE_A, BRANCH_A, List.of()));
        availabilityPortOut.bays.add(ServiceBay.of(BAY_A, "Bay A", ServiceBayStatus.ACTIVE, null, LIFT, BranchId.of(BRANCH_A)));
        availabilityPortOut.employeesA.add(new EmployeeCandidateView(MECHANIC_A, Set.of(MECHANIC)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(1, at(TOMORROW, 9, 12), TOMORROW, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(1, at(TOMORROW, 9, 12), TOMORROW, 2, EmployeeId.of(MECHANIC_A)));
    }

    private void seedBranchB() {
        servicePortOut.toReturn = List.of(service(SERVICE_A, BRANCH_A, List.of()), service(SERVICE_B, BRANCH_B, List.of()));
        availabilityPortOut.bays.add(ServiceBay.of(BAY_B, "Bay B", ServiceBayStatus.ACTIVE, null, LIFT, BranchId.of(BRANCH_B)));
        availabilityPortOut.employeesB.add(new EmployeeCandidateView(MECHANIC_B, Set.of(MECHANIC)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(2, at(TOMORROW, 9, 12), TOMORROW, 3, BAY_B));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(2, at(TOMORROW, 9, 12), TOMORROW, 4, EmployeeId.of(MECHANIC_B)));
    }

    private static List<String> startsOf(WorkshopResultView view) {
        return view.nextAvailableStarts().stream().map(s -> s.date() + "T" + s.startTime()).toList();
    }

    @Test
    void test_branch_with_free_slot_kept_with_first_three_starts() {
        //given
        seedBranchA();

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().branchId()).isEqualTo(BRANCH_A);
        assertThat(result.getFirst().tz()).isEqualTo("Europe/Warsaw");
        assertThat(startsOf(result.getFirst()))
                .containsExactly(TOMORROW + "T09:00", TOMORROW + "T09:15", TOMORROW + "T09:30");
    }

    @Test
    void test_fully_booked_branch_dropped() {
        //given
        seedBranchA();
        availabilityPortOut.bayOccupancy.add(ServiceBayBooking.of(
                1, at(TOMORROW, 9, 12), TOMORROW, BAY_A, com.hutnyk.carfix.booking.BookingId.genId()));

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void test_time_window_bounds_the_start_lower_inclusive_upper_exclusive() {
        //given
        seedBranchA();

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)),
                window(TOMORROW, TOMORROW, LocalTime.of(10, 0), LocalTime.of(10, 30)));

        //then
        assertThat(startsOf(result.getFirst())).containsExactly(TOMORROW + "T10:00", TOMORROW + "T10:15");
    }

    @Test
    void test_time_from_alone_and_time_to_alone() {
        //given
        seedBranchA();

        //when
        List<WorkshopResultView> fromOnly = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, LocalTime.of(10, 45), null));
        List<WorkshopResultView> toOnly = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, LocalTime.of(9, 30)));

        //then
        assertThat(startsOf(fromOnly.getFirst())).containsExactly(TOMORROW + "T10:45", TOMORROW + "T11:00");
        assertThat(startsOf(toOnly.getFirst())).containsExactly(TOMORROW + "T09:00", TOMORROW + "T09:15");
    }

    @Test
    void test_starts_span_days_chronologically_and_cap_at_three() {
        //given
        seedBranchA();
        availabilityPortOut.bayAvailability.clear();
        availabilityPortOut.employeeAvailability.clear();
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(1, at(TOMORROW, 11, 12), TOMORROW, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(1, at(TOMORROW, 11, 12), TOMORROW, 2, EmployeeId.of(MECHANIC_A)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(2, at(DAY_AFTER, 9, 12), DAY_AFTER, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(2, at(DAY_AFTER, 9, 12), DAY_AFTER, 2, EmployeeId.of(MECHANIC_A)));

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, DAY_AFTER, null, null));

        //then
        assertThat(startsOf(result.getFirst()))
                .containsExactly(TOMORROW + "T11:00", DAY_AFTER + "T09:00", DAY_AFTER + "T09:15");
    }

    @Test
    void test_days_before_branch_today_skipped_and_today_clamped_to_now() {
        //given
        seedBranchA();
        LocalDate yesterday = TODAY.minusDays(1);
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(3, at(yesterday, 9, 12), yesterday, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(3, at(yesterday, 9, 12), yesterday, 2, EmployeeId.of(MECHANIC_A)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(4, at(TODAY, 9, 12), TODAY, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(4, at(TODAY, 9, 12), TODAY, 2, EmployeeId.of(MECHANIC_A)));

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(yesterday, TODAY, null, null));

        //then — clock is 10:07 → first grid start 10:15
        assertThat(startsOf(result.getFirst()))
                .containsExactly(TODAY + "T10:15", TODAY + "T10:30", TODAY + "T10:45");
    }

    @Test
    void test_today_and_now_come_from_each_branch_zone() {
        //given
        seedBranchA();
        seedBranchB();
        LocalDate yesterday = TODAY.minusDays(1);
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(5, at(yesterday, 9, 23), yesterday, 1, BAY_A));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(5, at(yesterday, 9, 23), yesterday, 2, EmployeeId.of(MECHANIC_A)));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(6, at(yesterday, 9, 23), yesterday, 3, BAY_B));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(6, at(yesterday, 9, 23), yesterday, 4, EmployeeId.of(MECHANIC_B)));

        //when — Pago Pago is UTC-11, so at the fixed instant branch B is still on 2026-08-12 at 21:07
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A), candidate(BRANCH_B, SERVICE_B, PAGO_PAGO)),
                window(yesterday, yesterday, null, null));

        //then — yesterday is already past for Warsaw, still today for Pago Pago
        assertThat(result).extracting(WorkshopResultView::branchId).containsExactly(BRANCH_B);
        assertThat(startsOf(result.getFirst()))
                .containsExactly(yesterday + "T21:15", yesterday + "T21:30", yesterday + "T21:45");
    }

    @Test
    void test_resources_and_calendars_loaded_once_for_all_branches() {
        //given
        seedBranchA();
        seedBranchB();

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A), candidate(BRANCH_B, SERVICE_B)),
                window(TOMORROW, DAY_AFTER, null, null));

        //then
        assertThat(result).extracting(WorkshopResultView::branchId).containsExactly(BRANCH_A, BRANCH_B);
        assertThat(servicePortOut.calls).isEqualTo(1);
        assertThat(servicePortOut.receivedIds).containsExactlyInAnyOrder(SERVICE_A, SERVICE_B);
        assertThat(availabilityPortOut.bayBranchCalls).isEqualTo(1);
        assertThat(availabilityPortOut.receivedBayBranches)
                .containsExactlyInAnyOrder(BranchId.of(BRANCH_A), BranchId.of(BRANCH_B));
        assertThat(availabilityPortOut.receivedEmployeeBranches)
                .containsExactlyInAnyOrder(BranchId.of(BRANCH_A), BranchId.of(BRANCH_B));
        assertThat(availabilityPortOut.receivedEquipmentBranches)
                .containsExactlyInAnyOrder(BranchId.of(BRANCH_A), BranchId.of(BRANCH_B));
        assertThat(availabilityPortOut.bayAvailabilityCalls).isEqualTo(1);
        assertThat(availabilityPortOut.receivedBayAvailabilityIds).containsExactlyInAnyOrder(BAY_A, BAY_B);
        assertThat(availabilityPortOut.receivedFrom).isEqualTo(TOMORROW);
        assertThat(availabilityPortOut.receivedTo).isEqualTo(DAY_AFTER);
    }

    @Test
    void test_branch_without_qualifying_employee_dropped() {
        //given
        seedBranchA();
        availabilityPortOut.employeesA.clear();
        availabilityPortOut.employeesA.add(new EmployeeCandidateView(MECHANIC_A, Set.of(MECHANIC + 99)));

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void test_equipment_requirement_needs_free_unit() {
        //given
        seedBranchA();
        servicePortOut.toReturn = List.of(service(SERVICE_A, BRANCH_A,
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE)))));
        availabilityPortOut.equipment.add(Equipment.of(JACK_A, "Trolley jack", null, EquipmentStatus.ACTIVE, JACK_TYPE, BranchId.of(BRANCH_A)));

        //when — no jack availability rows at all
        List<WorkshopResultView> withoutJackTime = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));
        availabilityPortOut.equipmentAvailability.add(EquipmentAvailability.of(1, at(TOMORROW, 9, 12), TOMORROW, null, JACK_A));
        List<WorkshopResultView> withJackTime = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(withoutJackTime).isEmpty();
        assertThat(withJackTime).hasSize(1);
    }

    @Test
    void test_unknown_or_inactive_matched_service_dropped() {
        //given
        seedBranchA();
        servicePortOut.toReturn = List.of();

        //when
        List<WorkshopResultView> result = filter.filter(
                List.of(candidate(BRANCH_A, SERVICE_A)), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void test_no_candidates_no_port_calls() {
        //when
        List<WorkshopResultView> result = filter.filter(List.of(), window(TOMORROW, TOMORROW, null, null));

        //then
        assertThat(result).isEmpty();
        assertThat(servicePortOut.calls).isZero();
        assertThat(availabilityPortOut.bayBranchCalls).isZero();
    }
}
