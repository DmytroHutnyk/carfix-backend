package com.hutnyk.carfix.branch.exception;

import com.hutnyk.carfix.exception.ValidationException;
import com.hutnyk.carfix.openingHours.DayOfWeek;

public class InvalidBranchRegistrationException extends ValidationException {

    private InvalidBranchRegistrationException(String field, String message, Object rejectedValue) {
        super(BranchErrorCode.INVALID_BRANCH_REGISTRATION, message, field, rejectedValue);
    }

    public static InvalidBranchRegistrationException duplicateName(String field, String name) {
        return new InvalidBranchRegistrationException(field, "Duplicate name: " + name, name);
    }

    public static InvalidBranchRegistrationException unknownReference(String field, String what, String name) {
        return new InvalidBranchRegistrationException(field, "Unknown " + what + ": " + name, name);
    }

    public static InvalidBranchRegistrationException duplicateWeekday(String field, DayOfWeek day) {
        return new InvalidBranchRegistrationException(field, "Weekday listed twice: " + day, day);
    }
}
