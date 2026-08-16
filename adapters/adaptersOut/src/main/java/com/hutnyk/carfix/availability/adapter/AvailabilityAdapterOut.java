package com.hutnyk.carfix.availability.adapter;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.mapper.EmployeeMapper;
import com.hutnyk.carfix.employee.repository.EmployeeAvailabilityRepository;
import com.hutnyk.carfix.employee.repository.EmployeeBookingRepository;
import com.hutnyk.carfix.employee.repository.EmployeeRepository;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.equipment.mapper.EquipmentMapper;
import com.hutnyk.carfix.equipment.repository.EquipmentAvailabilityRepository;
import com.hutnyk.carfix.equipment.repository.EquipmentBookingRepository;
import com.hutnyk.carfix.equipment.repository.EquipmentRepository;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.mapper.OpeningHoursMapper;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursExceptionRepository;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursRepository;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.serviceBay.mapper.ServiceBayMapper;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayAvailabilityRepository;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayBookingRepository;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class AvailabilityAdapterOut implements AvailabilityPortOut {

    private final ServiceBayRepository serviceBayRepository;
    private final EmployeeRepository employeeRepository;
    private final EquipmentRepository equipmentRepository;
    private final ServiceBayAvailabilityRepository serviceBayAvailabilityRepository;
    private final ServiceBayBookingRepository serviceBayBookingRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final EmployeeBookingRepository employeeBookingRepository;
    private final EquipmentAvailabilityRepository equipmentAvailabilityRepository;
    private final EquipmentBookingRepository equipmentBookingRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final OpeningHoursExceptionRepository openingHoursExceptionRepository;

    @Override
    public List<ServiceBay> loadActiveBays(BranchId branchId) {
        return serviceBayRepository.findAllByBranchEntityIdAndStatus(branchId.id(), ServiceBayStatus.ACTIVE)
                .stream()
                .map(ServiceBayMapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId) {
        return employeeRepository.findAllWithRolesByBranchIdAndStatus(branchId.id(), EmployeeStatus.ACTIVE)
                .stream()
                .map(EmployeeMapper::toCandidateView)
                .toList();
    }

    @Override
    public List<Equipment> loadActiveEquipment(BranchId branchId) {
        return equipmentRepository.findAllByBranchEntityIdAndStatus(branchId.id(), EquipmentStatus.ACTIVE)
                .stream()
                .map(EquipmentMapper::toDomain)
                .toList();
    }

    @Override
    public Map<BranchId, List<ServiceBay>> loadActiveBaysByBranch(Collection<BranchId> branchIds) {
        if (branchIds.isEmpty()) return Map.of();
        return serviceBayRepository.findAllByBranchEntityIdInAndStatus(ids(branchIds), ServiceBayStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> BranchId.of(e.getBranchEntity().getId()),
                        Collectors.mapping(ServiceBayMapper::toDomain, Collectors.toList())));
    }

    @Override
    public Map<BranchId, List<EmployeeCandidateView>> loadActiveEmployeesByBranch(Collection<BranchId> branchIds) {
        if (branchIds.isEmpty()) return Map.of();
        return employeeRepository.findAllWithRolesByBranchIdInAndStatus(ids(branchIds), EmployeeStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> BranchId.of(e.getBranchEntity().getId()),
                        Collectors.mapping(EmployeeMapper::toCandidateView, Collectors.toList())));
    }

    @Override
    public Map<BranchId, List<Equipment>> loadActiveEquipmentByBranch(Collection<BranchId> branchIds) {
        if (branchIds.isEmpty()) return Map.of();
        return equipmentRepository.findAllByBranchEntityIdInAndStatus(ids(branchIds), EquipmentStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(
                        e -> BranchId.of(e.getBranchEntity().getId()),
                        Collectors.mapping(EquipmentMapper::toDomain, Collectors.toList())));
    }

    @Override
    public List<ServiceBayAvailability> loadBayAvailability(
            Collection<Integer> bayIds, LocalDate from, LocalDate to) {
        if (bayIds.isEmpty()) return List.of();
        return serviceBayAvailabilityRepository.findAllByServiceBayEntityIdInAndDateBetween(bayIds, from, to)
                .stream()
                .map(ServiceBayMapper::toDomain)
                .toList();
    }

    @Override
    public List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> bayIds, LocalDate from, LocalDate to) {
        if (bayIds.isEmpty()) return List.of();
        return serviceBayBookingRepository.findAllByServiceBayEntityIdInAndDateBetween(bayIds, from, to)
                .stream()
                .map(ServiceBayMapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeAvailability> loadEmployeeAvailability(
            Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
        if (employeeIds.isEmpty()) return List.of();
        return employeeAvailabilityRepository.findAllByEmployeeEntityIdInAndDateBetween(employeeIds, from, to)
                .stream()
                .map(EmployeeMapper::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeBooking> loadEmployeeOccupancy(
            Collection<UUID> employeeIds, LocalDate from, LocalDate to) {
        if (employeeIds.isEmpty()) return List.of();
        return employeeBookingRepository.findAllByEmployeeEntityIdInAndDateBetween(employeeIds, from, to)
                .stream()
                .map(EmployeeMapper::toDomain)
                .toList();
    }

    @Override
    public List<EquipmentAvailability> loadEquipmentAvailability(
            Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
        if (equipmentIds.isEmpty()) return List.of();
        return equipmentAvailabilityRepository.findAllByEquipmentEntityIdInAndDateBetween(equipmentIds, from, to)
                .stream()
                .map(EquipmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<EquipmentBooking> loadEquipmentOccupancy(
            Collection<Integer> equipmentIds, LocalDate from, LocalDate to) {
        if (equipmentIds.isEmpty()) return List.of();
        return equipmentBookingRepository.findAllByEquipmentEntityIdInAndDateBetween(equipmentIds, from, to)
                .stream()
                .map(EquipmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<OpeningHours> loadOpeningHours(BranchId branchId) {
        return openingHoursRepository.findAllByBranchEntityId(branchId.id())
                .stream()
                .map(OpeningHoursMapper::toDomain)
                .toList();
    }

    @Override
    public List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to) {
        return openingHoursExceptionRepository.findAllByBranchEntityIdAndDateBetween(branchId.id(), from, to)
                .stream()
                .map(OpeningHoursMapper::toDomain)
                .toList();
    }

    @Override
    public Map<BranchId, List<OpeningHours>> loadOpeningHoursByBranch(Collection<BranchId> branchIds) {
        if (branchIds.isEmpty()) return Map.of();
        return openingHoursRepository.findAllByBranchEntityIdIn(ids(branchIds)).stream()
                .map(OpeningHoursMapper::toDomain)
                .collect(Collectors.groupingBy(OpeningHours::getBranchId));
    }

    @Override
    public Map<BranchId, List<OpeningHoursException>> loadOpeningHoursExceptionsByBranch(
            Collection<BranchId> branchIds, LocalDate from, LocalDate to) {
        if (branchIds.isEmpty()) return Map.of();
        List<LocalDate> dates = from.datesUntil(to.plusDays(1)).toList();
        return openingHoursExceptionRepository.findAllByBranchEntityIdInAndDateIn(ids(branchIds), dates).stream()
                .map(OpeningHoursMapper::toDomain)
                .collect(Collectors.groupingBy(OpeningHoursException::getBranchId));
    }

    private static List<UUID> ids(Collection<BranchId> branchIds) {
        return branchIds.stream().map(BranchId::id).toList();
    }
}
