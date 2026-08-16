package com.hutnyk.carfix.serviceBay.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
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
        return ServiceBayType.of(
                e.getId(),
                e.getName(),
                e.getBranchEntity() == null ? null : BranchId.of(e.getBranchEntity().getId()));
    }

    public static ServiceBayTypeEntity toTypeEntity(ServiceBayType type, BranchEntity branch) {
        if (type == null) return null;
        ServiceBayTypeEntity entity = new ServiceBayTypeEntity();
        entity.setId(type.getId());
        entity.setName(type.getName());
        entity.setBranchEntity(branch);
        return entity;
    }

    public static ServiceBayEntity toEntity(ServiceBay bay, ServiceBayTypeEntity type, BranchEntity branch) {
        if (bay == null) return null;
        ServiceBayEntity entity = new ServiceBayEntity();
        entity.setId(bay.getId());
        entity.setName(bay.getName());
        entity.setStatus(bay.getStatus());
        entity.setNotes(bay.getNotes());
        entity.setServiceBayTypeEntity(type);
        entity.setBranchEntity(branch);
        return entity;
    }
}
