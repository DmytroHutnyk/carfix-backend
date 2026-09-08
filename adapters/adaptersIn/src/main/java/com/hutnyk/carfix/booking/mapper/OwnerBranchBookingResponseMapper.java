package com.hutnyk.carfix.booking.mapper;

import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingCarResponse;
import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingCustomerResponse;
import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingEmployeeResponse;
import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingResponse;
import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingServiceResponse;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingView;

public final class OwnerBranchBookingResponseMapper {

    private OwnerBranchBookingResponseMapper() {
    }

    public static OwnerBranchBookingResponse toResponse(OwnerBranchBookingView view) {
        if (view == null) {
            return null;
        }
        return new OwnerBranchBookingResponse(
                view.reference(),
                view.status(),
                view.start(),
                view.end(),
                new OwnerBranchBookingCustomerResponse(
                        view.customer().name(), view.customer().phone(), view.customer().email()),
                new OwnerBranchBookingCarResponse(
                        view.car().brand(), view.car().model(), view.car().plate()),
                view.services().stream()
                        .map(service -> new OwnerBranchBookingServiceResponse(
                                service.name(), service.durationMinutes(), service.price()))
                        .toList(),
                view.bay(),
                view.employees().stream()
                        .map(employee -> new OwnerBranchBookingEmployeeResponse(employee.name(), employee.role()))
                        .toList(),
                view.equipment(),
                view.totalDurationMinutes(),
                view.createdAt());
    }
}
