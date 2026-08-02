package com.hutnyk.carfix.carProfile.adapter;

import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;
import com.hutnyk.carfix.carCatalog.repository.ModelVersionRepository;
import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.carProfile.mapper.CarProfileMapper;
import com.hutnyk.carfix.carProfile.repository.CarProfileRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.customer.entity.CustomerEntity;
import com.hutnyk.carfix.customer.repository.CustomerRepository;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class CarProfileAdapterOut implements CarProfilePortOut {

    private final CarProfileRepository carProfileRepository;
    private final CustomerRepository customerRepository;
    private final ModelVersionRepository modelVersionRepository;

    @Override
    public List<CarProfileView> findAllByCustomerId(UUID customerId) {
        return carProfileRepository.findAllByCustomerIdWithDetails(customerId).stream()
                .map(CarProfileMapper::toView)
                .toList();
    }

    @Override
    public Optional<CarProfileView> findByIdAndCustomerId(UUID profileId, UUID customerId) {
        return carProfileRepository.findByIdAndCustomerIdWithDetails(profileId, customerId)
                .map(CarProfileMapper::toView);
    }

    @Override
    public boolean existsByIdAndCustomerId(UUID profileId, UUID customerId) {
        return carProfileRepository.existsByIdAndCustomerEntityId(profileId, customerId);
    }

    @Override
    public CarProfile insert(CarProfile profile) {
        CustomerEntity customer = customerRepository.getReferenceById(profile.getCustomerId().id());
        ModelVersionEntity version = modelVersionRepository.getReferenceById(profile.getModelVersionId());
        return CarProfileMapper.toDomain(carProfileRepository.save(CarProfileMapper.toEntity(profile, customer, version)));
    }

    @Override
    public CarProfile update(CarProfile profile) {
        CarProfileEntity entity = carProfileRepository.findById(profile.getId().id())
                .orElseThrow(() -> new UnexpectedStateException(
                        "Car profile row missing on update: " + profile.getId().id()));

        ModelVersionEntity version = modelVersionRepository.getReferenceById(profile.getModelVersionId());
        CarProfileMapper.updateEntity(entity, profile, version);

        return CarProfileMapper.toDomain(carProfileRepository.save(entity));
    }

    @Override
    public void deleteById(UUID profileId) {
        carProfileRepository.deleteById(profileId);
    }
}
