package com.hutnyk.carfix.serviceBay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ServiceBayTypeTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());

    @Test
    public void test_of_allows_platform_type_without_branch() {
        //when
        ServiceBayType result = ServiceBayType.of(2, "Two-post lift", null);

        //then
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("Two-post lift");
        assertThat(result.getBranchId()).isNull();
    }

    @Test
    public void test_create_is_branch_scoped_and_unsaved() {
        //when
        ServiceBayType result = ServiceBayType.create("With lift", BRANCH_ID);

        //then
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("With lift");
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_create_requires_branch() {
        //when + then
        assertThatThrownBy(() -> ServiceBayType.create("With lift", null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.NULL_VALUE, "branchId");
    }

    @Test
    public void test_create_rejects_blank_name() {
        //when + then
        assertThatThrownBy(() -> ServiceBayType.create("  ", BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }
}
