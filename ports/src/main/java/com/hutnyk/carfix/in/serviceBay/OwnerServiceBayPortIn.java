package com.hutnyk.carfix.in.serviceBay;

import com.hutnyk.carfix.in.serviceBay.commands.CreateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.commands.UpdateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;

import java.util.List;
import java.util.UUID;

public interface OwnerServiceBayPortIn {

    List<OwnerServiceBayView> getServiceBays(String ownerEmail, UUID branchId);

    List<ServiceBayTypeView> getServiceBayTypes(String ownerEmail, UUID branchId);

    OwnerServiceBayView createServiceBay(String ownerEmail, UUID branchId, CreateServiceBayCommand command);

    OwnerServiceBayView updateServiceBay(String ownerEmail, UUID branchId, Integer bayId, UpdateServiceBayCommand command);
}
