package com.hutnyk.carfix.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EmployeeIdTest {

    @Test
    public void test_of_wraps_uuid_and_genId_is_unique() {
        UUID raw = UUID.randomUUID();

        EmployeeId id = EmployeeId.of(raw);

        assertThat(id.id()).isEqualTo(raw);
        assertThat(EmployeeId.genId()).isNotEqualTo(EmployeeId.genId());
    }

    @Test
    public void test_null_uuid_is_rejected() {
        assertThatThrownBy(() -> EmployeeId.of(null)).isInstanceOf(DomainObjectValidationException.class);
    }
}
