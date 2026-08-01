package com.hutnyk.carfix.in;

import com.hutnyk.carfix.in.commands.CreateCarProfileCommand;
import com.hutnyk.carfix.in.commands.UpdateCarProfileCommand;
import com.hutnyk.carfix.query.CarProfileView;

import java.util.List;
import java.util.UUID;

public interface CarProfilePortIn {
    List<CarProfileView> getMyCarProfiles(String customerEmail);
    CarProfileView createCarProfile(String customerEmail, CreateCarProfileCommand command);
    CarProfileView updateCarProfile(String customerEmail, UUID profileId, UpdateCarProfileCommand command);
    void deleteCarProfile(String customerEmail, UUID profileId);
}
