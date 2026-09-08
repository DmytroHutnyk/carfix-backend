package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.serviceBay.dto.response.ServiceBayResponse;
import com.hutnyk.carfix.serviceBay.dto.response.ServiceBayTypeResponse;

public class ServiceBayResponseMapper {

    public static ServiceBayResponse toResponse(OwnerServiceBayView view) {
        if (view == null) return null;
        return new ServiceBayResponse(
                view.id(),
                view.name(),
                view.typeId(),
                view.typeName(),
                view.notes(),
                view.status());
    }

    public static ServiceBayTypeResponse toTypeResponse(ServiceBayTypeView view) {
        if (view == null) return null;
        return new ServiceBayTypeResponse(view.id(), view.name());
    }
}
