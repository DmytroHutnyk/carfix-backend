package com.hutnyk.carfix.equipment.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.equipment.entity.EquipmentAvailabilityEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentBookingEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.scheduling.mapper.TimeRangeMapper;

public class EquipmentMapper {

    public static Equipment toDomain(EquipmentEntity e) {
        if (e == null) return null;
        return Equipment.of(
                e.getId(),
                e.getName(),
                e.getNotes(),
                e.getStatus(),
                e.getEquipmentTypeEntity().getId(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static EquipmentType toTypeDomain(EquipmentTypeEntity e) {
        if (e == null) return null;
        return EquipmentType.of(e.getId(), e.getName());
    }

    public static EquipmentAvailability toDomain(EquipmentAvailabilityEntity e) {
        if (e == null) return null;
        return EquipmentAvailability.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getAvailableTime()),
                e.getDate(),
                e.getSeriesId(),
                e.getEquipmentEntity().getId());
    }

    public static EquipmentBooking toDomain(EquipmentBookingEntity e) {
        if (e == null) return null;
        return EquipmentBooking.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getBookedTime()),
                e.getDate(),
                e.getEquipmentEntity().getId(),
                BookingId.of(e.getBookingEntity().getId()));
    }

    public static EquipmentBookingEntity toEntity(EquipmentBooking b, EquipmentEntity equipment, BookingEntity booking) {
        if (b == null) return null;
        return new EquipmentBookingEntity(
                b.getId(),
                TimeRangeMapper.toRange(b.getBookedTime()),
                b.getDate(),
                equipment,
                booking);
    }
}
