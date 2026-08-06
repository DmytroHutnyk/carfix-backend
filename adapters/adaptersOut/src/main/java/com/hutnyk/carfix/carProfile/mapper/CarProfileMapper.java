package com.hutnyk.carfix.carProfile.mapper;

import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;
import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.customer.entity.CustomerEntity;
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
                e.getModelVersionEntity().getId()
        );
    }

    public static CarProfileView toView(CarProfileEntity e) {
        if (e == null) return null;
        ModelVersionEntity mve = e.getModelVersionEntity();
        CarModelEntity cme = mve.getCarModelEntity();
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
                mve.getId(),
                mve.getName()
        );
    }

    public static CarProfileEntity toEntity(CarProfile p, CustomerEntity customer, ModelVersionEntity version) {
        if (p == null) return null;
        return new CarProfileEntity(
                p.getId().id(),
                p.getName(),
                p.getVin(),
                p.getPlates(),
                p.getServiceCertificateDate(),
                p.getInsuranceDate(),
                customer,
                version
        );
    }

    public static void updateEntity(CarProfileEntity e, CarProfile p, ModelVersionEntity version) {
        if (e == null || p == null) return;
        e.setName(p.getName());
        e.setVin(p.getVin());
        e.setPlates(p.getPlates());
        e.setServiceCertificateDate(p.getServiceCertificateDate());
        e.setInsuranceDate(p.getInsuranceDate());
        e.setModelVersionEntity(version);
    }
}
