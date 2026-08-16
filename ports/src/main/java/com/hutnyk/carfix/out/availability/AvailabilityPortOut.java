package com.hutnyk.carfix.out.availability;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AvailabilityPortOut {

    List<ServiceBay> loadActiveBays(BranchId branchId);

    List<EmployeeCandidateView> loadActiveEmployees(BranchId branchId);

    List<Equipment> loadActiveEquipment(BranchId branchId);

    Map<BranchId, List<ServiceBay>> loadActiveBaysByBranch(Collection<BranchId> branchIds);

    Map<BranchId, List<EmployeeCandidateView>> loadActiveEmployeesByBranch(Collection<BranchId> branchIds);

    Map<BranchId, List<Equipment>> loadActiveEquipmentByBranch(Collection<BranchId> branchIds);

    List<ServiceBayAvailability> loadBayAvailability(Collection<Integer> bayIds, LocalDate from, LocalDate to);

    List<ServiceBayBooking> loadBayOccupancy(Collection<Integer> bayIds, LocalDate from, LocalDate to);

    List<EmployeeAvailability> loadEmployeeAvailability(Collection<UUID> employeeIds, LocalDate from, LocalDate to);

    List<EmployeeBooking> loadEmployeeOccupancy(Collection<UUID> employeeIds, LocalDate from, LocalDate to);

    List<EquipmentAvailability> loadEquipmentAvailability(Collection<Integer> equipmentIds, LocalDate from, LocalDate to);

    List<EquipmentBooking> loadEquipmentOccupancy(Collection<Integer> equipmentIds, LocalDate from, LocalDate to);

    /** The branch's weekly opening_hours rows (all weekdays it has rows for). */
    List<OpeningHours> loadOpeningHours(BranchId branchId);

    /** [from, to] both inclusive */
    List<OpeningHoursException> loadOpeningHoursExceptions(BranchId branchId, LocalDate from, LocalDate to);
}
