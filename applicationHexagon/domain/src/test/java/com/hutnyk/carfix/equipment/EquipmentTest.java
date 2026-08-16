package com.hutnyk.carfix.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EquipmentTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());

    @Test
    public void test_type_of_allows_platform_row_and_create_is_branch_scoped() {
        //when
        EquipmentType platform = EquipmentType.of(1, "OBD scanner", null);
        EquipmentType owned = EquipmentType.create("2-post lift", BRANCH_ID);

        //then
        assertThat(platform.getBranchId()).isNull();
        assertThat(owned.getId()).isNull();
        assertThat(owned.getName()).isEqualTo("2-post lift");
        assertThat(owned.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_type_create_requires_branch() {
        //when + then
        assertThatThrownBy(() -> EquipmentType.create("2-post lift", null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "branchId");
    }

    @Test
    public void test_create_builds_active_unit_without_notes_or_id() {
        //when
        Equipment result = Equipment.create("2-post lift #1", 7, BRANCH_ID);

        //then
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("2-post lift #1");
        assertThat(result.getStatus()).isEqualTo(EquipmentStatus.ACTIVE);
        assertThat(result.getNotes()).isNull();
        assertThat(result.getEquipmentTypeId()).isEqualTo(7);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_create_rejects_blank_name() {
        //when + then
        assertThatThrownBy(() -> Equipment.create(" ", 7, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }
}
