package com.hutnyk.carfix.out.carProfile;

import com.hutnyk.carfix.carProfile.CarProfile;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarProfilePortOut {
    List<CarProfileView> findAllByCustomerId(UUID customerId);
    Optional<CarProfileView> findByIdAndCustomerId(UUID profileId, UUID customerId);
    boolean existsByIdAndCustomerId(UUID profileId, UUID customerId);
    CarProfile insert(CarProfile profile);
    CarProfile update(CarProfile profile);
    void deleteById(UUID profileId);
}
