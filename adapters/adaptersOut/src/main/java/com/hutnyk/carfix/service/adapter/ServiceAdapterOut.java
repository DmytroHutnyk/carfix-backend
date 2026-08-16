package com.hutnyk.carfix.service.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.mapper.ServiceMapper;
import com.hutnyk.carfix.service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class ServiceAdapterOut implements ServicePortOut {

    private final ServiceRepository serviceRepository;

    @Override
    public List<Service> loadByIds(Collection<Integer> serviceIds) {
        if (serviceIds.isEmpty()) return List.of();
        return serviceRepository.findAllWithRequirementsByIdIn(serviceIds).stream()
                .map(ServiceMapper::toDomain)
                .toList();
    }
}
