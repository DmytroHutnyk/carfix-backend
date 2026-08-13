package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.scheduling.SlotPortIn;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;
import com.hutnyk.carfix.in.scheduling.query.DaySlotsView;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.scheduling.query.SlotView;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.exception.InvalidSlotQueryException;
import com.hutnyk.carfix.scheduling.exception.ServiceNotFoundException;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.user.UserId;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@ApplicationService
public class SlotService implements SlotPortIn {

    private static final int MAX_SERVICES = 3;
    private static final int MAX_RANGE_DAYS = 7;

    private final BranchPortOut branchPortOut;
    private final ServicePortOut servicePortOut;
    private final AvailabilityPortOut availabilityPortOut;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public BranchSlotsView getSlots(BranchSlotsQuery query) {
        validate(query);
        BranchId branchId = BranchId.of(query.branchId());
        if (!branchPortOut.existsActiveById(branchId)) {
            throw new BranchNotFoundException(query.branchId());
        }
        List<Service> services = loadServices(query.serviceIds(), branchId);
        List<LocalDate> dates = query.from().datesUntil(query.to().plusDays(1)).toList();

        Set<Integer> commonBayTypes = commonBayTypes(services);
        if (commonBayTypes.isEmpty()) {
            return new BranchSlotsView(false, emptyDays(dates));
        }

        List<ServiceBay> bays = availabilityPortOut.loadActiveBays(branchId).stream()
                .filter(b -> commonBayTypes.contains(b.getServiceBayTypeId()))
                .toList();
        List<EmployeeCandidateView> employees = availabilityPortOut.loadActiveEmployees(branchId);
        List<Equipment> equipment = availabilityPortOut.loadActiveEquipment(branchId);

        if (bays.isEmpty() || !staffable(services, employees) || !equippable(services, equipment)) {
            return new BranchSlotsView(true, emptyDays(dates));
        }

        List<EmployeeCandidateView> relevantEmployees = employees.stream()
                .filter(e -> qualifiesAnyEmployeeSlot(services, e))
                .toList();
        List<Equipment> relevantEquipment = equipment.stream()
                .filter(e -> qualifiesAnyEquipmentSlot(services, e))
                .toList();

        List<Integer> bayIds = bays.stream().map(ServiceBay::getId).toList();
        List<UUID> employeeIds = relevantEmployees.stream().map(EmployeeCandidateView::employeeId).toList();
        List<Integer> equipmentIds = relevantEquipment.stream().map(Equipment::getId).toList();
        LocalDate from = query.from();
        LocalDate to = query.to();

        Map<LocalDate, Map<Integer, List<TimeRange>>> bayAvail = byDateAndResource(
                availabilityPortOut.loadBayAvailability(bayIds, from, to),
                ServiceBayAvailability::getDate, ServiceBayAvailability::getServiceBayId,
                ServiceBayAvailability::getAvailableTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> bayOcc = byDateAndResource(
                availabilityPortOut.loadBayOccupancy(bayIds, from, to),
                ServiceBayBooking::getDate, ServiceBayBooking::getServiceBayId,
                ServiceBayBooking::getBookedTime);
        Map<LocalDate, Map<UserId, List<TimeRange>>> employeeAvail = byDateAndResource(
                availabilityPortOut.loadEmployeeAvailability(employeeIds, from, to),
                EmployeeAvailability::getDate, EmployeeAvailability::getEmployeeId,
                EmployeeAvailability::getAvailableTime);
        Map<LocalDate, Map<UserId, List<TimeRange>>> employeeOcc = byDateAndResource(
                availabilityPortOut.loadEmployeeOccupancy(employeeIds, from, to),
                EmployeeBooking::getDate, EmployeeBooking::getEmployeeId,
                EmployeeBooking::getBookedTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentAvail = byDateAndResource(
                availabilityPortOut.loadEquipmentAvailability(equipmentIds, from, to),
                EquipmentAvailability::getDate, EquipmentAvailability::getEquipmentId,
                EquipmentAvailability::getAvailableTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentOcc = byDateAndResource(
                availabilityPortOut.loadEquipmentOccupancy(equipmentIds, from, to),
                EquipmentBooking::getDate, EquipmentBooking::getEquipmentId,
                EquipmentBooking::getBookedTime);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = LocalDate.now(clock);
        List<DaySlotsView> days = new ArrayList<>();
        for (LocalDate date : dates) {
            List<BaySchedule> baySchedules = bays.stream()
                    .map(b -> new BaySchedule(b.getId(), b.getServiceBayTypeId(),
                            freeOf(bayAvail, bayOcc, date, b.getId())))
                    .filter(s -> !s.free().isEmpty())
                    .toList();
            List<EmployeeSchedule> employeeSchedules = relevantEmployees.stream()
                    .map(e -> new EmployeeSchedule(UserId.of(e.employeeId()), e.roleIds(),
                            freeOf(employeeAvail, employeeOcc, date, UserId.of(e.employeeId()))))
                    .filter(s -> !s.free().isEmpty())
                    .toList();
            List<EquipmentSchedule> equipmentSchedules = relevantEquipment.stream()
                    .map(e -> new EquipmentSchedule(e.getId(), e.getEquipmentTypeId(),
                            freeOf(equipmentAvail, equipmentOcc, date, e.getId())))
                    .filter(s -> !s.free().isEmpty())
                    .toList();

            LocalDateTime notBefore = date.isEqual(today) ? now : date.atStartOfDay();
            List<VisitPlan> plans = SlotCalculator.computeVisits(
                    services, baySchedules, employeeSchedules, equipmentSchedules, notBefore);
            days.add(new DaySlotsView(date, plans.stream()
                    .map(p -> new SlotView(p.start().toLocalTime(), p.end().toLocalTime()))
                    .toList()));
        }
        return new BranchSlotsView(true, List.copyOf(days));
    }

    private void validate(BranchSlotsQuery query) {
        List<Integer> ids = query.serviceIds();
        if (ids == null || ids.isEmpty()) {
            throw new InvalidSlotQueryException("at least one serviceId is required");
        }
        if (ids.size() > MAX_SERVICES) {
            throw new InvalidSlotQueryException("at most " + MAX_SERVICES + " services per visit");
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new InvalidSlotQueryException("serviceIds must be distinct");
        }
        if (query.from() == null || query.to() == null) {
            throw new InvalidSlotQueryException("from and to are required");
        }
        if (query.from().isAfter(query.to())) {
            throw new InvalidSlotQueryException("from must not be after to");
        }
        if (ChronoUnit.DAYS.between(query.from(), query.to()) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidSlotQueryException("date range must be at most " + MAX_RANGE_DAYS + " days");
        }
        if (query.from().isBefore(LocalDate.now(clock))) {
            throw new InvalidSlotQueryException("from must not be in the past");
        }
    }

    private List<Service> loadServices(List<Integer> serviceIds, BranchId branchId) {
        List<Service> services = servicePortOut.loadByIds(serviceIds);
        boolean allValid = services.size() == serviceIds.size() && services.stream()
                .allMatch(s -> s.getStatus() == ServiceStatus.ACTIVE && s.getBranchId().equals(branchId));
        if (!allValid) {
            throw new ServiceNotFoundException(serviceIds);
        }
        return services;
    }

    private static Set<Integer> commonBayTypes(List<Service> services) {
        Set<Integer> common = new HashSet<>(services.getFirst().getServiceBayTypeIds());
        services.forEach(s -> common.retainAll(s.getServiceBayTypeIds()));
        return common;
    }

    private static boolean staffable(List<Service> services, List<EmployeeCandidateView> employees) {
        return services.stream()
                .flatMap(s -> s.getEmployeeRequirements().stream())
                .allMatch(req -> employees.stream()
                        .anyMatch(e -> !Collections.disjoint(e.roleIds(), req.getRoleIds())));
    }

    private static boolean equippable(List<Service> services, List<Equipment> equipment) {
        return services.stream()
                .flatMap(s -> s.getEquipmentRequirements().stream())
                .allMatch(req -> equipment.stream()
                        .anyMatch(e -> req.getEquipmentTypeIds().contains(e.getEquipmentTypeId())));
    }

    private static boolean qualifiesAnyEmployeeSlot(List<Service> services, EmployeeCandidateView e) {
        return services.stream()
                .flatMap(s -> s.getEmployeeRequirements().stream())
                .anyMatch(req -> !Collections.disjoint(e.roleIds(), req.getRoleIds()));
    }

    private static boolean qualifiesAnyEquipmentSlot(List<Service> services, Equipment e) {
        return services.stream()
                .flatMap(s -> s.getEquipmentRequirements().stream())
                .anyMatch(req -> req.getEquipmentTypeIds().contains(e.getEquipmentTypeId()));
    }

    private static List<DaySlotsView> emptyDays(List<LocalDate> dates) {
        return dates.stream().map(d -> new DaySlotsView(d, List.<SlotView>of())).toList();
    }

    private static <T, K> Map<LocalDate, Map<K, List<TimeRange>>> byDateAndResource(
            List<T> rows, Function<T, LocalDate> date, Function<T, K> resource, Function<T, TimeRange> range) {
        return rows.stream().collect(Collectors.groupingBy(date,
                Collectors.groupingBy(resource, Collectors.mapping(range, Collectors.toList()))));
    }

    private static <K> List<TimeRange> freeOf(
            Map<LocalDate, Map<K, List<TimeRange>>> avail,
            Map<LocalDate, Map<K, List<TimeRange>>> occ,
            LocalDate date, K resourceId) {
        return TimeRanges.free(
                avail.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of()),
                occ.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of()));
    }
}
