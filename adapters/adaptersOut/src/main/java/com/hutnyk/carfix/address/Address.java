package com.hutnyk.carfix.address;

import com.hutnyk.carfix.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer id;

    @Column(name = "building_number", nullable = false, length = 10)
    private String buildingNumber;

    @Column(name = "flat_number", length = 10)
    private String flatNumber;

    @OneToOne(mappedBy = "address")
    private User user;

    @ManyToOne
    @JoinColumn(name = "street_id", nullable = false)
    private Street street;

    @ManyToOne
    @JoinColumn(name = "postal_code_id", nullable = false)
    private PostalCode postalCode;
}
