package com.hutnyk.carfix.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.equipment.exception.EquipmentTypeNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.service.OwnerServicePortIn;
import com.hutnyk.carfix.in.service.commands.CreateServiceCommand;
import com.hutnyk.carfix.in.service.commands.UpdateServiceCommand;
import com.hutnyk.carfix.in.service.query.EmployeeRequirementView;
import com.hutnyk.carfix.in.service.query.EquipmentRequirementView;
import com.hutnyk.carfix.in.service.query.OwnerServiceView;
import com.hutnyk.carfix.role.exception.RoleNotFoundException;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.service.exception.ServiceInUseException;
import com.hutnyk.carfix.service.exception.ServiceNotFoundException;
import com.hutnyk.carfix.serviceBay.exception.ServiceBayTypeNotFoundException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OwnerServiceControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final int SERVICE_ID = 42;

    private static final String VALID_BODY = """
            {"name":"Oil Change","description":"notes","durationMinutes":30,"price":49.99,"categoryId":3,
             "bayTypes":["General"],
             "employeeRequirements":[{"name":"Mechanic","roles":["Mechanic"]}],
             "equipmentRequirements":[{"name":"Oil Drain","types":["Oil Drain"]}]}""";

    private static OwnerServiceView view(ServiceStatus status) {
        return new OwnerServiceView(
                SERVICE_ID, "Oil Change", "notes", (short) 30, new BigDecimal("49.99"), status,
                3, "Maintenance", List.of("General"),
                List.of(new EmployeeRequirementView("Mechanic", List.of("Mechanic"))),
                List.of(new EquipmentRequirementView("Oil Drain", List.of("Oil Drain"))));
    }

    private static final class StubPortIn implements OwnerServicePortIn {
        String receivedEmail;
        UUID receivedBranchId;
        Integer receivedServiceId;
        CreateServiceCommand receivedCreate;
        UpdateServiceCommand receivedUpdate;
        boolean deleteCalled;
        RuntimeException toThrow;
        OwnerServiceView single = view(ServiceStatus.ACTIVE);

        @Override
        public List<OwnerServiceView> getServices(String ownerEmail, UUID branchId) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return List.of(view(ServiceStatus.ACTIVE));
        }

        @Override
        public OwnerServiceView createService(String ownerEmail, UUID branchId, CreateServiceCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedCreate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }

        @Override
        public OwnerServiceView updateService(
                String ownerEmail, UUID branchId, Integer serviceId, UpdateServiceCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedServiceId = serviceId;
            this.receivedUpdate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }

        @Override
        public OwnerServiceView activateService(String ownerEmail, UUID branchId, Integer serviceId) {
            this.receivedServiceId = serviceId;
            if (toThrow != null) throw toThrow;
            return view(ServiceStatus.ACTIVE);
        }

        @Override
        public OwnerServiceView suspendService(String ownerEmail, UUID branchId, Integer serviceId) {
            this.receivedServiceId = serviceId;
            if (toThrow != null) throw toThrow;
            return view(ServiceStatus.SUSPENDED);
        }

        @Override
        public void deleteService(String ownerEmail, UUID branchId, Integer serviceId) {
            this.receivedServiceId = serviceId;
            this.deleteCalled = true;
            if (toThrow != null) throw toThrow;
        }
    }

    private final StubPortIn stub = new StubPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerServiceController(stub))
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
    public void getReturnsNameBasedShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/" + BRANCH_ID + "/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SERVICE_ID))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].categoryName").value("Maintenance"))
                .andExpect(jsonPath("$[0].bayTypes[0]").value("General"))
                .andExpect(jsonPath("$[0].employeeRequirements[0].roles[0]").value("Mechanic"))
                .andExpect(jsonPath("$[0].equipmentRequirements[0].types[0]").value("Oil Drain"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
    }

    @Test
    public void postCreatesAndReturns201WithNameBasedCommand() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SERVICE_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.bayTypes[0]").value("General"));

        assertThat(stub.receivedCreate.name()).isEqualTo("Oil Change");
        assertThat(stub.receivedCreate.categoryId()).isEqualTo(3);
        assertThat(stub.receivedCreate.bayTypes()).containsExactly("General");
        assertThat(stub.receivedCreate.employeeRequirements().get(0).roles()).containsExactly("Mechanic");
        assertThat(stub.receivedCreate.equipmentRequirements().get(0).types()).containsExactly("Oil Drain");
    }

    @Test
    public void postWithBlankNameMissingDurationEmptyBayTypesIs400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  ","price":49.99,"categoryId":3,"bayTypes":[],
                                 "employeeRequirements":[{"name":"Mechanic","roles":["Mechanic"]}],
                                 "equipmentRequirements":[]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.durationMinutes").exists())
                .andExpect(jsonPath("$.errors.bayTypes").exists());
        assertThat(stub.receivedCreate).isNull();
    }

    @Test
    public void postOnForeignOrUnknownBranchIs404() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
    }

    @Test
    public void postWithUnknownCategoryIs404() throws Exception {
        stub.toThrow = new ServiceCategoryNotFoundException(3);

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SERVICE_CATEGORY_NOT_FOUND"));
    }

    @Test
    public void postWithUnknownBayTypeIs404() throws Exception {
        stub.toThrow = new ServiceBayTypeNotFoundException("General");

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SERVICE_BAY_TYPE_NOT_FOUND"));
    }

    @Test
    public void postWithUnknownRoleIs404() throws Exception {
        stub.toThrow = new RoleNotFoundException("Mechanic");

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_FOUND"));
    }

    @Test
    public void postWithUnknownEquipmentTypeIs404() throws Exception {
        stub.toThrow = new EquipmentTypeNotFoundException("Oil Drain");

        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EQUIPMENT_TYPE_NOT_FOUND"));
    }

    @Test
    public void putUpdatesAndReturns200() throws Exception {
        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SERVICE_ID));

        assertThat(stub.receivedServiceId).isEqualTo(SERVICE_ID);
        assertThat(stub.receivedUpdate.name()).isEqualTo("Oil Change");
        assertThat(stub.receivedUpdate.bayTypes()).containsExactly("General");
    }

    @Test
    public void putOfUnknownServiceIs404() throws Exception {
        stub.toThrow = new ServiceNotFoundException(SERVICE_ID);

        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SERVICE_NOT_FOUND"));
    }

    @Test
    public void putWithBlankNameIs400WithFieldErrors() throws Exception {
        mockMvc.perform(put("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  ","durationMinutes":30,"price":49.99,"categoryId":3,"bayTypes":["General"],
                                 "employeeRequirements":[{"name":"Mechanic","roles":["Mechanic"]}],
                                 "equipmentRequirements":[]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists());
        assertThat(stub.receivedUpdate).isNull();
    }

    @Test
    public void activateReturns200Active() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        assertThat(stub.receivedServiceId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void suspendReturns200Suspended() throws Exception {
        mockMvc.perform(post("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID + "/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
        assertThat(stub.receivedServiceId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID))
                .andExpect(status().isNoContent());
        assertThat(stub.deleteCalled).isTrue();
        assertThat(stub.receivedServiceId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void deleteOfServiceInUseIs409() throws Exception {
        stub.toThrow = new ServiceInUseException(SERVICE_ID);

        mockMvc.perform(delete("/api/owner/branches/" + BRANCH_ID + "/services/" + SERVICE_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SERVICE_IN_USE"));
    }
}
