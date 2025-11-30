package com.hutnyk.carfix.entity.user;

import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.entity.carProfile.CarProfileEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "customers")
public class CustomerEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus customerStatus;

    @OneToMany(mappedBy = "customerEntity")
    private Set<CarProfileEntity> carProfileEntities;
}
