package com.hutnyk.carfix.service.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.mapper.ServiceMapper;
import com.hutnyk.carfix.service.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class ServiceCategoryAdapterOut implements ServiceCategoryPortOut {

    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    public List<ServiceCategory> findAll() {
        return serviceCategoryRepository.findAllByOrderByNameAsc().stream().map(ServiceMapper::toDomain).toList();
    }
}
