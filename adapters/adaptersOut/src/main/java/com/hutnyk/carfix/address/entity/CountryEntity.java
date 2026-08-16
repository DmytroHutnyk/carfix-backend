package com.hutnyk.carfix.address.entity;

import com.hutnyk.carfix.address.CountryIso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "countries")
public class CountryEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "iso", length = 2, nullable = false)
    private CountryIso iso;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
