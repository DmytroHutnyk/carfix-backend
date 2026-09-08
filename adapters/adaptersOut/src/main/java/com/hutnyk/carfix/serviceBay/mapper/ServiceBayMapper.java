package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.scheduling.mapper.TimeRangeMapper;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayAvailabilityEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayBookingEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;

public class ServiceBayMapper {

    public static ServiceBay toDomain(ServiceBayEntity e) {
        if (e == null) return null;
        return ServiceBay.of(
                e.getId(),
                e.getName(),
                e.getStatus(),
                e.getNotes(),
                e.getServiceBayTypeEntity().getId(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static ServiceBayType toTypeDomain(ServiceBayTypeEntity e) {
        if (e == null) return null;
        return ServiceBayType.of(
                e.getId(),
                e.getName(),
                e.getBranchEntity() == null ? null : BranchId.of(e.getBranchEntity().getId()));
    }

    public static ServiceBayTypeEntity toTypeEntity(ServiceBayType type, BranchEntity branch) {
        if (type == null) return null;
        ServiceBayTypeEntity entity = new ServiceBayTypeEntity();
        entity.setId(type.getId());
        entity.setName(type.getName());
        entity.setBranchEntity(branch);
        return entity;
    }

    public static ServiceBayEntity toEntity(ServiceBay bay, ServiceBayTypeEntity type, BranchEntity branch) {
        if (bay == null) return null;
        ServiceBayEntity entity = new ServiceBayEntity();
        entity.setId(bay.getId());
        entity.setName(bay.getName());
        entity.setStatus(bay.getStatus());
        entity.setNotes(bay.getNotes());
        entity.setServiceBayTypeEntity(type);
        entity.setBranchEntity(branch);
        return entity;
    }

    public static void updateEntity(ServiceBayEntity entity, ServiceBay bay, ServiceBayTypeEntity type) {
        if (entity == null || bay == null) return;
        entity.setName(bay.getName());
        entity.setStatus(bay.getStatus());
        entity.setNotes(bay.getNotes());
        entity.setServiceBayTypeEntity(type);
    }

    public static OwnerServiceBayView toOwnerView(ServiceBayEntity e) {
        if (e == null) return null;
        return new OwnerServiceBayView(
                e.getId(),
                e.getName(),
                e.getServiceBayTypeEntity().getId(),
                e.getServiceBayTypeEntity().getName(),
                e.getNotes(),
                e.getStatus());
    }

    public static ServiceBayTypeView toTypeView(ServiceBayTypeEntity e) {
        if (e == null) return null;
        return new ServiceBayTypeView(e.getId(), e.getName());
    }

    public static ServiceBayAvailability toDomain(ServiceBayAvailabilityEntity e) {
        if (e == null) return null;
        return ServiceBayAvailability.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getAvailableTime()),
                e.getDate(),
                e.getSeriesId(),
                e.getServiceBayEntity().getId());
    }

    public static ServiceBayBooking toDomain(ServiceBayBookingEntity e) {
        if (e == null) return null;
        return ServiceBayBooking.of(
                e.getId(),
                TimeRangeMapper.toDomain(e.getBookedTime()),
                e.getDate(),
                e.getServiceBayEntity().getId(),
                BookingId.of(e.getBookingEntity().getId()));
    }

    public static ServiceBayBookingEntity toEntity(ServiceBayBooking b, ServiceBayEntity bay, BookingEntity booking) {
        if (b == null) return null;
        return new ServiceBayBookingEntity(
                b.getId(),
                TimeRangeMapper.toRange(b.getBookedTime()),
                b.getDate(),
                bay,
                booking);
    }
}
