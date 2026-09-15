package com.hutnyk.carfix.serviceBay.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import com.hutnyk.carfix.serviceBay.mapper.ServiceBayMapper;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayRepository;
import com.hutnyk.carfix.serviceBay.repository.ServiceBayTypeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public ServiceBay update(ServiceBay bay) {
        ServiceBayEntity entity = serviceBayRepository.findById(bay.getId())
                .orElseThrow(IllegalStateException::new);
        ServiceBayTypeEntity type = entityManager.getReference(ServiceBayTypeEntity.class, bay.getServiceBayTypeId());
        ServiceBayMapper.updateEntity(entity, bay, type);
        return ServiceBayMapper.toDomain(serviceBayRepository.save(entity));
    }

    @Override
    public List<OwnerServiceBayView> findViewsByBranchId(UUID branchId) {
        return serviceBayRepository.findAllByBranchEntityId(branchId).stream()
                .map(ServiceBayMapper::toOwnerView)
                .toList();
    }

    @Override
    public Optional<OwnerServiceBayView> findViewByIdAndBranchId(Integer bayId, UUID branchId) {
        return serviceBayRepository.findByIdAndBranchEntityId(bayId, branchId).map(ServiceBayMapper::toOwnerView);
    }

    @Override
    public Optional<ServiceBay> findByIdAndBranchId(Integer bayId, UUID branchId) {
        return serviceBayRepository.findByIdAndBranchEntityId(bayId, branchId).map(ServiceBayMapper::toDomain);
    }

    @Override
    public List<ServiceBayTypeView> findTypesForBranch(UUID branchId) {
        return serviceBayTypeRepository.findAllForBranch(branchId).stream()
                .map(ServiceBayMapper::toTypeView)
                .toList();
    }

    @Override
    public Optional<ServiceBayType> findTypeByNameForBranch(String name, UUID branchId) {
        return serviceBayTypeRepository.findByNameForBranch(name, branchId).stream()
                .findFirst()
                .map(ServiceBayMapper::toTypeDomain);
    }
}
