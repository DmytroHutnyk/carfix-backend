package com.hutnyk.carfix.serviceBay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ServiceBayTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());

    @Test
    public void test_of_builds_bay() {
        ServiceBay result = ServiceBay.of(1, "Lift 1", ServiceBayStatus.ACTIVE, null, 2, BRANCH_ID);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Lift 1");
        assertThat(result.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(result.getNotes()).isNull();
        assertThat(result.getServiceBayTypeId()).isEqualTo(2);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_of_throws_when_bay_type_is_null() {
        assertThatThrownBy(() -> ServiceBay.of(1, "Lift 1", ServiceBayStatus.ACTIVE, null, null, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @Test
    public void test_create_builds_active_bay_without_notes_or_id() {
        ServiceBay result = ServiceBay.create("Bay 1", 4, BRANCH_ID);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Bay 1");
        assertThat(result.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(result.getNotes()).isNull();
        assertThat(result.getServiceBayTypeId()).isEqualTo(4);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_create_requires_type() {
        assertThatThrownBy(() -> ServiceBay.create("Bay 1", null, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "serviceBayTypeId");
    }

    @Test
    public void test_create_with_notes_builds_active_bay_with_notes() {
        ServiceBay result = ServiceBay.create("Bay 2", 5, "handles vans", BRANCH_ID);

        assertThat(result.getId()).isNull();
        assertThat(result.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(result.getNotes()).isEqualTo("handles vans");
        assertThat(result.getServiceBayTypeId()).isEqualTo(5);
    }

    @Test
    public void test_update_replaces_fields_and_preserves_id_status_branch() {
        ServiceBay existing = ServiceBay.of(9, "Old", ServiceBayStatus.SUSPENDED, "old note", 2, BRANCH_ID);

        ServiceBay result = existing.update("New", 7, "new note");

        assertThat(result.getId()).isEqualTo(9);
        assertThat(result.getStatus()).isEqualTo(ServiceBayStatus.SUSPENDED);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getServiceBayTypeId()).isEqualTo(7);
        assertThat(result.getNotes()).isEqualTo("new note");
    }

    @Test
    public void test_update_requires_name() {
        ServiceBay existing = ServiceBay.of(9, "Old", ServiceBayStatus.ACTIVE, null, 2, BRANCH_ID);

        assertThatThrownBy(() -> existing.update("  ", 7, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("name");
    }
}
