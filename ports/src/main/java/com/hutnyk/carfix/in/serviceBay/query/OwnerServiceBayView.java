package com.hutnyk.carfix.in.serviceBay.query;

import com.hutnyk.carfix.serviceBay.ServiceBayStatus;

public record OwnerServiceBayView(
        Integer id,
        String name,
        Integer typeId,
        String typeName,
        String notes,
        ServiceBayStatus status
) {
}
