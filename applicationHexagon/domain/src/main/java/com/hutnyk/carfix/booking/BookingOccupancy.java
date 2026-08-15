package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.util.Validator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * The resource rows a booking reserves: the bay for the whole visit (segments and gaps), and one
 * employee row and one equipment row per filled requirement slot per segment. Deleted by
 * booking id on cancellation. Employee and equipment rows are ordered by resource id, then start,
 * so two bookings that share resources insert them in the same order and wait on the exclusion
 * constraint in the same order — no lock cycle, no deadlock.
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
                        .map(employeeId -> EmployeeBooking.of(null, segment.time(), date, employeeId, bookingId)))
                .sorted(Comparator.comparing((EmployeeBooking e) -> e.getEmployeeId().id())
                        .thenComparing(e -> e.getBookedTime().lower()))
                .toList();
        List<EquipmentBooking> equipment = plan.segments().stream()
                .flatMap(segment -> segment.equipmentByRequirementId().values().stream()
                        .map(equipmentId -> EquipmentBooking.of(null, segment.time(), date, equipmentId, bookingId)))
                .sorted(Comparator.comparing(EquipmentBooking::getEquipmentId)
                        .thenComparing(e -> e.getBookedTime().lower()))
                .toList();
        return new BookingOccupancy(bays, employees, equipment);
    }
}
