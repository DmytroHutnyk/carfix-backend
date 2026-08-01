package com.hutnyk.carfix.in.carProfile;

import com.hutnyk.carfix.in.carProfile.commands.CreateCarProfileCommand;
import com.hutnyk.carfix.in.carProfile.commands.UpdateCarProfileCommand;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;

import java.util.List;
import java.util.UUID;

public interface CarProfilePortIn {
    List<CarProfileView> getMyCarProfiles(String customerEmail);
    CarProfileView createCarProfile(String customerEmail, CreateCarProfileCommand command);
    CarProfileView updateCarProfile(String customerEmail, UUID profileId, UpdateCarProfileCommand command);
    void deleteCarProfile(String customerEmail, UUID profileId);
}
