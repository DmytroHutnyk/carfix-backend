package com.hutnyk.carfix.user.entity;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.user.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
@Entity
public class UserEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String surname;

    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "ph_country_code", length = 4, nullable = false)
    private String phoneCountryCode;

    @Column(name = "email", length = 30, nullable = false)
    private String email;

    @ToString.Exclude
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private AddressEntity addressEntity;

    @Column(name = "preferred_city", length = 100)
    private String preferredCity;

    @Column(name = "preferred_region", length = 100)
    private String preferredRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_country_iso", length = 2)
    private CountryIso preferredCountryIso;

    @Column(name = "preferred_latitude", precision = 9, scale = 6)
    private BigDecimal preferredLatitude;

    @Column(name = "preferred_longitude", precision = 9, scale = 6)
    private BigDecimal preferredLongitude;
}
