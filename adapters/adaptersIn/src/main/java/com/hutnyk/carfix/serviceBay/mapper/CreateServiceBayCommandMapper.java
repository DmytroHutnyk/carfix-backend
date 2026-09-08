package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.in.serviceBay.commands.CreateServiceBayCommand;
import com.hutnyk.carfix.serviceBay.dto.request.CreateServiceBayRequest;

public class CreateServiceBayCommandMapper {
    public static CreateServiceBayCommand toCommand(CreateServiceBayRequest request) {
        if (request == null) return null;
        return new CreateServiceBayCommand(request.name(), request.serviceBayTypeId(), request.notes());
    }
}
