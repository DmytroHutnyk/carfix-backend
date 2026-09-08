package com.hutnyk.carfix.equipment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.equipment.exception.EquipmentNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.equipment.OwnerEquipmentPortIn;
import com.hutnyk.carfix.in.equipment.commands.CreateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.commands.UpdateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
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

public class OwnerEquipmentControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");

    private static final class StubPortIn implements OwnerEquipmentPortIn {
        String receivedEmail;
        UUID receivedBranchId;
        Integer receivedEquipmentId;
        CreateEquipmentCommand receivedCreate;
        UpdateEquipmentCommand receivedUpdate;
        RuntimeException toThrow;
        List<OwnerEquipmentView> listReturn =
                List.of(new OwnerEquipmentView(5, "2-post lift #1", "2-post lift", null, EquipmentStatus.ACTIVE));
        OwnerEquipmentView createReturn =
                new OwnerEquipmentView(9, "Diagnostic scanner", "OBD scanner", "handheld", EquipmentStatus.ACTIVE);
        OwnerEquipmentView updateReturn =
                new OwnerEquipmentView(5, "new name", "Lift", "note", EquipmentStatus.SUSPENDED);

        @Override
        public List<OwnerEquipmentView> getEquipment(String email, UUID branchId) {
            this.receivedEmail = email;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return listReturn;
        }

        @Override
        public OwnerEquipmentView createEquipment(String email, UUID branchId, CreateEquipmentCommand command) {
            this.receivedEmail = email;
            this.receivedBranchId = branchId;
            this.receivedCreate = command;
            if (toThrow != null) throw toThrow;
            return createReturn;
        }

        @Override
        public OwnerEquipmentView updateEquipment(
                String email, UUID branchId, Integer equipmentId, UpdateEquipmentCommand command) {
            this.receivedEmail = email;
            this.receivedBranchId = branchId;
            this.receivedEquipmentId = equipmentId;
            this.receivedUpdate = command;
            if (toThrow != null) throw toThrow;
            return updateReturn;
        }
    }

    private final StubPortIn stub = new StubPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerEquipmentController(stub))
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
    public void test_get_returns_the_wire_shape_with_null_notes_as_empty_string() throws Exception {
        mockMvc.perform(get("/api/owner/branches/{branchId}/equipment", BRANCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("2-post lift #1"))
                .andExpect(jsonPath("$[0].type").value("2-post lift"))
                .andExpect(jsonPath("$[0].notes").value(""))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_post_returns_201_and_passes_the_command() throws Exception {
        mockMvc.perform(post("/api/owner/branches/{branchId}/equipment", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Diagnostic scanner\",\"type\":\"OBD scanner\",\"notes\":\"handheld\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name").value("Diagnostic scanner"))
                .andExpect(jsonPath("$.type").value("OBD scanner"))
                .andExpect(jsonPath("$.notes").value("handheld"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(stub.receivedCreate.name()).isEqualTo("Diagnostic scanner");
        assertThat(stub.receivedCreate.type()).isEqualTo("OBD scanner");
        assertThat(stub.receivedCreate.notes()).isEqualTo("handheld");
    }

    @Test
    public void test_put_returns_200_and_passes_the_id_and_command() throws Exception {
        mockMvc.perform(put("/api/owner/branches/{branchId}/equipment/{equipmentId}", BRANCH_ID, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new name\",\"type\":\"Lift\",\"notes\":\"note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        assertThat(stub.receivedEquipmentId).isEqualTo(5);
        assertThat(stub.receivedUpdate.name()).isEqualTo("new name");
        assertThat(stub.receivedUpdate.type()).isEqualTo("Lift");
        assertThat(stub.receivedUpdate.notes()).isEqualTo("note");
    }

    @Test
    public void test_blank_name_is_400_with_field_error() throws Exception {
        mockMvc.perform(post("/api/owner/branches/{branchId}/equipment", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"type\":\"Lift\",\"notes\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists());
        assertThat(stub.receivedCreate).isNull();
    }

    @Test
    public void test_blank_type_is_400_with_field_error() throws Exception {
        mockMvc.perform(post("/api/owner/branches/{branchId}/equipment", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Scanner\",\"type\":\"  \",\"notes\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.type").exists());
        assertThat(stub.receivedCreate).isNull();
    }

    @Test
    public void test_foreign_branch_is_404_branch_not_found() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(get("/api/owner/branches/{branchId}/equipment", BRANCH_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
    }

    @Test
    public void test_unknown_equipment_is_404_equipment_not_found() throws Exception {
        stub.toThrow = new EquipmentNotFoundException(99);

        mockMvc.perform(put("/api/owner/branches/{branchId}/equipment/{equipmentId}", BRANCH_ID, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"type\":\"Lift\",\"notes\":null}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EQUIPMENT_NOT_FOUND"));
    }
}
