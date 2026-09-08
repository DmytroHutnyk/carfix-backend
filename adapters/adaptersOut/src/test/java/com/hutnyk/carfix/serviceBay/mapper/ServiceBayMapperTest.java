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
        //given
        ServiceBayType type = ServiceBayType.create("With lift", BranchId.of(BRANCH_UUID));

        //when
        ServiceBayTypeEntity entity = ServiceBayMapper.toTypeEntity(type, branch());
        entity.setId(4);
        ServiceBayType back = ServiceBayMapper.toTypeDomain(entity);

        //then
        assertThat(entity.getName()).isEqualTo("With lift");
        assertThat(entity.getBranchEntity().getId()).isEqualTo(BRANCH_UUID);
        assertThat(back.getId()).isEqualTo(4);
        assertThat(back.getBranchId()).isEqualTo(BranchId.of(BRANCH_UUID));
    }

    @Test
    public void test_toTypeDomain_platform_type_has_no_branch() {
        //given
        ServiceBayTypeEntity entity = new ServiceBayTypeEntity();
        entity.setId(1);
        entity.setName("Two-post lift");

        //when
        ServiceBayType type = ServiceBayMapper.toTypeDomain(entity);

        //then
        assertThat(type.getBranchId()).isNull();
    }

    @Test
    public void test_toEntity_copies_every_field_and_references() {
        //given
        ServiceBay bay = ServiceBay.create("Bay 1", 4, BranchId.of(BRANCH_UUID));
        ServiceBayTypeEntity type = new ServiceBayTypeEntity();
        type.setId(4);

        //when
        ServiceBayEntity entity = ServiceBayMapper.toEntity(bay, type, branch());
        entity.setId(10);
        ServiceBay back = ServiceBayMapper.toDomain(entity);

        //then
        assertThat(entity.getId()).isEqualTo(10);
        assertThat(entity.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(entity.getServiceBayTypeEntity()).isSameAs(type);
        assertThat(back.getId()).isEqualTo(10);
        assertThat(back.getServiceBayTypeId()).isEqualTo(4);
        assertThat(back.getBranchId()).isEqualTo(BranchId.of(BRANCH_UUID));
    }

    @Test
    public void test_null_guards() {
        //when + then
        assertThat(ServiceBayMapper.toTypeEntity(null, null)).isNull();
        assertThat(ServiceBayMapper.toEntity((ServiceBay) null, null, null)).isNull();
    }

    @Test
    public void test_toOwnerView_flattens_type_name() {
        ServiceBayTypeEntity type = new ServiceBayTypeEntity();
        type.setId(5);
        type.setName("Basic");
        ServiceBayEntity entity = new ServiceBayEntity();
        entity.setId(10);
        entity.setName("Bay 1");
        entity.setStatus(ServiceBayStatus.ACTIVE);
        entity.setNotes("note");
        entity.setServiceBayTypeEntity(type);
        entity.setBranchEntity(branch());

        com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView view = ServiceBayMapper.toOwnerView(entity);

        assertThat(view.id()).isEqualTo(10);
        assertThat(view.typeId()).isEqualTo(5);
        assertThat(view.typeName()).isEqualTo("Basic");
        assertThat(view.notes()).isEqualTo("note");
        assertThat(view.status()).isEqualTo(ServiceBayStatus.ACTIVE);
    }

    @Test
    public void test_updateEntity_overwrites_editable_fields_and_type_reference() {
        ServiceBayEntity entity = new ServiceBayEntity();
        entity.setId(10);
        entity.setName("Old");
        entity.setStatus(ServiceBayStatus.SUSPENDED);
        entity.setNotes("old");
        ServiceBayTypeEntity newType = new ServiceBayTypeEntity();
        newType.setId(7);
        ServiceBay updated = ServiceBay.of(10, "Old", ServiceBayStatus.SUSPENDED, "old", 2, BranchId.of(BRANCH_UUID))
                .update("New", 7, "new");

        ServiceBayMapper.updateEntity(entity, updated, newType);

        assertThat(entity.getName()).isEqualTo("New");
        assertThat(entity.getStatus()).isEqualTo(ServiceBayStatus.SUSPENDED);
        assertThat(entity.getNotes()).isEqualTo("new");
        assertThat(entity.getServiceBayTypeEntity()).isSameAs(newType);
    }
}
