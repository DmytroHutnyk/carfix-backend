package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.carCatalog.CarBrandEntity;
import com.hutnyk.carfix.carCatalog.CarModelEntity;
import com.hutnyk.carfix.carCatalog.ModelGenerationEntity;
import com.hutnyk.carfix.customer.CustomerEntity;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.user.UserId;

public class CarProfileMapper {

    public static CarProfile toDomain(CarProfileEntity e) {
        if (e == null) return null;
        return CarProfile.of(
                CarProfileId.of(e.getId()),
                e.getName(),
                e.getVin(),
                e.getPlates(),
                e.getServiceCertificateDate(),
                e.getInsuranceDate(),
                UserId.of(e.getCustomerEntity().getId()),
                null,
                e.getModelGenerationEntity().getId()
        );
    }

    public static CarProfileView toView(CarProfileEntity e) {
        if (e == null) return null;
        ModelGenerationEntity mge = e.getModelGenerationEntity();
        CarModelEntity cme = mge.getCarModelEntity();
        CarBrandEntity cbe = cme.getCarBrandEntity();
        return new CarProfileView(
                e.getId(),
                e.getName(),
                e.getVin(),
                e.getPlates(),
                e.getServiceCertificateDate(),
                e.getInsuranceDate(),
                e.getCustomerEntity().getId(),
                null,
                cbe.getId(),
                cbe.getName(),
                cme.getId(),
                cme.getName(),
                mge.getId(),
                mge.getName()
        );
    }

    public static CarProfileEntity toEntity(CarProfile p, CustomerEntity customer, ModelGenerationEntity generation) {
        if (p == null) return null;
        return new CarProfileEntity(
                p.getId().id(),
                p.getName(),
                p.getVin(),
                p.getPlates(),
                p.getServiceCertificateDate(),
                p.getInsuranceDate(),
                customer,
                generation
        );
    }

    public static void updateEntity(CarProfileEntity e, CarProfile p, ModelGenerationEntity generation) {
        if (e == null || p == null) return;
        e.setName(p.getName());
        e.setVin(p.getVin());
        e.setPlates(p.getPlates());
        e.setServiceCertificateDate(p.getServiceCertificateDate());
        e.setInsuranceDate(p.getInsuranceDate());
        e.setModelGenerationEntity(generation);
    }
}
