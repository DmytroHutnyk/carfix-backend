package com.hutnyk.carfix.carCatalog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "car_models")
public class CarModelEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_model_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "car_brand_id", nullable = false)
    private CarBrandEntity carBrandEntity;
}
