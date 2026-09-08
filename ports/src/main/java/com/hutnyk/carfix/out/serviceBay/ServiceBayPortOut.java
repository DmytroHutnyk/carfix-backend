package com.hutnyk.carfix.out.serviceBay;

import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceBayPortOut {
    ServiceBayType insertType(ServiceBayType type);
    ServiceBay insert(ServiceBay bay);
    ServiceBay update(ServiceBay bay);

    List<OwnerServiceBayView> findViewsByBranchId(UUID branchId);
    Optional<OwnerServiceBayView> findViewByIdAndBranchId(Integer bayId, UUID branchId);
    Optional<ServiceBay> findByIdAndBranchId(Integer bayId, UUID branchId);

    List<ServiceBayTypeView> findTypesForBranch(UUID branchId);
    boolean existsTypeForBranch(Integer typeId, UUID branchId);
}
