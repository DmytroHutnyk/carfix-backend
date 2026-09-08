package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.branch.exception.InvalidBranchRegistrationException;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchOpeningHoursCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceCommand;
import com.hutnyk.carfix.openingHours.DayOfWeek;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Cross-field checks a registration must pass before any row is written: type names unique per list,
 * every name reference resolves to a declared type, at most one opening-hours row per weekday.
 * Pure — no ports. Names are matched trimmed and case-insensitively ({@link #key}).
 */
public final class BranchRegistrationValidator {

    private BranchRegistrationValidator() {
    }

    public static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    public static void validate(RegisterBranchCommand cmd) {
        Set<String> bayTypes = uniqueKeys(cmd.serviceBayTypes(), "serviceBayTypes");
        Set<String> equipmentTypes = uniqueKeys(cmd.equipmentTypes(), "equipmentTypes");
        Set<String> roles = uniqueKeys(cmd.roles(), "roles");
        requireUniqueWeekdays(cmd.openingHours());

        for (int i = 0; i < cmd.serviceBays().size(); i++) {
            requireKnown(bayTypes, cmd.serviceBays().get(i).type(), "serviceBays[" + i + "].type", "service bay type");
        }
        for (int i = 0; i < cmd.equipment().size(); i++) {
            requireKnown(equipmentTypes, cmd.equipment().get(i).type(), "equipment[" + i + "].type", "equipment type");
        }
        for (int i = 0; i < cmd.employees().size(); i++) {
            RegisterBranchEmployeeCommand employee = cmd.employees().get(i);
            requireAllKnown(roles, employee.roles(), "employees[" + i + "].roles", "role");
        }
        for (int i = 0; i < cmd.services().size(); i++) {
            RegisterBranchServiceCommand service = cmd.services().get(i);
            String path = "services[" + i + "]";
            requireAllKnown(bayTypes, service.bayTypes(), path + ".bayTypes", "service bay type");
            for (int j = 0; j < service.employeeRequirements().size(); j++) {
                requireAllKnown(roles, service.employeeRequirements().get(j).roles(),
                        path + ".employeeRequirements[" + j + "].roles", "role");
            }
            for (int j = 0; j < service.equipmentRequirements().size(); j++) {
                requireAllKnown(equipmentTypes, service.equipmentRequirements().get(j).types(),
                        path + ".equipmentRequirements[" + j + "].types", "equipment type");
            }
        }
    }

    private static Set<String> uniqueKeys(List<String> names, String field) {
        Set<String> keys = new HashSet<>();
        for (String name : names) {
            if (!keys.add(key(name))) {
                throw InvalidBranchRegistrationException.duplicateName(field, name);
            }
        }
        return keys;
    }

    private static void requireAllKnown(Set<String> declared, List<String> references, String field, String what) {
        for (String reference : references) {
            requireKnown(declared, reference, field, what);
        }
    }

    private static void requireKnown(Set<String> declared, String reference, String field, String what) {
        if (!declared.contains(key(reference))) {
            throw InvalidBranchRegistrationException.unknownReference(field, what, reference);
        }
    }

    public static void requireUniqueWeekdays(List<RegisterBranchOpeningHoursCommand> hours) {
        Set<DayOfWeek> seen = EnumSet.noneOf(DayOfWeek.class);
        for (int i = 0; i < hours.size(); i++) {
            if (!seen.add(hours.get(i).dayOfWeek())) {
                throw InvalidBranchRegistrationException.duplicateWeekday(
                        "openingHours[" + i + "].dayOfWeek", hours.get(i).dayOfWeek());
            }
        }
    }
}
