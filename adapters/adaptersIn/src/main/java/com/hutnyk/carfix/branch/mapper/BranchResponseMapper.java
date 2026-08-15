package com.hutnyk.carfix.branch.mapper;

import com.hutnyk.carfix.branch.dto.response.BranchBrandResponse;
import com.hutnyk.carfix.branch.dto.response.BranchOpeningHoursResponse;
import com.hutnyk.carfix.branch.dto.response.BranchResponse;
import com.hutnyk.carfix.branch.dto.response.BranchReviewResponse;
import com.hutnyk.carfix.branch.dto.response.BranchReviewsPageResponse;
import com.hutnyk.carfix.branch.dto.response.BranchServiceCategoryResponse;
import com.hutnyk.carfix.branch.dto.response.BranchServiceResponse;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchServiceCategoryView;
import com.hutnyk.carfix.in.branch.query.BranchView;

import java.time.format.DateTimeFormatter;

public class BranchResponseMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static BranchResponse toResponse(BranchView view) {
        if (view == null) {
            return null;
        }
        return new BranchResponse(
                view.branchId(), view.name(), view.phoneNumber(), view.email(),
                view.description(), view.cancellationPolicy(),
                view.rating(), view.reviewCount(),
                view.streetName(), view.buildingNumber(), view.city(),
                view.latitude(), view.longitude(), view.googlePlaceId(), view.tz(),
                view.brands().stream()
                        .map(brand -> new BranchBrandResponse(brand.carBrandId(), brand.name()))
                        .toList(),
                view.openingHours().stream()
                        .map(oh -> new BranchOpeningHoursResponse(
                                oh.dayOfWeek().name(),
                                TIME_FORMAT.format(oh.startTime()),
                                TIME_FORMAT.format(oh.closeTime())))
                        .toList(),
                view.serviceCategories().stream()
                        .map(BranchResponseMapper::toResponse)
                        .toList());
    }

    public static BranchReviewsPageResponse toResponse(BranchReviewsPage page) {
        if (page == null) {
            return null;
        }
        return new BranchReviewsPageResponse(
                page.content().stream().map(BranchResponseMapper::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private static BranchServiceCategoryResponse toResponse(BranchServiceCategoryView category) {
        return new BranchServiceCategoryResponse(
                category.categoryId(), category.name(),
                category.services().stream()
                        .map(service -> new BranchServiceResponse(
                                service.serviceId(), service.name(), service.description(),
                                service.durationMinutes(), service.price()))
                        .toList());
    }

    private static BranchReviewResponse toResponse(BranchReviewView review) {
        return new BranchReviewResponse(
                review.reviewId(), review.starsNumber(), review.contents(),
                review.createdAt(), review.customerName(), review.customerSurname());
    }
}
