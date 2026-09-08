package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeAddress;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.employee.entity.EmployeeAvailabilityEntity;
import com.hutnyk.carfix.employee.entity.EmployeeBookingEntity;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.scheduling.mapper.TimeRangeMapper;
import com.hutnyk.carfix.user.UserId;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EmployeeMapper {

    public static Employee toDomain(EmployeeEntity e) {
        if (e == null) return null;
        return Employee.of(
                EmployeeId.of(e.getId()),
                e.getFirstName(),
                e.getLastName(),
                e.getPhone(),
                e.getEmail(),
                e.getUserId() == null ? null : UserId.of(e.getUserId()),
                e.getStatus(),
                e.getNotes(),
                e.getSalary(),
                new EmployeeAddress(e.getStreet(), e.getApartment(), e.getRegion(), e.getCountry(), e.getPostalCode()),
                BranchId.of(e.getBranchEntity().getId()),
                e.getRoles() == null
                        ? Set.of()
                        : e.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toSet()));
    }

    public static EmployeeEntity toEntity(Employee employee, BranchEntity branch, Set<RoleEntity> roles) {
        if (employee == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(employee.getId().id());
        entity.setFirstName(employee.getFirstName());
        entity.setLastName(employee.getLastName());
        entity.setPhone(employee.getPhone());
        entity.setEmail(employee.getEmail());
        entity.setUserId(employee.getUserId() == null ? null : employee.getUserId().id());
        entity.setStatus(employee.getStatus());
        entity.setNotes(employee.getNotes());
        entity.setSalary(employee.getSalary());
        applyAddress(entity, employee.getAddress());
        entity.setBranchEntity(branch);
        entity.setRoles(roles);
        return entity;
    }

    public static EmployeeEntity updateEntity(EmployeeEntity entity, Employee employee, Set<RoleEntity> roles) {
        if (entity == null || employee == null) return entity;
        entity.setFirstName(employee.getFirstName());
        entity.setLastName(employee.getLastName());
        entity.setPhone(employee.getPhone());
        entity.setEmail(employee.getEmail());
        entity.setStatus(employee.getStatus());
        entity.setNotes(employee.getNotes());
        entity.setSalary(employee.getSalary());
        applyAddress(entity, employee.getAddress());
        entity.setRoles(roles);
        return entity;
    }

    public static OwnerEmployeeView toOwnerView(EmployeeEntity e) {
        if (e == null) return null;
        List<String> roleNames = e.getRoles() == null
                ? List.of()
                : e.getRoles().stream().map(RoleEntity::getName).toList();
        return new OwnerEmployeeView(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                e.getPhone(),
                e.getEmail(),
                e.getSalary(),
                e.getStatus(),
                e.getStreet(),
                e.getApartment(),
                e.getRegion(),
                e.getCountry(),
                e.getPostalCode(),
                roleNames);
    }

    private static void applyAddress(EmployeeEntity entity, EmployeeAddress address) {
        entity.setStreet(address == null ? null : address.street());
        entity.setApartment(address == null ? null : address.apartment());
        entity.setRegion(address == null ? null : address.region());
        entity.setCountry(address == null ? null : address.country());
        entity.setPostalCode(address == null ? null : address.postalCode());
    }

    public static EmployeeAvailability toDomain(EmployeeAvailabilityEntity e) {
        if (e == null) return null;
        return EmployeeAvailability.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getAvailableTime()),
                e.getDate(),
                e.getSeriesId(),
                EmployeeId.of(e.getEmployeeEntity().getId()));
    }

    public static EmployeeBooking toDomain(EmployeeBookingEntity e) {
        if (e == null) return null;
        return EmployeeBooking.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getBookedTime()),
                e.getDate(),
                EmployeeId.of(e.getEmployeeEntity().getId()),
                BookingId.of(e.getBookingEntity().getId()));
    }

    public static EmployeeBookingEntity toEntity(EmployeeBooking b, EmployeeEntity employee, BookingEntity booking) {
        if (b == null) return null;
        return new EmployeeBookingEntity(
                b.getId(),
                TimeRangeMapper.toRange(b.getBookedTime()),
                b.getDate(),
                employee,
                booking);
    }

    public static EmployeeCandidateView toCandidateView(EmployeeEntity e) {
        if (e == null) return null;
        return new EmployeeCandidateView(
                EmployeeId.of(e.getId()),
                e.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toSet()));
    }
}
