package com.hutnyk.carfix.carProfile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "car_models")
public class CarModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_model_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "car_brand_id", nullable = false)
    private CarBrandEntity carBrandEntity;

    @OneToMany(mappedBy = "carModelEntity")
    private Set<ModelGenerationEntity> modelGenerationEntities;
}
