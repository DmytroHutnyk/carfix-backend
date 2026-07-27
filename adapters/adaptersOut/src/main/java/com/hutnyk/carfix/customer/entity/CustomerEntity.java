package com.hutnyk.carfix.customer.entity;

import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "customers")
@Entity
public class CustomerEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus customerStatus;

    @ToString.Exclude
    @OneToMany(mappedBy = "customerEntity")
    private Set<CarProfileEntity> carProfileEntities;
}
