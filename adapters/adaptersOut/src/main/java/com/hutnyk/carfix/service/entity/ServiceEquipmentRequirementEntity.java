package com.hutnyk.carfix.service.entity;

import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "service_equipment_requirements")
@Entity
public class ServiceEquipmentRequirementEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_equipment_requirement_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity serviceEntity;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "service_equipment_requirement_types",
            joinColumns = @JoinColumn(name = "service_equipment_requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_type_id"))
    private Set<EquipmentTypeEntity> equipmentTypes;
}
