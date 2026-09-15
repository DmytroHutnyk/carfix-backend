package com.hutnyk.carfix.out.service;

import com.hutnyk.carfix.in.service.query.OwnerServiceView;
import com.hutnyk.carfix.service.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicePortOut {

    // Missing ids are omitted; result order need not match input order.
    List<Service> loadByIds(Collection<Integer> serviceIds);

    Service insert(Service service);

    List<OwnerServiceView> findViewsByBranchId(UUID branchId);

    Optional<OwnerServiceView> findViewByIdAndBranchId(Integer serviceId, UUID branchId);

    Optional<Service> findByIdAndBranchId(Integer serviceId, UUID branchId);

    Service update(Service service);

    Service updateStatus(Service service);

    void deleteById(Integer serviceId);

    boolean existsBookingReference(Integer serviceId);
}
