package com.hutnyk.carfix.entity.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "postal_codes")
public class PostalCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postal_code_id")
    private Integer id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity cityEntity;

    @OneToMany(mappedBy = "postalCodeEntity")
    private Set<AddressEntity> addressEntities;
}
