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
        //when
        ServiceBay result = ServiceBay.of(1, "Lift 1", ServiceBayStatus.ACTIVE, null, 2, BRANCH_ID);

        //then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Lift 1");
        assertThat(result.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(result.getNotes()).isNull();
        assertThat(result.getServiceBayTypeId()).isEqualTo(2);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_of_throws_when_bay_type_is_null() {
        //when + then
        assertThatThrownBy(() -> ServiceBay.of(1, "Lift 1", ServiceBayStatus.ACTIVE, null, null, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
