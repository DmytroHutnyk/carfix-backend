package com.hutnyk.carfix.service.entity;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "services")
@Entity
public class ServiceEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Short durationMinutes;

    @Column(name = "price", nullable = false, precision = 7, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategoryEntity serviceCategoryEntity;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "services_service_bay_types",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "service_bay_type_id"))
    private Set<ServiceBayTypeEntity> serviceBayTypes;

    @ToString.Exclude
    @OneToMany(mappedBy = "serviceEntity", fetch = FetchType.LAZY)
    private Set<ServiceEmployeeRequirementEntity> employeeRequirements;

    @ToString.Exclude
    @OneToMany(mappedBy = "serviceEntity", fetch = FetchType.LAZY)
    private Set<ServiceEquipmentRequirementEntity> equipmentRequirements;
}
