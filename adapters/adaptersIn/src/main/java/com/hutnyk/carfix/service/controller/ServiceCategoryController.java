package com.hutnyk.carfix.service.controller;

import com.hutnyk.carfix.in.service.ServiceCategoryPortIn;
import com.hutnyk.carfix.service.dto.response.ServiceCategoryResponse;
import com.hutnyk.carfix.service.mapper.ServiceCategoryResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryPortIn serviceCategoryPortIn;

    @GetMapping
    public ResponseEntity<List<ServiceCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(serviceCategoryPortIn.getAllCategories().stream()
                .map(ServiceCategoryResponseMapper::toResponse)
                .toList());
    }
}
