package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.in.serviceBay.commands.UpdateServiceBayCommand;
import com.hutnyk.carfix.serviceBay.dto.request.UpdateServiceBayRequest;

public class UpdateServiceBayCommandMapper {
    public static UpdateServiceBayCommand toCommand(UpdateServiceBayRequest request) {
        if (request == null) return null;
        return new UpdateServiceBayCommand(request.name(), request.serviceBayTypeId(), request.notes());
    }
}
