package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.openingHours.OpeningCalendar;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.exception.ServiceNotFoundException;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads everything the slot math needs for one branch and one service chain, and folds
 * availability minus occupancy into per-date free schedules clipped to the branch's opening
 * hours. Shared by {@code SlotService} (week view) and {@code BookingService} (single-start
 * recompute); plain class, no Spring, constructed by each service around its ports.
 */
public final class BranchScheduleLoader {

    private final ServicePortOut servicePortOut;
    private final AvailabilityPortOut availabilityPortOut;

    public BranchScheduleLoader(ServicePortOut servicePortOut, AvailabilityPortOut availabilityPortOut) {
        this.servicePortOut = servicePortOut;
        this.availabilityPortOut = availabilityPortOut;
    }

    public List<Service> loadServices(List<Integer> serviceIds, BranchId branchId) {
        List<Service> services = servicePortOut.loadByIds(serviceIds);
        boolean allValid = services.size() == serviceIds.size() && services.stream()
                .allMatch(s -> s.getStatus() == ServiceStatus.ACTIVE && s.getBranchId().equals(branchId));
        if (!allValid) {
            throw new ServiceNotFoundException(serviceIds);
        }
        return services;
    }

    public static Set<Integer> commonBayTypes(List<Service> services) {
        Set<Integer> common = new HashSet<>(services.getFirst().getServiceBayTypeIds());
        services.forEach(s -> common.retainAll(s.getServiceBayTypeIds()));
        return common;
    }

    public Map<LocalDate, List<TimeRange>> openRangesByDate(BranchId branchId, LocalDate from, LocalDate to) {
        OpeningCalendar calendar = OpeningCalendar.of(
                availabilityPortOut.loadOpeningHours(branchId),
                availabilityPortOut.loadOpeningHoursExceptions(branchId, from, to));
        Map<LocalDate, List<TimeRange>> openByDate = new LinkedHashMap<>();
        for (LocalDate date : from.datesUntil(to.plusDays(1)).toList()) {
            List<TimeRange> open = calendar.openRanges(date);
            if (!open.isEmpty()) {
                openByDate.put(date, open);
            }
        }
        return Collections.unmodifiableMap(openByDate);
    }

    public BranchResources loadResources(BranchId branchId, List<Service> services, Set<Integer> commonBayTypes) {
        List<ServiceBay> bays = availabilityPortOut.loadActiveBays(branchId).stream()
                .filter(b -> commonBayTypes.contains(b.getServiceBayTypeId()))
                .toList();
        List<EmployeeCandidateView> employees = availabilityPortOut.loadActiveEmployees(branchId).stream()
                .filter(e -> qualifiesAnyEmployeeSlot(services, e))
                .toList();
        List<Equipment> equipment = availabilityPortOut.loadActiveEquipment(branchId).stream()
                .filter(e -> qualifiesAnyEquipmentSlot(services, e))
                .toList();
        return new BranchResources(services, bays, employees, equipment);
    }

    public Map<LocalDate, DaySchedules> loadSchedules(BranchResources resources, LocalDate from, LocalDate to,
                                                      Map<LocalDate, List<TimeRange>> openByDate) {
        if (openByDate.isEmpty()) {
            return Map.of();
        }
        List<Integer> bayIds = resources.bays().stream().map(ServiceBay::getId).toList();
        List<UUID> employeeIds = resources.employees().stream().map(EmployeeCandidateView::employeeId).toList();
        List<Integer> equipmentIds = resources.equipment().stream().map(Equipment::getId).toList();

        Map<LocalDate, Map<Integer, List<TimeRange>>> bayAvailability = byDateAndResource(
                availabilityPortOut.loadBayAvailability(bayIds, from, to),
                ServiceBayAvailability::getDate, ServiceBayAvailability::getServiceBayId,
                ServiceBayAvailability::getAvailableTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> bayOccupancy = byDateAndResource(
                availabilityPortOut.loadBayOccupancy(bayIds, from, to),
                ServiceBayBooking::getDate, ServiceBayBooking::getServiceBayId,
                ServiceBayBooking::getBookedTime);
        Map<LocalDate, Map<EmployeeId, List<TimeRange>>> employeeAvailability = byDateAndResource(
                availabilityPortOut.loadEmployeeAvailability(employeeIds, from, to),
                EmployeeAvailability::getDate, EmployeeAvailability::getEmployeeId,
                EmployeeAvailability::getAvailableTime);
        Map<LocalDate, Map<EmployeeId, List<TimeRange>>> employeeOccupancy = byDateAndResource(
                availabilityPortOut.loadEmployeeOccupancy(employeeIds, from, to),
                EmployeeBooking::getDate, EmployeeBooking::getEmployeeId,
                EmployeeBooking::getBookedTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentAvailability = byDateAndResource(
                availabilityPortOut.loadEquipmentAvailability(equipmentIds, from, to),
                EquipmentAvailability::getDate, EquipmentAvailability::getEquipmentId,
                EquipmentAvailability::getAvailableTime);
        Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentOccupancy = byDateAndResource(
                availabilityPortOut.loadEquipmentOccupancy(equipmentIds, from, to),
                EquipmentBooking::getDate, EquipmentBooking::getEquipmentId,
                EquipmentBooking::getBookedTime);

        Map<LocalDate, DaySchedules> days = new LinkedHashMap<>();
        for (LocalDate date : from.datesUntil(to.plusDays(1)).toList()) {
            List<TimeRange> open = openByDate.get(date);
            if (open == null) {
                continue;
            }
            List<BaySchedule> baySchedules = resources.bays().stream()
                    .map(b -> new BaySchedule(b.getId(), b.getServiceBayTypeId(),
                            freeOf(bayAvailability, bayOccupancy, date, b.getId(), open)))
                    .filter(s -> !s.free().isEmpty())
                    .toList();
            List<EmployeeSchedule> employeeSchedules = resources.employees().stream()
                    .map(e -> new EmployeeSchedule(EmployeeId.of(e.employeeId()), e.roleIds(),
                            freeOf(employeeAvailability, employeeOccupancy, date, EmployeeId.of(e.employeeId()), open)))
                    .filter(s -> !s.free().isEmpty())
                    .toList();
            List<EquipmentSchedule> equipmentSchedules = resources.equipment().stream()
                    .map(e -> new EquipmentSchedule(e.getId(), e.getEquipmentTypeId(),
                            freeOf(equipmentAvailability, equipmentOccupancy, date, e.getId(), open)))
                    .filter(s -> !s.free().isEmpty())
                    .toList();
            days.put(date, new DaySchedules(baySchedules, employeeSchedules, equipmentSchedules));
        }
        return Collections.unmodifiableMap(days);
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

    private static <T, K> Map<LocalDate, Map<K, List<TimeRange>>> byDateAndResource(
            List<T> rows, Function<T, LocalDate> date, Function<T, K> resource, Function<T, TimeRange> range) {
        return rows.stream().collect(Collectors.groupingBy(date,
                Collectors.groupingBy(resource, Collectors.mapping(range, Collectors.toList()))));
    }

    private static <K> List<TimeRange> freeOf(
            Map<LocalDate, Map<K, List<TimeRange>>> avail,
            Map<LocalDate, Map<K, List<TimeRange>>> occ,
            LocalDate date, K resourceId, List<TimeRange> open) {
        return TimeRanges.intersect(
                TimeRanges.free(
                        avail.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of()),
                        occ.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of())),
                open);
    }
}
