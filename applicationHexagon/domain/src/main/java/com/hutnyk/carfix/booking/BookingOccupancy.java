package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.util.Validator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * The resource rows a booking reserves: the bay for the whole visit (segments and gaps), and one
 * employee row and one equipment row per filled requirement slot per segment. Deleted by
 * booking id on cancellation.
 */
public record BookingOccupancy(List<ServiceBayBooking> bays,
                               List<EmployeeBooking> employees,
                               List<EquipmentBooking> equipment) {

    public BookingOccupancy {
        bays = List.copyOf(Validator.notNull(bays, "bays"));
        employees = List.copyOf(Validator.notNull(employees, "employees"));
        equipment = List.copyOf(Validator.notNull(equipment, "equipment"));
    }

    public static BookingOccupancy of(BookingId bookingId, VisitPlan plan) {
        Validator.notNull(bookingId, "bookingId");
        Validator.notNull(plan, "plan");
        LocalDate date = plan.start().toLocalDate();
        List<ServiceBayBooking> bays = List.of(ServiceBayBooking.of(
                null, TimeRange.of(plan.start(), plan.end()), date, plan.bayId(), bookingId));
        List<EmployeeBooking> employees = plan.segments().stream()
                .flatMap(segment -> segment.employeeByRequirementId().values().stream()
                        .sorted(Comparator.comparing(UserId::id))
                        .map(employeeId -> EmployeeBooking.of(
                                null, segment.time(), date, employeeId, bookingId)))
                .toList();
        List<EquipmentBooking> equipment = plan.segments().stream()
                .flatMap(segment -> segment.equipmentByRequirementId().values().stream()
                        .sorted()
                        .map(equipmentId -> EquipmentBooking.of(
                                null, segment.time(), date, equipmentId, bookingId)))
                .toList();
        return new BookingOccupancy(bays, employees, equipment);
    }
}
