package com.hutnyk.carfix.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ServiceTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());

    private EmployeeRequirement mechanicSlot() {
        return EmployeeRequirement.of(10, "Engine specialist", Set.of(1, 2));
    }

    private Service service(
            Set<Integer> bayTypeIds,
            List<EmployeeRequirement> employeeRequirements,
            List<EquipmentRequirement> equipmentRequirements) {
        return Service.of(
                5,
                "Engine Replacement",
                "Full engine swap",
                (short) 240,
                new BigDecimal("4500.00"),
                ServiceStatus.ACTIVE,
                BRANCH_ID,
                7,
                bayTypeIds,
                employeeRequirements,
                equipmentRequirements);
    }

    @Test
    public void test_of_builds_service_with_requirements() {
        //given
        EquipmentRequirement hoist = EquipmentRequirement.of(20, "Hoist", Set.of(3));

        //when
        Service result = service(Set.of(1, 4), List.of(mechanicSlot()), List.of(hoist));

        //then
        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(result.getServiceCategoryId()).isEqualTo(7);
        assertThat(result.getServiceBayTypeIds()).containsExactlyInAnyOrder(1, 4);
        assertThat(result.getEmployeeRequirements()).hasSize(1);
        assertThat(result.getEmployeeRequirements().getFirst().getRoleIds())
                .containsExactlyInAnyOrder(1, 2);
        assertThat(result.getEquipmentRequirements()).hasSize(1);
    }

    @Test
    public void test_of_allows_empty_equipment_requirements() {
        //when
        Service result = service(Set.of(1), List.of(mechanicSlot()), List.of());

        //then
        assertThat(result.getEquipmentRequirements()).isEmpty();
    }

    @Test
    public void test_of_throws_when_no_bay_type() {
        //when + then
        assertThatThrownBy(() -> service(Set.of(), List.of(mechanicSlot()), List.of()))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }

    @Test
    public void test_of_throws_when_no_employee_requirement() {
        //when + then
        assertThatThrownBy(() -> service(Set.of(1), List.of(), List.of()))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }

    @Test
    public void test_employee_requirement_throws_when_no_role_alternative() {
        //when + then
        assertThatThrownBy(() -> EmployeeRequirement.of(10, "Engine specialist", Set.of()))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }

    @Test
    public void test_equipment_requirement_throws_when_name_is_blank() {
        //when + then
        assertThatThrownBy(() -> EquipmentRequirement.of(20, "  ", Set.of(3)))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }
}
