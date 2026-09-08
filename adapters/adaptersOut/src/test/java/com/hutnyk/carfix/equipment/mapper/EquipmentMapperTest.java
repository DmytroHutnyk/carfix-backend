package com.hutnyk.carfix.equipment.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EquipmentMapperTest {

    private static EquipmentTypeEntity type(int id, String name) {
        EquipmentTypeEntity t = new EquipmentTypeEntity();
        t.setId(id);
        t.setName(name);
        return t;
    }

    private static BranchEntity branch(UUID id) {
        BranchEntity b = new BranchEntity();
        b.setId(id);
        return b;
    }

    @Test
    public void test_updateEntity_rewrites_scalars_and_type_but_keeps_id_and_branch() {
        //given
        BranchEntity branch = branch(UUID.randomUUID());
        EquipmentEntity entity = new EquipmentEntity();
        entity.setId(42);
        entity.setName("old");
        entity.setNotes("worn");
        entity.setStatus(EquipmentStatus.SUSPENDED);
        entity.setEquipmentTypeEntity(type(7, "Lift"));
        entity.setBranchEntity(branch);

        Equipment domain = Equipment.of(
                42, "new lift", "serviced", EquipmentStatus.SUSPENDED, 9, BranchId.of(branch.getId()));
        EquipmentTypeEntity newType = type(9, "2-post lift");

        //when
        EquipmentMapper.updateEntity(entity, domain, newType);

        //then
        assertThat(entity.getId()).isEqualTo(42);
        assertThat(entity.getName()).isEqualTo("new lift");
        assertThat(entity.getNotes()).isEqualTo("serviced");
        assertThat(entity.getStatus()).isEqualTo(EquipmentStatus.SUSPENDED);
        assertThat(entity.getEquipmentTypeEntity()).isSameAs(newType);
        assertThat(entity.getBranchEntity()).isSameAs(branch);
    }

    @Test
    public void test_toOwnerView_reads_the_type_name_and_carries_null_notes() {
        //given
        EquipmentEntity entity = new EquipmentEntity();
        entity.setId(5);
        entity.setName("2-post lift #1");
        entity.setNotes(null);
        entity.setStatus(EquipmentStatus.ACTIVE);
        entity.setEquipmentTypeEntity(type(7, "2-post lift"));

        //when
        OwnerEquipmentView view = EquipmentMapper.toOwnerView(entity);

        //then
        assertThat(view.id()).isEqualTo(5);
        assertThat(view.name()).isEqualTo("2-post lift #1");
        assertThat(view.typeName()).isEqualTo("2-post lift");
        assertThat(view.notes()).isNull();
        assertThat(view.status()).isEqualTo(EquipmentStatus.ACTIVE);
    }

    @Test
    public void test_toOwnerView_null_guard() {
        //when + then
        assertThat(EquipmentMapper.toOwnerView(null)).isNull();
    }
}
