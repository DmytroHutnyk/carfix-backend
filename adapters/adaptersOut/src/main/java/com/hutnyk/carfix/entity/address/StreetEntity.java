package com.hutnyk.carfix.entity.address;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Set;


@NoArgsConstructor
@Entity
@Table(name = "streets")
public class StreetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "street_id")
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "streetEntity")
    private Set<AddressEntity> addressEntities;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity cityEntity;


}
