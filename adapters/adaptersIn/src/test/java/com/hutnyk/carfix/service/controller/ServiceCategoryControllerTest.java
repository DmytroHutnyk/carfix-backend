package com.hutnyk.carfix.service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.service.ServiceCategoryPortIn;
import com.hutnyk.carfix.service.ServiceCategory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

public class ServiceCategoryControllerTest {

    private static final class StubServiceCategoryPortIn implements ServiceCategoryPortIn {
        @Override
        public List<ServiceCategory> getAllCategories() {
            return List.of(ServiceCategory.of(4, "Brakes"), ServiceCategory.of(2, "Maintenance"));
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ServiceCategoryController(new StubServiceCategoryPortIn()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    public void test_get_lists_categories_in_port_order() throws Exception {
        //when + then
        mockMvc.perform(get("/api/service-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].name").value("Brakes"))
                .andExpect(jsonPath("$[1].name").value("Maintenance"));
    }
}
