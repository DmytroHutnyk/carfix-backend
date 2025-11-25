package com.hutnyk.carfix.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "postal_codes")
public class PostalCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postal_code_id")
    private Integer id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @OneToMany(mappedBy = "postalCode")
    private Set<Address> addresses;
}
