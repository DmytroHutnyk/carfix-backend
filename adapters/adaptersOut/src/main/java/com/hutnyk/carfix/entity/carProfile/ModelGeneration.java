package com.hutnyk.carfix.entity.carProfile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "model_generations")
public class ModelGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_generation_id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "start_production", nullable = false)
    private Short startProduction;

    @Column(name = "end_production")
    private Short endProduction;

    @ManyToOne
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModel carModel;

    @OneToMany(mappedBy = "modelGeneration")
    private Set<CarProfile> carProfiles;
}
