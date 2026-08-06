package com.hutnyk.carfix.carCatalog.dto.response;

public record ModelVersionResponse(Integer id, String name, Short startProduction, Short endProduction, Integer modelId) {}
