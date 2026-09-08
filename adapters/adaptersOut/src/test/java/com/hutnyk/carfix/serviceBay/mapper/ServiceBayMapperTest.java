package com.hutnyk.carfix.serviceBay.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ServiceBayMapperTest {

    private static final UUID BRANCH_UUID = UUID.randomUUID();

    private static BranchEntity branch() {
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_UUID);
        return branch;
    }

    @Test
    public void test_toTypeEntity_and_toTypeDomain_round_trip_with_branch() {
        ServiceBayType type = ServiceBayType.create("With lift", BranchId.of(BRANCH_UUID));

        ServiceBayTypeEntity entity = ServiceBayMapper.toTypeEntity(type, branch());
        entity.setId(4);
        ServiceBayType back = ServiceBayMapper.toTypeDomain(entity);

        assertThat(entity.getName()).isEqualTo("With lift");
        assertThat(entity.getBranchEntity().getId()).isEqualTo(BRANCH_UUID);
        assertThat(back.getId()).isEqualTo(4);
        assertThat(back.getBranchId()).isEqualTo(BranchId.of(BRANCH_UUID));
    }

    @Test
    public void test_toTypeDomain_platform_type_has_no_branch() {
        ServiceBayTypeEntity entity = new ServiceBayTypeEntity();
        entity.setId(1);
        entity.setName("Two-post lift");

        ServiceBayType type = ServiceBayMapper.toTypeDomain(entity);

        assertThat(type.getBranchId()).isNull();
    }

    @Test
    public void test_toEntity_copies_every_field_and_references() {
        ServiceBay bay = ServiceBay.create("Bay 1", 4, BranchId.of(BRANCH_UUID));
        ServiceBayTypeEntity type = new ServiceBayTypeEntity();
        type.setId(4);

        ServiceBayEntity entity = ServiceBayMapper.toEntity(bay, type, branch());
        entity.setId(10);
        ServiceBay back = ServiceBayMapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(10);
        assertThat(entity.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(entity.getServiceBayTypeEntity()).isSameAs(type);
        assertThat(back.getId()).isEqualTo(10);
        assertThat(back.getServiceBayTypeId()).isEqualTo(4);
        assertThat(back.getBranchId()).isEqualTo(BranchId.of(BRANCH_UUID));
    }

    @Test
    public void test_null_guards() {
        assertThat(ServiceBayMapper.toTypeEntity(null, null)).isNull();
        assertThat(ServiceBayMapper.toEntity((ServiceBay) null, null, null)).isNull();
    }
}
