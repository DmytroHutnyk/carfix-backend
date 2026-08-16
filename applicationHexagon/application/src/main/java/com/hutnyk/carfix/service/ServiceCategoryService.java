package com.hutnyk.carfix.service;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.service.ServiceCategoryPortIn;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ApplicationService
@RequiredArgsConstructor
public class ServiceCategoryService implements ServiceCategoryPortIn {

    private final ServiceCategoryPortOut serviceCategoryPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategory> getAllCategories() {
        return serviceCategoryPortOut.findAll();
    }
}
