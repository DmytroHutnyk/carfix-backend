package com.hutnyk.carfix.carProfile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity(name = "car_brands")
public class CarBrandEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_brand_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @ToString.Exclude
    @OneToMany(mappedBy = "carBrandEntity")
    private Set<CarModelEntity> carModelEntities;
}
