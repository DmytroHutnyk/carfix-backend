package com.hutnyk.carfix.address.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "regions")
public class RegionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "countries_iso")
    private CountryEntity countryEntity;

    @OneToMany(mappedBy = "regionEntity")
    private Set<CityEntity>  cities;
}
