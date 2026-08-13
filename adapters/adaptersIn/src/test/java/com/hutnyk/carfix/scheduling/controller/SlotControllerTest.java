package com.hutnyk.carfix.scheduling.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.exception.CarFixException;
import com.hutnyk.carfix.in.scheduling.SlotPortIn;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;
import com.hutnyk.carfix.in.scheduling.query.DaySlotsView;
import com.hutnyk.carfix.in.scheduling.query.SlotView;
import com.hutnyk.carfix.scheduling.exception.InvalidSlotQueryException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SlotControllerTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static class StubSlotPortIn implements SlotPortIn {
        BranchSlotsQuery receivedQuery;
        CarFixException toThrow;

        @Override
        public BranchSlotsView getSlots(BranchSlotsQuery query) {
            this.receivedQuery = query;
            if (toThrow != null) {
                throw toThrow;
            }
            return new BranchSlotsView(true, List.of(
                    new DaySlotsView(LocalDate.of(2026, 8, 14), List.of(
                            new SlotView(LocalTime.of(9, 0), LocalTime.of(10, 45)))),
                    new DaySlotsView(LocalDate.of(2026, 8, 15), List.of())));
        }
    }

    private final StubSlotPortIn stub = new StubSlotPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SlotController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @Test
    public void test_getSlots_returns_full_shape_and_binds_params() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/slots")
                        .param("serviceIds", "3,7")
                        .param("from", "2026-08-14")
                        .param("to", "2026-08-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chainable").value(true))
                .andExpect(jsonPath("$.days[0].date").value("2026-08-14"))
                .andExpect(jsonPath("$.days[0].slots[0].startTime").value("09:00"))
                .andExpect(jsonPath("$.days[0].slots[0].endTime").value("10:45"))
                .andExpect(jsonPath("$.days[1].slots").isEmpty());

        assertThat(stub.receivedQuery.branchId()).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedQuery.serviceIds()).containsExactly(3, 7);
        assertThat(stub.receivedQuery.from()).isEqualTo(LocalDate.of(2026, 8, 14));
        assertThat(stub.receivedQuery.to()).isEqualTo(LocalDate.of(2026, 8, 15));
    }

    @Test
    public void test_invalid_query_maps_to_400_with_code() throws Exception {
        stub.toThrow = new InvalidSlotQueryException("from must not be after to");
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/slots")
                        .param("serviceIds", "3")
                        .param("from", "2026-08-15")
                        .param("to", "2026-08-14"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SLOT_QUERY"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    public void test_branch_not_found_maps_to_404() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/slots")
                        .param("serviceIds", "3")
                        .param("from", "2026-08-14")
                        .param("to", "2026-08-14"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
    }

    @Test
    public void test_missing_from_param_is_400() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/slots")
                        .param("serviceIds", "3")
                        .param("to", "2026-08-14"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_malformed_date_is_400() throws Exception {
        mockMvc.perform(get("/api/branches/" + BRANCH_ID + "/slots")
                        .param("serviceIds", "3")
                        .param("from", "14-08-2026")
                        .param("to", "2026-08-15"))
                .andExpect(status().isBadRequest());
    }
}
