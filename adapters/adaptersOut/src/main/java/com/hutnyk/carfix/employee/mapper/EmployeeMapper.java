package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.entity.EmployeeAvailabilityEntity;
import com.hutnyk.carfix.employee.entity.EmployeeBookingEntity;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.scheduling.mapper.TimeRangeMapper;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.mapper.UserMapper;

import java.util.stream.Collectors;

public class EmployeeMapper {

    public static Employee toDomain(EmployeeEntity e) {
        if (e == null) return null;
        return Employee.of(
                UserMapper.toDomain(e.getUserEntity()),
                e.getStatus(),
                e.getNotes(),
                e.getSalary(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static EmployeeAvailability toDomain(EmployeeAvailabilityEntity e) {
        if (e == null) return null;
        return EmployeeAvailability.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getAvailableTime()),
                e.getDate(),
                e.getSeriesId(),
                UserId.of(e.getEmployeeEntity().getId()));
    }

    public static EmployeeBooking toDomain(EmployeeBookingEntity e) {
        if (e == null) return null;
        return EmployeeBooking.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getBookedTime()),
                e.getDate(),
                UserId.of(e.getEmployeeEntity().getId()),
                BookingId.of(e.getBookingEntity().getId()));
    }

    public static EmployeeCandidateView toCandidateView(EmployeeEntity e) {
        if (e == null) return null;
        return new EmployeeCandidateView(
                e.getId(),
                e.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toSet()));
    }
}
