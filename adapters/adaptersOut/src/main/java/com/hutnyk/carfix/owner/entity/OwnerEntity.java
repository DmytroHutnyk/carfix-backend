package com.hutnyk.carfix.owner.entity;

import com.hutnyk.carfix.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "owners")
@Entity
public class OwnerEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "business_name", length = 100, nullable = false)
    private String businessName;

    @Column(name = "vat_in", length = 15, nullable = false)
    private String vatIn;

    @Column(name = "regon", length = 9, nullable = false)
    private String regon;
}
