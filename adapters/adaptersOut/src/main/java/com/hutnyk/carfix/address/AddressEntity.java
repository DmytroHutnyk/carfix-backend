package com.hutnyk.carfix.address;

import com.hutnyk.carfix.address.City;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "addresses")
@Entity
public class AddressEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer id;

    @Column(name = "street_name", nullable = false, length = 100)
    private String streetName;

    @Column(name = "building_number", nullable = false, length = 10)
    private String buildingNumober;

    @Column(name = "flat_number", length = 10)
    private String flatNumber;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity cityEntity;
}
