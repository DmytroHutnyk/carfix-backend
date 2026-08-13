package com.hutnyk.carfix.service;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
    private final BranchId branchId;
    private final Integer serviceCategoryId;

    /** Acceptable bay types — alternatives (OR): one free bay of any listed type is enough. */
    private final Set<Integer> serviceBayTypeIds;

    /** All mandatory (AND); one distinct employee per requirement. */
    private final List<EmployeeRequirement> employeeRequirements;

    /** All mandatory (AND); one distinct unit per requirement. May be empty. */
    private final List<EquipmentRequirement> equipmentRequirements;

    @Builder
    private Service(
            Integer id,
            String name,
            String description,
            Short durationMinutes,
            BigDecimal price,
            ServiceStatus status,
            BranchId branchId,
            Integer serviceCategoryId,
            Set<Integer> serviceBayTypeIds,
            List<EmployeeRequirement> employeeRequirements,
            List<EquipmentRequirement> equipmentRequirements) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.description = description;
        this.durationMinutes = validateDurationMinutes(durationMinutes);
        this.price = validatePrice(price);
        this.status = Validator.notNull(status, "status");
        this.branchId = Validator.notNull(branchId, "branchId");
        this.serviceCategoryId = Validator.notNull(serviceCategoryId, "serviceCategoryId");
        this.serviceBayTypeIds = validateBayTypeIds(serviceBayTypeIds);
        this.employeeRequirements = validateEmployeeRequirements(employeeRequirements);
        this.equipmentRequirements = List.copyOf(
                Validator.notNull(equipmentRequirements, "equipmentRequirements"));
    }

    public static Service of(
            Integer id,
            String name,
            String description,
            Short durationMinutes,
            BigDecimal price,
            ServiceStatus status,
            BranchId branchId,
            Integer serviceCategoryId,
            Set<Integer> serviceBayTypeIds,
            List<EmployeeRequirement> employeeRequirements,
            List<EquipmentRequirement> equipmentRequirements) {
        return Service.builder()
                .id(id)
                .name(name)
                .description(description)
                .durationMinutes(durationMinutes)
                .price(price)
                .status(status)
                .branchId(branchId)
                .serviceCategoryId(serviceCategoryId)
                .serviceBayTypeIds(serviceBayTypeIds)
                .employeeRequirements(employeeRequirements)
                .equipmentRequirements(equipmentRequirements)
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

    private static Set<Integer> validateBayTypeIds(Set<Integer> serviceBayTypeIds) {
        Validator.notNull(serviceBayTypeIds, "serviceBayTypeIds");

        if (serviceBayTypeIds.isEmpty()) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.VALUE_OUT_OF_RANGE, "serviceBayTypeIds", serviceBayTypeIds);
        }

        return Set.copyOf(serviceBayTypeIds);
    }

    private static List<EmployeeRequirement> validateEmployeeRequirements(
            List<EmployeeRequirement> employeeRequirements) {
        Validator.notNull(employeeRequirements, "employeeRequirements");

        if (employeeRequirements.isEmpty()) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.VALUE_OUT_OF_RANGE, "employeeRequirements", employeeRequirements);
        }

        return List.copyOf(employeeRequirements);
    }
}
