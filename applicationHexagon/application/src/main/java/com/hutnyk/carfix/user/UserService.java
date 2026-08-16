package com.hutnyk.carfix.user;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.CityResolver;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.LocationCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;

import java.util.Optional;

@ApplicationService
public class UserService implements UserPortIn {

    private final UserPortOut userPortOut;
    private final AddressPortOut addressPortOut;
    private final CityResolver cityResolver;

    public UserService(UserPortOut userPortOut, AddressPortOut addressPortOut) {
        this.userPortOut = userPortOut;
        this.addressPortOut = addressPortOut;
        this.cityResolver = new CityResolver(addressPortOut);
    }

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userPortOut.loadUserByEmail(email);
    }

    @Override
    public User updateUser(String email, UpdateUserCommand command) {
        User existing = loadOrThrow(email);
        LocationCommand preferred = command.preferredLocation();
        Integer preferredCityId = preferred == null
                ? null
                : cityResolver.resolveCityId(preferred.city(), preferred.region(),
                        CountryIso.parse(preferred.countryIso()), preferred.latitude(), preferred.longitude());

        User user = User.of(
                existing.getId(), command.name(), command.surname(), existing.getPhoneNumber(), existing.getEmail(),
                existing.getRole(), existing.getPasswordHash(), command.dateOfBirth(), existing.getAddressId(),
                preferredCityId);
        return userPortOut.update(user);
    }

    @Override
    public AddressView updateAddress(String email, UpdateUserAddressCommand command) {
        User existing = loadOrThrow(email);
        CountryIso countryIso = CountryIso.parse(command.countryIso());
        Integer cityId = cityResolver.resolveCityId(command.city(), command.region(), countryIso);

        Address address = Address.of(
                existing.getAddressId(),
                command.streetName(),
                command.buildingNumber(),
                command.flatNumber(),
                command.postalCode(),
                cityId,
                command.latitude(),
                command.longitude(),
                command.googlePlaceId()
        );

        Address saved;
        if (existing.getAddressId() == null) {
            saved = addressPortOut.insert(address);
            userPortOut.update(existing.linkAddress(saved.getId()));
        } else {
            saved = addressPortOut.update(address);
        }

        return addressPortOut.loadView(saved.getId())
                .orElseThrow(() -> new UnexpectedStateException("Address vanished right after write: " + saved.getId()));
    }

    @Override
    public void deleteAddress(String email) {
        User existing = loadOrThrow(email);
        if (existing.getAddressId() == null) {
            return;
        }
        userPortOut.update(existing.unlinkAddress());
        addressPortOut.deleteById(existing.getAddressId());
    }

    private User loadOrThrow(String email) {
        return userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(email));
    }
}
