package com.hutnyk.carfix.serviceBay.dto.response;

import com.hutnyk.carfix.serviceBay.ServiceBayStatus;

public record ServiceBayResponse(
        Integer id,
        String name,
        Integer typeId,
        String type,
        String notes,
        ServiceBayStatus status
) {}
