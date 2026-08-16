package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
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
        return ServiceBayType.of(e.getId(), e.getName());
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
