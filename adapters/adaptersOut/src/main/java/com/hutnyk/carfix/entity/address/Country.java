package com.hutnyk.carfix.entity.address;

import com.hutnyk.carfix.address.CountryIso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "countries")
public class Country {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "iso", length = 2, nullable = false)
    private CountryIso iso;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
