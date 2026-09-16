package com.hutnyk.carfix.in.service;

import com.hutnyk.carfix.in.service.commands.CreateServiceCommand;
import com.hutnyk.carfix.in.service.commands.UpdateServiceCommand;
import com.hutnyk.carfix.in.service.query.OwnerServiceView;

import java.util.List;
import java.util.UUID;

public interface OwnerServicePortIn {

    List<OwnerServiceView> getServices(String ownerEmail, UUID branchId);

    OwnerServiceView createService(String ownerEmail, UUID branchId, CreateServiceCommand command);

    OwnerServiceView updateService(String ownerEmail, UUID branchId, Integer serviceId, UpdateServiceCommand command);

    OwnerServiceView activateService(String ownerEmail, UUID branchId, Integer serviceId);

    OwnerServiceView suspendService(String ownerEmail, UUID branchId, Integer serviceId);

    void deleteService(String ownerEmail, UUID branchId, Integer serviceId);
}
