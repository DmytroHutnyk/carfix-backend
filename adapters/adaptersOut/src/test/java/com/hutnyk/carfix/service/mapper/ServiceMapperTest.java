package com.hutnyk.carfix.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import com.hutnyk.carfix.service.entity.ServiceEmployeeRequirementEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import com.hutnyk.carfix.service.entity.ServiceEquipmentRequirementEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ServiceMapperTest {

    private static final UUID BRANCH_UUID = UUID.randomUUID();

    private static BranchEntity branch() {
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_UUID);
        return branch;
    }

    private static ServiceCategoryEntity category(int id) {
        ServiceCategoryEntity category = new ServiceCategoryEntity();
        category.setId(id);
        category.setName("Maintenance");
        return category;
    }

    private static ServiceBayTypeEntity bayType(int id) {
        ServiceBayTypeEntity type = new ServiceBayTypeEntity();
        type.setId(id);
        return type;
    }

    private static RoleEntity role(int id) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        return role;
    }

    private static EquipmentTypeEntity equipmentType(int id) {
        EquipmentTypeEntity type = new EquipmentTypeEntity();
        type.setId(id);
        return type;
    }

    private static Service service() {
        return Service.of(null, "Oil & filter change", null, (short) 30, new BigDecimal("150.00"),
                ServiceStatus.ACTIVE, BranchId.of(BRANCH_UUID), 2, Set.of(4, 5),
                List.of(EmployeeRequirement.of(null, "Mechanic", Set.of(7))),
                List.of(EquipmentRequirement.of(null, "Lift", Set.of(9))));
    }

    @Test
    public void test_toEntity_copies_scalars_and_bay_type_links_but_not_requirements() {
        //given
        Service service = service();

        //when
        ServiceEntity entity = ServiceMapper.toEntity(service, branch(), category(2), Set.of(bayType(4), bayType(5)));

        //then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Oil & filter change");
        assertThat(entity.getDurationMinutes()).isEqualTo((short) 30);
        assertThat(entity.getPrice()).isEqualByComparingTo("150.00");
        assertThat(entity.getStatus()).isEqualTo(ServiceStatus.ACTIVE);
        assertThat(entity.getBranchEntity().getId()).isEqualTo(BRANCH_UUID);
        assertThat(entity.getServiceCategoryEntity().getId()).isEqualTo(2);
        assertThat(entity.getServiceBayTypes()).extracting(ServiceBayTypeEntity::getId).containsExactlyInAnyOrder(4, 5);
        assertThat(entity.getEmployeeRequirements()).isNull();
        assertThat(entity.getEquipmentRequirements()).isNull();
    }

    @Test
    public void test_requirement_entities_point_back_at_the_service() {
        //given
        ServiceEntity service = new ServiceEntity();
        service.setId(12);

        //when
        ServiceEmployeeRequirementEntity employee = ServiceMapper.toEntity(
                EmployeeRequirement.of(null, "Mechanic", Set.of(7)), service, Set.of(role(7)));
        ServiceEquipmentRequirementEntity equipment = ServiceMapper.toEntity(
                EquipmentRequirement.of(null, "Lift", Set.of(9)), service, Set.of(equipmentType(9)));

        //then
        assertThat(employee.getName()).isEqualTo("Mechanic");
        assertThat(employee.getServiceEntity()).isSameAs(service);
        assertThat(employee.getRoles()).extracting(RoleEntity::getId).containsExactly(7);
        assertThat(equipment.getName()).isEqualTo("Lift");
        assertThat(equipment.getServiceEntity()).isSameAs(service);
        assertThat(equipment.getEquipmentTypes()).extracting(EquipmentTypeEntity::getId).containsExactly(9);
    }

    @Test
    public void test_toDomain_reads_the_full_graph_and_category() {
        //given
        ServiceEntity entity = ServiceMapper.toEntity(service(), branch(), category(2), Set.of(bayType(4)));
        entity.setId(12);
        ServiceEmployeeRequirementEntity employee = ServiceMapper.toEntity(
                EmployeeRequirement.of(null, "Mechanic", Set.of(7)), entity, Set.of(role(7)));
        employee.setId(31);
        entity.setEmployeeRequirements(Set.of(employee));
        entity.setEquipmentRequirements(Set.of());

        //when
        Service back = ServiceMapper.toDomain(entity);
        ServiceCategory category = ServiceMapper.toDomain(category(2));

        //then
        assertThat(back.getId()).isEqualTo(12);
        assertThat(back.getServiceBayTypeIds()).containsExactly(4);
        assertThat(back.getEmployeeRequirements()).singleElement()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(31);
                    assertThat(r.getRoleIds()).containsExactly(7);
                });
        assertThat(back.getEquipmentRequirements()).isEmpty();
        assertThat(category.getId()).isEqualTo(2);
        assertThat(category.getName()).isEqualTo("Maintenance");
    }

    @Test
    public void test_null_guards() {
        //when + then
        assertThat(ServiceMapper.toEntity((Service) null, null, null, null)).isNull();
        assertThat(ServiceMapper.toEntity((EmployeeRequirement) null, null, null)).isNull();
        assertThat(ServiceMapper.toEntity((EquipmentRequirement) null, null, null)).isNull();
        assertThat(ServiceMapper.toDomain((ServiceCategoryEntity) null)).isNull();
    }
}
