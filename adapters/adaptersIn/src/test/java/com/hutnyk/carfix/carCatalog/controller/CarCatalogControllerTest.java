package com.hutnyk.carfix.carCatalog.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;
import com.hutnyk.carfix.in.carCatalog.CarCatalogPortIn;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

public class CarCatalogControllerTest {

    private static final class StubCarCatalogPortIn implements CarCatalogPortIn {

        @Override
        public List<CarBrand> getAllBrands() {
            return List.of(CarBrand.of(1, "Toyota"));
        }

        @Override
        public List<CarModel> getModelsByBrandId(Integer brandId) {
            return List.of(CarModel.of(10, "Camry", brandId));
        }

        @Override
        public List<ModelVersion> getVersionsByModelId(Integer modelId) {
            return List.of(ModelVersion.of(100, "XV70 2.5 Hybrid", (short) 2018, (short) 2024, modelId));
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CarCatalogController(new StubCarCatalogPortIn()))
            .build();

    @Test
    public void test_getBrands_returns_brand_json() throws Exception {
        mockMvc.perform(get("/api/car-catalog/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Toyota"));
    }

    @Test
    public void test_getModels_returns_model_json_for_brand() throws Exception {
        mockMvc.perform(get("/api/car-catalog/brands/7/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Camry"))
                .andExpect(jsonPath("$[0].brandId").value(7));
    }

    @Test
    public void test_getVersions_returns_version_json_for_model() throws Exception {
        mockMvc.perform(get("/api/car-catalog/models/10/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].name").value("XV70 2.5 Hybrid"))
                .andExpect(jsonPath("$[0].startProduction").value(2018))
                .andExpect(jsonPath("$[0].endProduction").value(2024))
                .andExpect(jsonPath("$[0].modelId").value(10));
    }
}
