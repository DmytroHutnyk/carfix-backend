package com.hutnyk.carfix.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ServiceCategoryServiceTest {

    private static final class StubServiceCategoryPortOut implements ServiceCategoryPortOut {
        int calls;

        @Override
        public List<ServiceCategory> findAll() {
            calls++;
            return List.of(ServiceCategory.of(1, "Brakes"), ServiceCategory.of(2, "Engine"));
        }
    }

    @Test
    public void test_getAllCategories_passes_the_port_result_through() {
        StubServiceCategoryPortOut port = new StubServiceCategoryPortOut();

        List<ServiceCategory> result = new ServiceCategoryService(port).getAllCategories();

        assertThat(result).extracting(ServiceCategory::getName).containsExactly("Brakes", "Engine");
        assertThat(port.calls).isEqualTo(1);
    }
}
