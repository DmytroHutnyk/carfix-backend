package com.hutnyk.carfix.serviceBay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "service_bay_types")
@Entity
public class ServiceBayTypeEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_bay_type_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 40, nullable = false)
    private String name;
}
