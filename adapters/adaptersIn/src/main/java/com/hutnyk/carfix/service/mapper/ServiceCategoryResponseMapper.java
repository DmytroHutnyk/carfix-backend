package com.hutnyk.carfix.service.mapper;

import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.dto.response.ServiceCategoryResponse;

public final class ServiceCategoryResponseMapper {

    private ServiceCategoryResponseMapper() {
    }

    public static ServiceCategoryResponse toResponse(ServiceCategory category) {
        if (category == null) {
            return null;
        }
        return new ServiceCategoryResponse(category.getId(), category.getName());
    }
}
