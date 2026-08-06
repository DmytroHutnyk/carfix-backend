package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.carCatalog.exception.ModelVersionNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.carProfile.CarProfilePortIn;
import com.hutnyk.carfix.in.carProfile.commands.CreateCarProfileCommand;
import com.hutnyk.carfix.in.carProfile.commands.UpdateCarProfileCommand;
import com.hutnyk.carfix.out.carCatalog.CarCatalogPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;
import com.hutnyk.carfix.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationService
@RequiredArgsConstructor
public class CarProfileService implements CarProfilePortIn {

    private final CustomerPortOut customerPortOut;
    private final CarProfilePortOut carProfilePortOut;
    private final CarCatalogPortOut carCatalogPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<CarProfileView> getMyCarProfiles(String email) {
        Customer customer = customerPortOut.loadCustomerByUsername(email);
        return carProfilePortOut.findAllByCustomerId(customer.getUser().getId().id());
    }

    @Override
    public CarProfileView createCarProfile(String email, CreateCarProfileCommand cmd) {
        if (!carCatalogPortOut.existsVersionById(cmd.modelVersionId())) {
            throw new ModelVersionNotFoundException(cmd.modelVersionId());
        }
        Customer customer = customerPortOut.loadCustomerByUsername(email);
        UUID customerId = customer.getUser().getId().id();

        CarProfile profile = CarProfile.create(
                CarProfileId.genId(),
                cmd.name(),
                cmd.vin(),
                cmd.plates(),
                cmd.serviceCertificateDate(),
                cmd.insuranceDate(),
                customer.getUser().getId(),
                null,
                cmd.modelVersionId()
        );
        carProfilePortOut.insert(profile);

        return carProfilePortOut.findByIdAndCustomerId(profile.getId().id(), customerId)
                .orElseThrow(() -> new UnexpectedStateException(
                        "Car profile disappeared right after insert: " + profile.getId().id()));
    }

    @Override
    public CarProfileView updateCarProfile(String email, UUID profileId, UpdateCarProfileCommand cmd) {
        Customer customer = customerPortOut.loadCustomerByUsername(email);
        UUID customerId = customer.getUser().getId().id();

        CarProfileView existing = carProfilePortOut
                .findByIdAndCustomerId(profileId, customerId)
                .orElseThrow(() -> new CarProfileNotFoundException(profileId));

        if (!cmd.modelVersionId().equals(existing.versionId())
                && !carCatalogPortOut.existsVersionById(cmd.modelVersionId())) {
            throw new ModelVersionNotFoundException(cmd.modelVersionId());
        }

        CarProfile updated = CarProfile.create(
                CarProfileId.of(existing.id()),
                cmd.name(),
                cmd.vin(),
                cmd.plates(),
                cmd.serviceCertificateDate(),
                cmd.insuranceDate(),
                UserId.of(existing.customerId()),
                existing.fileId(),
                cmd.modelVersionId()
        );
        carProfilePortOut.update(updated);

        return carProfilePortOut.findByIdAndCustomerId(profileId, customerId)
                .orElseThrow(() -> new UnexpectedStateException(
                        "Car profile disappeared right after update: " + profileId));
    }

    @Override
    public void deleteCarProfile(String email, UUID profileId) {
        Customer customer = customerPortOut.loadCustomerByUsername(email);
        if (!carProfilePortOut.existsByIdAndCustomerId(profileId, customer.getUser().getId().id())) {
            throw new CarProfileNotFoundException(profileId);
        }
        carProfilePortOut.deleteById(profileId);
    }
}
