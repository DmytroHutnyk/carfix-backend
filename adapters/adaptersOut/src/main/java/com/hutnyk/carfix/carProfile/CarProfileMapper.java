package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.entity.user.CustomerEntity;
import com.hutnyk.carfix.query.CarBrandView;
import com.hutnyk.carfix.query.CarModelView;
import com.hutnyk.carfix.query.CarProfileView;
import com.hutnyk.carfix.query.ModelGenerationView;
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

    public static CarBrandView toBrandView(CarBrandEntity e) {
        if (e == null) return null;
        return new CarBrandView(e.getId(), e.getName());
    }

    public static CarModelView toModelView(CarModelEntity e) {
        if (e == null) return null;
        return new CarModelView(e.getId(), e.getName(), e.getCarBrandEntity().getId());
    }

    public static ModelGenerationView toGenerationView(ModelGenerationEntity e) {
        if (e == null) return null;
        return new ModelGenerationView(e.getId(), e.getName(), e.getStartProduction(), e.getEndProduction(), e.getCarModelEntity().getId());
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
