package com.hutnyk.carfix.service;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Service {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    //Nullable
    private final String description;
    private final Short durationMinutes;
    private final BigDecimal price;
    private final ServiceStatus status;
    private final Integer serviceBayId;
    private final Integer serviceCategoryId;

    @Builder
    private Service(
            Integer id,
            String name,
            String description,
            Short durationMinutes,
            BigDecimal price,
            ServiceStatus status,
            Integer serviceBayId,
            Integer serviceCategoryId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.description = description;
        this.durationMinutes = validateDurationMinutes(durationMinutes);
        this.price = validatePrice(price);
        this.status = Validator.notNull(status, "status");
        this.serviceBayId = Validator.notNull(serviceBayId, "serviceBayId");
        this.serviceCategoryId = Validator.notNull(serviceCategoryId, "serviceCategoryId");
    }

    public static Service of(
            Integer id,
            String name,
            String description,
            Short durationMinutes,
            BigDecimal price,
            ServiceStatus status,
            Integer serviceBayId,
            Integer serviceCategoryId) {
        return Service.builder()
                .id(id)
                .name(name)
                .description(description)
                .durationMinutes(durationMinutes)
                .price(price)
                .status(status)
                .serviceBayId(serviceBayId)
                .serviceCategoryId(serviceCategoryId)
                .build();
    }

    private static Short validateDurationMinutes(Short durationMinutes) {
        Validator.notNull(durationMinutes, "durationMinutes");

        if (durationMinutes <= 0) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.VALUE_OUT_OF_RANGE, "durationMinutes", durationMinutes);
        }

        return durationMinutes;
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        Validator.notNull(price, "price");

        if (price.signum() < 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "price", price);
        }

        return price;
    }
}
