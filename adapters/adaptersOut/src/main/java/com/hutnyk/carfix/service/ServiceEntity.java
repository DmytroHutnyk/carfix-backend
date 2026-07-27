package com.hutnyk.carfix.service;

import com.hutnyk.carfix.service.ServiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

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

    // FK to service_bays(service_bay_id); becomes a @ManyToOne once ServiceBayEntity is mapped.
    @Column(name = "service_bay_id", nullable = false)
    private Integer serviceBayId;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategoryEntity serviceCategoryEntity;
}
