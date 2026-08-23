package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.hutnyk.carfix.branch.exception.InvalidBranchRegistrationException;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchAddressCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeRequirementCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEquipmentCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEquipmentRequirementCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchOpeningHoursCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceBayCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceCommand;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import com.hutnyk.carfix.service.ServiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class BranchRegistrationValidatorTest {

    static RegisterBranchAddressCommand address() {
        return new RegisterBranchAddressCommand("Nowogrodzka", "10", null, "00-511", "Warsaw",
                "Masovian Voivodeship", "PL", new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJx");
    }

    static RegisterBranchServiceCommand service(List<String> bayTypes, List<String> roles, List<String> equipmentTypes) {
        return new RegisterBranchServiceCommand("Oil & filter change", null, (short) 30, new BigDecimal("150.00"), 2,
                ServiceStatus.ACTIVE, bayTypes,
                List.of(new RegisterBranchEmployeeRequirementCommand("Mechanic", roles)),
                equipmentTypes.isEmpty()
                        ? List.of()
                        : List.of(new RegisterBranchEquipmentRequirementCommand("Lift", equipmentTypes)));
    }

    /** A complete, self-consistent command every test mutates one field of. */
    static RegisterBranchCommand valid() {
        return new RegisterBranchCommand(
                "AutoFix", "+48221234567", "kontakt@autofix.pl", "Europe/Warsaw", address(),
                List.of(new RegisterBranchOpeningHoursCommand(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), OpeningHoursMode.OPEN),
                        new RegisterBranchOpeningHoursCommand(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(14, 0), OpeningHoursMode.BY_APPOINTMENT)),
                Set.of(1, 2),
                List.of("Basic", "With lift"),
                List.of(new RegisterBranchServiceBayCommand("Bay 1", "Basic"),
                        new RegisterBranchServiceBayCommand("Bay 2", "with lift")),
                List.of("2-post lift"),
                List.of(new RegisterBranchEquipmentCommand("2-post lift #1", "2-post lift")),
                List.of("Mechanic", "EV high-voltage"),
                List.of(new RegisterBranchEmployeeCommand("Oleh", "Savchuk", List.of("EV high-voltage"))),
                List.of(service(List.of("Basic"), List.of("Mechanic", "EV high-voltage"), List.of("2-post lift"))));
    }

    static RegisterBranchCommand with(RegisterBranchCommand c,
                                      List<String> serviceBayTypes, List<RegisterBranchServiceBayCommand> serviceBays,
                                      List<String> equipmentTypes, List<RegisterBranchEquipmentCommand> equipment,
                                      List<String> roles, List<RegisterBranchEmployeeCommand> employees,
                                      List<RegisterBranchServiceCommand> services,
                                      List<RegisterBranchOpeningHoursCommand> openingHours) {
        return new RegisterBranchCommand(c.name(), c.phoneNumber(), c.email(), c.timezone(), c.address(),
                openingHours, c.carBrandIds(), serviceBayTypes, serviceBays, equipmentTypes, equipment, roles, employees, services);
    }

    @Test
    public void test_valid_command_passes() {
        //when + then
        assertThatCode(() -> BranchRegistrationValidator.validate(valid())).doesNotThrowAnyException();
    }

    @Test
    public void test_key_trims_and_lowercases() {
        //then
        assertThat(BranchRegistrationValidator.key("  With Lift ")).isEqualTo("with lift");
    }

    @Test
    public void test_duplicate_type_names_are_case_insensitive() {
        //given
        RegisterBranchCommand c = valid();
        RegisterBranchCommand duplicated = with(c, List.of("Basic", "basic "), c.serviceBays(), c.equipmentTypes(),
                c.equipment(), c.roles(), c.employees(), c.services(), c.openingHours());

        //when + then
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(duplicated))
                .isInstanceOf(InvalidBranchRegistrationException.class)
                .extracting("fieldName")
                .isEqualTo("serviceBayTypes");
    }

    @Test
    public void test_bay_referencing_undeclared_type_names_the_path() {
        //given
        RegisterBranchCommand c = valid();
        RegisterBranchCommand bad = with(c, c.serviceBayTypes(),
                List.of(new RegisterBranchServiceBayCommand("Bay 1", "Basic"), new RegisterBranchServiceBayCommand("Bay 9", "Pit")),
                c.equipmentTypes(), c.equipment(), c.roles(), c.employees(), c.services(), c.openingHours());

        //when + then
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(bad))
                .isInstanceOf(InvalidBranchRegistrationException.class)
                .satisfies(e -> assertThat(((InvalidBranchRegistrationException) e).details())
                        .containsExactly(entry("serviceBays[1].type", "Unknown service bay type: Pit")));
    }

    @Test
    public void test_equipment_employee_and_service_references_are_checked() {
        //given
        RegisterBranchCommand c = valid();

        //when + then
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), List.of(new RegisterBranchEquipmentCommand("Jack", "Trolley jack")),
                c.roles(), c.employees(), c.services(), c.openingHours())))
                .extracting("fieldName").isEqualTo("equipment[0].type");
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), c.equipment(), c.roles(),
                List.of(new RegisterBranchEmployeeCommand("A", "B", List.of("Mechanic", "Painter"))),
                c.services(), c.openingHours())))
                .extracting("fieldName").isEqualTo("employees[0].roles");
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), c.equipment(), c.roles(), c.employees(),
                List.of(service(List.of("Pit"), List.of("Mechanic"), List.of())), c.openingHours())))
                .extracting("fieldName").isEqualTo("services[0].bayTypes");
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), c.equipment(), c.roles(), c.employees(),
                List.of(service(List.of("Basic"), List.of("Welder"), List.of())), c.openingHours())))
                .extracting("fieldName").isEqualTo("services[0].employeeRequirements[0].roles");
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(with(c, c.serviceBayTypes(), c.serviceBays(),
                c.equipmentTypes(), c.equipment(), c.roles(), c.employees(),
                List.of(service(List.of("Basic"), List.of("Mechanic"), List.of("Brake lathe"))), c.openingHours())))
                .extracting("fieldName").isEqualTo("services[0].equipmentRequirements[0].types");
    }

    @Test
    public void test_weekday_listed_twice_is_rejected() {
        //given
        RegisterBranchCommand c = valid();
        RegisterBranchCommand bad = with(c, c.serviceBayTypes(), c.serviceBays(), c.equipmentTypes(), c.equipment(),
                c.roles(), c.employees(), c.services(),
                List.of(new RegisterBranchOpeningHoursCommand(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), OpeningHoursMode.OPEN),
                        new RegisterBranchOpeningHoursCommand(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(17, 0), OpeningHoursMode.OPEN)));

        //when + then
        assertThatThrownBy(() -> BranchRegistrationValidator.validate(bad))
                .isInstanceOf(InvalidBranchRegistrationException.class)
                .extracting("fieldName").isEqualTo("openingHours[1].dayOfWeek");
    }

    @Test
    public void test_empty_lists_are_valid() {
        //given
        RegisterBranchCommand c = valid();
        RegisterBranchCommand empty = with(c, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        //when + then
        assertThatCode(() -> BranchRegistrationValidator.validate(empty)).doesNotThrowAnyException();
    }
}
