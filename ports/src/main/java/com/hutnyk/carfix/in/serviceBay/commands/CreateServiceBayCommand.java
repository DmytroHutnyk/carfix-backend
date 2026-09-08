package com.hutnyk.carfix.in.serviceBay.commands;

public record CreateServiceBayCommand(String name, Integer serviceBayTypeId, String notes) {
}
