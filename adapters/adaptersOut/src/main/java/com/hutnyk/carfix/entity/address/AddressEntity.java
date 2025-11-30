package com.hutnyk.carfix.entity.address;

import com.hutnyk.carfix.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "addresses")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer id;

    @Column(name = "building_number", nullable = false, length = 10)
    private String buildingNumber;

    @Column(name = "flat_number", length = 10)
    private String flatNumber;

    @OneToOne(mappedBy = "addressEntity")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "street_id", nullable = false)
    private StreetEntity streetEntity;

    @ManyToOne
    @JoinColumn(name = "postal_code_id", nullable = false)
    private PostalCodeEntity postalCodeEntity;
}
