package com.hutnyk.carfix.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void test_of_builds_role() {
        //when
        Role result = Role.of(3, "Engine Mechanic", null);

        //then
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("Engine Mechanic");
        assertThat(result.getBranchId()).isNull();
    }

    @Test
    public void test_of_throws_when_name_is_blank() {
        //when + then
        assertThatThrownBy(() -> Role.of(3, "  ", null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_create_is_branch_scoped_and_requires_branch() {
        //given
        BranchId branchId = BranchId.of(UUID.randomUUID());

        //when
        Role result = Role.create("EV high-voltage", branchId);

        //then
        assertThat(result.getId()).isNull();
        assertThat(result.getBranchId()).isEqualTo(branchId);
        assertThatThrownBy(() -> Role.create("EV high-voltage", null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "branchId");
    }

    @Test
    public void test_resource_status_enums_are_uppercase_and_match_db_checks() {
        //then
        assertThat(EmployeeStatus.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("ACTIVE", "SUSPENDED");
        assertThat(EquipmentStatus.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("ACTIVE", "SUSPENDED");
        assertThat(ServiceBayStatus.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("ACTIVE", "SUSPENDED");
    }
}
