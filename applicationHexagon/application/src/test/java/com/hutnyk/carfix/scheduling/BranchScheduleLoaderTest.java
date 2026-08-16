package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.exception.ServiceNotFoundException;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class BranchScheduleLoaderTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final BranchId OTHER_BRANCH = BranchId.genId();
    private static final LocalDate DAY = LocalDate.of(2030, 6, 12);
    private static final int LIFT = 1;
    private static final int PIT = 2;
    private static final int MECHANIC = 10;
    private static final int SENIOR = 11;
    private static final int JACK_TYPE = 20;
    private static final UUID ANNA = UUID.randomUUID();
    private static final UUID JAN = UUID.randomUUID();

    private static Service service(int id, Set<Integer> bayTypes, ServiceStatus status, BranchId branch,
                                   List<EquipmentRequirement> equipmentReqs) {
        return Service.of(id, "Service " + id, null, (short) 60, BigDecimal.TEN, status, branch, 1, bayTypes,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), equipmentReqs);
    }

    private static Service service(int id, Set<Integer> bayTypes) {
        return service(id, bayTypes, ServiceStatus.ACTIVE, BRANCH_ID, List.of());
    }

    private static List<OpeningHours> allWeek(LocalTime open, LocalTime close) {
        return Arrays.stream(DayOfWeek.values())
                .map(day -> OpeningHours.of(null, day, open, close, OpeningHoursMode.OPEN, BRANCH_ID))
                .toList();
    }

    private static List<OpeningHours> monToFri(LocalTime open, LocalTime close) {
        return Arrays.stream(DayOfWeek.values())
                .filter(day -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY)
                .map(day -> OpeningHours.of(null, day, open, close, OpeningHoursMode.OPEN, BRANCH_ID))
                .toList();
    }

    private static class StubServicePortOut implements ServicePortOut {
        List<Service> toReturn = List.of();

        @Override
        public List<Service> loadByIds(Collection<Integer> serviceIds) {
            return toReturn;
        }
    
        @Override
        public Service insert(Service service) {
            throw new UnsupportedOperationException();
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
        List<OpeningHours> openingHours = new ArrayList<>(allWeek(LocalTime.of(6, 0), LocalTime.of(22, 0)));
        List<OpeningHoursException> openingHoursExceptions = new ArrayList<>();
        Collection<Integer> lastBayIds;
        Collection<UUID> lastEmployeeIds;
        Collection<Integer> lastEquipmentIds;
        BranchId lastOpeningHoursBranchId;
        LocalDate lastExceptionsFrom;
        LocalDate lastExceptionsTo;

        @Override public List<ServiceBay> loadActiveBays(BranchId branchId) { return bays; }
        @Override public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) { return employees; }
        @Override public List<Equipment> loadActiveEquipment(BranchId branchId) { return equipment; }
        @Override public Map<BranchId, List<ServiceBay>> loadActiveBaysByBranch(Collection<BranchId> branchIds) { throw new UnsupportedOperationException(); }
        @Override public Map<BranchId, List<EmployeeCandidateView>> loadActiveEmployeesByBranch(Collection<BranchId> branchIds) { throw new UnsupportedOperationException(); }
        @Override public Map<BranchId, List<Equipment>> loadActiveEquipmentByBranch(Collection<BranchId> branchIds) { throw new UnsupportedOperationException(); }
        @Override public List<ServiceBayAvailability> loadBayAvailability(Collection<Integer> ids, LocalDate from, LocalDate to) { lastBayIds = ids; return bayAvailability; }
        @Override public List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> ids, LocalDate from, LocalDate to) { return bayOccupancy; }
        @Override public List<EmployeeAvailability> loadEmployeeAvailability(Collection<UUID> ids, LocalDate from, LocalDate to) { lastEmployeeIds = ids; return employeeAvailability; }
        @Override public List<EmployeeBooking> loadEmployeeOccupancy(Collection<UUID> ids, LocalDate from, LocalDate to) { return employeeOccupancy; }
        @Override public List<EquipmentAvailability> loadEquipmentAvailability(Collection<Integer> ids, LocalDate from, LocalDate to) { lastEquipmentIds = ids; return equipmentAvailability; }
        @Override public List<EquipmentBooking> loadEquipmentOccupancy(Collection<Integer> ids, LocalDate from, LocalDate to) { return equipmentOccupancy; }

        @Override public List<OpeningHours> loadOpeningHours(BranchId branchId) {
            lastOpeningHoursBranchId = branchId;
            return openingHours;
        }

        @Override public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
            lastExceptionsFrom = from;
            lastExceptionsTo = to;
            return openingHoursExceptions;
        }

        @Override public Map<BranchId, List<OpeningHours>> loadOpeningHoursByBranch(Collection<BranchId> branchIds) { throw new UnsupportedOperationException(); }
        @Override public Map<BranchId, List<OpeningHoursException>> loadOpeningHoursExceptionsByBranch(Collection<BranchId> branchIds, LocalDate from, LocalDate to) { throw new UnsupportedOperationException(); }
    }

    private final StubServicePortOut servicePortOut = new StubServicePortOut();
    private final StubAvailabilityPortOut availabilityPortOut = new StubAvailabilityPortOut();
    private final BranchScheduleLoader loader = new BranchScheduleLoader(servicePortOut, availabilityPortOut);

    @Test
    void test_loadServices_rejects_missing_inactive_and_foreign() {
        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)));
        assertThatThrownBy(() -> loader.loadServices(List.of(1, 2), BRANCH_ID))
                .isInstanceOf(ServiceNotFoundException.class);

        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT), ServiceStatus.SUSPENDED, BRANCH_ID, List.of()));
        assertThatThrownBy(() -> loader.loadServices(List.of(1), BRANCH_ID))
                .isInstanceOf(ServiceNotFoundException.class);

        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT), ServiceStatus.ACTIVE, OTHER_BRANCH, List.of()));
        assertThatThrownBy(() -> loader.loadServices(List.of(1), BRANCH_ID))
                .isInstanceOf(ServiceNotFoundException.class);

        servicePortOut.toReturn = List.of(service(1, Set.of(LIFT)));
        assertThat(loader.loadServices(List.of(1), BRANCH_ID)).hasSize(1);
    }

    @Test
    void test_commonBayTypes_is_the_intersection() {
        assertThat(BranchScheduleLoader.commonBayTypes(List.of(service(1, Set.of(LIFT, PIT)), service(2, Set.of(PIT)))))
                .containsExactly(PIT);
        assertThat(BranchScheduleLoader.commonBayTypes(List.of(service(1, Set.of(LIFT)), service(2, Set.of(PIT)))))
                .isEmpty();
    }

    @Test
    void test_loadResources_filters_by_bay_type_role_and_equipment_type() {
        Service withJack = service(1, Set.of(LIFT), ServiceStatus.ACTIVE, BRANCH_ID,
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE))));
        availabilityPortOut.bays.add(ServiceBay.of(100, "Lift bay", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.bays.add(ServiceBay.of(101, "Pit bay", ServiceBayStatus.ACTIVE, null, PIT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        availabilityPortOut.employees.add(new EmployeeCandidateView(JAN, Set.of(SENIOR)));
        availabilityPortOut.equipment.add(Equipment.of(500, "Jack", null, EquipmentStatus.ACTIVE, JACK_TYPE, BRANCH_ID));
        availabilityPortOut.equipment.add(Equipment.of(501, "Welder", null, EquipmentStatus.ACTIVE, 99, BRANCH_ID));

        BranchResources resources = loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT));

        assertThat(resources.bays()).extracting(ServiceBay::getId).containsExactly(100);
        assertThat(resources.employees()).extracting(EmployeeCandidateView::employeeId).containsExactly(ANNA);
        assertThat(resources.equipment()).extracting(Equipment::getId).containsExactly(500);
        assertThat(resources.canServe()).isTrue();
    }

    @Test
    void test_canServe_false_without_bay_staff_or_equipment() {
        Service withJack = service(1, Set.of(LIFT), ServiceStatus.ACTIVE, BRANCH_ID,
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE))));

        assertThat(loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT)).canServe()).isFalse();

        availabilityPortOut.bays.add(ServiceBay.of(100, "Lift bay", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        assertThat(loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT)).canServe()).isFalse();

        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        assertThat(loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT)).canServe()).isFalse();

        availabilityPortOut.equipment.add(Equipment.of(500, "Jack", null, EquipmentStatus.ACTIVE, JACK_TYPE, BRANCH_ID));
        assertThat(loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT)).canServe()).isTrue();
    }

    @Test
    void test_loadSchedules_folds_free_time_per_resource_per_date_and_keeps_every_date() {
        Service svc = service(1, Set.of(LIFT));
        availabilityPortOut.bays.add(ServiceBay.of(100, "Lift bay", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        BranchResources resources = loader.loadResources(BRANCH_ID, List.of(svc), Set.of(LIFT));

        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(DAY.atTime(9, 0), DAY.atTime(12, 0)), DAY, 1, 100));
        availabilityPortOut.bayOccupancy.add(ServiceBayBooking.of(
                1, TimeRange.of(DAY.atTime(10, 0), DAY.atTime(11, 0)), DAY, 100, BookingId.genId()));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(DAY.atTime(9, 0), DAY.atTime(12, 0)), DAY, 2, EmployeeId.of(ANNA)));

        Map<LocalDate, DaySchedules> schedules = loader.loadSchedules(resources, DAY, DAY.plusDays(1),
                loader.openRangesByDate(BRANCH_ID, DAY, DAY.plusDays(1)));

        assertThat(schedules).containsOnlyKeys(DAY, DAY.plusDays(1));
        DaySchedules day = schedules.get(DAY);
        assertThat(day.bays()).hasSize(1);
        assertThat(day.bays().getFirst().free()).containsExactly(
                TimeRange.of(DAY.atTime(9, 0), DAY.atTime(10, 0)),
                TimeRange.of(DAY.atTime(11, 0), DAY.atTime(12, 0)));
        assertThat(day.employees()).extracting(EmployeeSchedule::employeeId).containsExactly(EmployeeId.of(ANNA));
        assertThat(day.equipment()).isEmpty();
        DaySchedules next = schedules.get(DAY.plusDays(1));
        assertThat(next.bays()).isEmpty();
        assertThat(next.employees()).isEmpty();
        assertThat(availabilityPortOut.lastBayIds).containsExactly(100);
        assertThat(availabilityPortOut.lastEmployeeIds).containsExactly(ANNA);
        assertThat(availabilityPortOut.lastEquipmentIds).isEmpty();
    }

    @Test
    void test_openRangesByDate_keeps_only_the_open_dates_ascending() {
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));

        Map<LocalDate, List<TimeRange>> open = loader.openRangesByDate(BRANCH_ID, DAY, DAY.plusDays(4));

        assertThat(open).containsOnlyKeys(DAY, DAY.plusDays(1), DAY.plusDays(2));
        assertThat(open.keySet()).containsExactly(DAY, DAY.plusDays(1), DAY.plusDays(2));
        assertThat(open.get(DAY)).containsExactly(TimeRange.of(DAY.atTime(8, 0), DAY.atTime(18, 0)));
        assertThat(availabilityPortOut.lastOpeningHoursBranchId).isEqualTo(BRANCH_ID);
        assertThat(availabilityPortOut.lastExceptionsFrom).isEqualTo(DAY);
        assertThat(availabilityPortOut.lastExceptionsTo).isEqualTo(DAY.plusDays(4));
    }

    @Test
    void test_loadSchedules_builds_no_day_for_a_closed_date() {
        BranchResources resources = seedLiftBayAndMechanic();
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(DAY.atTime(9, 0), DAY.atTime(12, 0)), DAY, 1, 100));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(DAY.atTime(9, 0), DAY.atTime(12, 0)), DAY, 2, EmployeeId.of(ANNA)));
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));

        Map<LocalDate, DaySchedules> schedules = loader.loadSchedules(resources, DAY.plusDays(2), DAY.plusDays(3),
                loader.openRangesByDate(BRANCH_ID, DAY.plusDays(2), DAY.plusDays(3)));

        assertThat(schedules).containsOnlyKeys(DAY.plusDays(2));
    }

    @Test
    void test_loadSchedules_loads_no_availability_when_every_date_is_closed() {
        BranchResources resources = seedLiftBayAndMechanic();
        availabilityPortOut.openingHours = new ArrayList<>(monToFri(LocalTime.of(8, 0), LocalTime.of(18, 0)));

        Map<LocalDate, DaySchedules> schedules = loader.loadSchedules(resources, DAY.plusDays(3), DAY.plusDays(4),
                loader.openRangesByDate(BRANCH_ID, DAY.plusDays(3), DAY.plusDays(4)));

        assertThat(schedules).isEmpty();
        assertThat(availabilityPortOut.lastBayIds).isNull();
        assertThat(availabilityPortOut.lastEmployeeIds).isNull();
        assertThat(availabilityPortOut.lastEquipmentIds).isNull();
    }

    @Test
    void test_loadSchedules_clips_every_resource_kind_to_the_open_ranges() {
        Service withJack = service(1, Set.of(LIFT), ServiceStatus.ACTIVE, BRANCH_ID,
                List.of(EquipmentRequirement.of(1, "Jack", Set.of(JACK_TYPE))));
        availabilityPortOut.bays.add(ServiceBay.of(100, "Lift bay", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        availabilityPortOut.equipment.add(Equipment.of(500, "Jack", null, EquipmentStatus.ACTIVE, JACK_TYPE, BRANCH_ID));
        BranchResources resources = loader.loadResources(BRANCH_ID, List.of(withJack), Set.of(LIFT));
        availabilityPortOut.bayAvailability.add(ServiceBayAvailability.of(
                1, TimeRange.of(DAY.atTime(8, 0), DAY.atTime(18, 0)), DAY, 1, 100));
        availabilityPortOut.employeeAvailability.add(EmployeeAvailability.of(
                1, TimeRange.of(DAY.atTime(8, 0), DAY.atTime(18, 0)), DAY, 2, EmployeeId.of(ANNA)));
        availabilityPortOut.equipmentAvailability.add(EquipmentAvailability.of(
                1, TimeRange.of(DAY.atTime(8, 0), DAY.atTime(18, 0)), DAY, null, 500));
        availabilityPortOut.openingHours = new ArrayList<>(allWeek(LocalTime.of(10, 0), LocalTime.of(12, 0)));

        Map<LocalDate, DaySchedules> schedules = loader.loadSchedules(resources, DAY, DAY,
                loader.openRangesByDate(BRANCH_ID, DAY, DAY));

        TimeRange openWindow = TimeRange.of(DAY.atTime(10, 0), DAY.atTime(12, 0));
        DaySchedules day = schedules.get(DAY);
        assertThat(day.bays().getFirst().free()).containsExactly(openWindow);
        assertThat(day.employees().getFirst().free()).containsExactly(openWindow);
        assertThat(day.equipment().getFirst().free()).containsExactly(openWindow);
    }

    private BranchResources seedLiftBayAndMechanic() {
        availabilityPortOut.bays.add(ServiceBay.of(100, "Lift bay", ServiceBayStatus.ACTIVE, null, LIFT, BRANCH_ID));
        availabilityPortOut.employees.add(new EmployeeCandidateView(ANNA, Set.of(MECHANIC)));
        return loader.loadResources(BRANCH_ID, List.of(service(1, Set.of(LIFT))), Set.of(LIFT));
    }
}
