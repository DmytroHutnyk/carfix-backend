package com.hutnyk.carfix.entity.carProfile;

import com.hutnyk.carfix.entity.user.CustomerEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity(name = "car_profiles")
public class CarProfileEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "car_profile_id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "plates", length = 10)
    private String plates;

    @Column(name = "service_certificate_date")
    private LocalDate serviceCertificateDate;

    @Column(name = "insurance_date")
    private LocalDate insuranceDate;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customerEntity;

//    @Column(name = "file_id")
//    private Integer fileId;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "model_generation_id", nullable = false)
    private ModelGenerationEntity modelGenerationEntity;


}
