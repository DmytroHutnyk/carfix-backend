package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;

public class ServiceBayMapper {

    public static ServiceBay toDomain(ServiceBayEntity e) {
        if (e == null) return null;
        return ServiceBay.of(
                e.getId(),
                e.getName(),
                e.getStatus(),
                e.getNotes(),
                e.getServiceBayTypeEntity().getId(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static ServiceBayType toTypeDomain(ServiceBayTypeEntity e) {
        if (e == null) return null;
        return ServiceBayType.of(e.getId(), e.getName());
    }
}
