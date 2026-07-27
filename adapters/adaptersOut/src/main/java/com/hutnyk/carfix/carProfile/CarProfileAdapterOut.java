package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.carCatalog.ModelGenerationEntity;
import com.hutnyk.carfix.carCatalog.ModelGenerationRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.customer.CustomerEntity;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class CarProfileAdapterOut implements CarProfilePortOut {

    private final CarProfileRepository carProfileRepository;
    private final CustomerRepository customerRepository;
    private final ModelGenerationRepository modelGenerationRepository;

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
        ModelGenerationEntity generation = modelGenerationRepository.getReferenceById(profile.getModelGenerationId());
        return CarProfileMapper.toDomain(carProfileRepository.save(CarProfileMapper.toEntity(profile, customer, generation)));
    }

    @Override
    public CarProfile update(CarProfile profile) {
        CarProfileEntity entity = carProfileRepository.findById(profile.getId().id()).orElseThrow(IllegalStateException::new);

        ModelGenerationEntity generation = modelGenerationRepository.getReferenceById(profile.getModelGenerationId());
        CarProfileMapper.updateEntity(entity, profile, generation);

        return CarProfileMapper.toDomain(carProfileRepository.save(entity));
    }

    @Override
    public void deleteById(UUID profileId) {
        carProfileRepository.deleteById(profileId);
    }
}
