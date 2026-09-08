package com.hutnyk.carfix.in.serviceBay.commands;

public record UpdateServiceBayCommand(String name, Integer serviceBayTypeId, String notes) {
}
