package com.hutnyk.carfix.serviceBay.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.serviceBay.OwnerServiceBayPortIn;
import com.hutnyk.carfix.in.serviceBay.commands.CreateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.commands.UpdateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.serviceBay.exception.ServiceBayNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

public class OwnerServiceBayControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final int BAY_ID = 10;

    private static OwnerServiceBayView view() {
        return new OwnerServiceBayView(BAY_ID, "Bay 1", 5, "Basic", "handles vans", ServiceBayStatus.ACTIVE);
    }

    private static final class StubPortIn implements OwnerServiceBayPortIn {
        String receivedEmail;
        UUID receivedBranchId;
        Integer receivedBayId;
        CreateServiceBayCommand receivedCreate;
        UpdateServiceBayCommand receivedUpdate;
        RuntimeException toThrow;
        List<OwnerServiceBayView> bays = List.of(view());
        List<ServiceBayTypeView> types = List.of(new ServiceBayTypeView(5, "Basic"), new ServiceBayTypeView(6, "With lift"));
        OwnerServiceBayView single = view();

        @Override
        public List<OwnerServiceBayView> getServiceBays(String ownerEmail, UUID branchId) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return bays;
        }

        @Override
        public List<ServiceBayTypeView> getServiceBayTypes(String ownerEmail, UUID branchId) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return types;
        }

        @Override
        public OwnerServiceBayView createServiceBay(String ownerEmail, UUID branchId, CreateServiceBayCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedCreate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }

        @Override
        public OwnerServiceBayView updateServiceBay(
                String ownerEmail, UUID branchId, Integer bayId, UpdateServiceBayCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedBayId = bayId;
            this.receivedUpdate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }
    }

    private final StubPortIn stub = new StubPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerServiceBayController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @BeforeEach
    public void authenticate() {
        UserDetails principal = User.withUsername(EMAIL).password("irrelevant").roles("OWNER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getReturnsServiceBayShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/" + BRANCH_ID + "/service-bays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(BAY_ID))
                .andExpect(jsonPath("$[0].name").value("Bay 1"))
                .andExpect(jsonPath("$[0].typeId").value(5))
                .andExpect(jsonPath("$[0].type").value("Basic"))
                .andExpect(jsonPath("$[0].notes").value("handles vans"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
    }

    @Test
    public void getTypesReturnsTypeShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/" + BRANCH_ID + "/service-bays/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Basic"))
                .andExpect(jsonPath("$[1].name").value("With lift"));
    }

    @Test
    public void postCreatesAndReturns201WithView() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/service-bays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bay 1\",\"serviceBayType\":\"Basic\",\"notes\":\"handles vans\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BAY_ID))
                .andExpect(jsonPath("$.type").value("Basic"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedCreate.name()).isEqualTo("Bay 1");
        assertThat(stub.receivedCreate.serviceBayType()).isEqualTo("Basic");
        assertThat(stub.receivedCreate.notes()).isEqualTo("handles vans");
    }

    @Test
    public void postWithBlankNameAndMissingTypeIs400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/service-bays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"notes\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.serviceBayType").exists());
        assertThat(stub.receivedCreate).isNull();
    }

    @Test
    public void postOnForeignOrUnknownBranchIs404() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/service-bays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bay 1\",\"serviceBayType\":\"Basic\",\"notes\":null}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void putUpdatesAndReturns200() throws Exception {
        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/service-bays/" + BAY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bay renamed\",\"serviceBayType\":\"With lift\",\"notes\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BAY_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(stub.receivedBayId).isEqualTo(BAY_ID);
        assertThat(stub.receivedUpdate.name()).isEqualTo("Bay renamed");
        assertThat(stub.receivedUpdate.serviceBayType()).isEqualTo("With lift");
        assertThat(stub.receivedUpdate.notes()).isNull();
    }

    @Test
    public void putOfUnknownBayIs404() throws Exception {
        stub.toThrow = new ServiceBayNotFoundException(BAY_ID);

        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/service-bays/" + BAY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bay renamed\",\"serviceBayType\":\"With lift\",\"notes\":null}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SERVICE_BAY_NOT_FOUND"));
    }

    @Test
    public void putWithBlankNameIs400WithFieldErrors() throws Exception {
        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/service-bays/" + BAY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.serviceBayType").exists());
        assertThat(stub.receivedUpdate).isNull();
    }
}
