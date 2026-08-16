package com.hutnyk.carfix.serviceBay.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import com.hutnyk.carfix.serviceBay.mapper.ServiceBayMapper;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayRepository;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayTypeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PersistenceAdapter
public class ServiceBayAdapterOut implements ServiceBayPortOut {

    private final ServiceBayTypeRepository serviceBayTypeRepository;
    private final ServiceBayRepository serviceBayRepository;
    private final EntityManager entityManager;

    @Override
    public ServiceBayType insertType(ServiceBayType type) {
        BranchEntity branch = type.getBranchId() == null
                ? null
                : entityManager.getReference(BranchEntity.class, type.getBranchId().id());
        return ServiceBayMapper.toTypeDomain(
                serviceBayTypeRepository.save(ServiceBayMapper.toTypeEntity(type, branch)));
    }

    @Override
    public ServiceBay insert(ServiceBay bay) {
        ServiceBayTypeEntity type = entityManager.getReference(ServiceBayTypeEntity.class, bay.getServiceBayTypeId());
        BranchEntity branch = entityManager.getReference(BranchEntity.class, bay.getBranchId().id());
        return ServiceBayMapper.toDomain(serviceBayRepository.save(ServiceBayMapper.toEntity(bay, type, branch)));
    }
}
