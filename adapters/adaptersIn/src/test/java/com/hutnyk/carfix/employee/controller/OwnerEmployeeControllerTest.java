package com.hutnyk.carfix.employee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.exception.EmployeeNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.employee.OwnerEmployeePortIn;
import com.hutnyk.carfix.in.employee.commands.CreateEmployeeCommand;
import com.hutnyk.carfix.in.employee.commands.UpdateEmployeeCommand;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
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

public class OwnerEmployeeControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID EMPLOYEE_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001");

    private static OwnerEmployeeView sampleView() {
        return new OwnerEmployeeView(EMPLOYEE_ID, "Anna", "Nowak", "+48700200300", "anna@carfix.dev",
                new BigDecimal("6200.50"), EmployeeStatus.ACTIVE, "Wolska", "3", "Masovian", "Poland", "01-001",
                List.of("Mechanic"));
    }

    private static final class StubPortIn implements OwnerEmployeePortIn {
        String receivedEmail;
        UUID receivedBranchId;
        UUID receivedEmployeeId;
        CreateEmployeeCommand receivedCreate;
        UpdateEmployeeCommand receivedUpdate;
        List<OwnerEmployeeView> list = List.of(sampleView());
        OwnerEmployeeView single = sampleView();
        RuntimeException toThrow;

        @Override
        public List<OwnerEmployeeView> getEmployees(String ownerEmail, UUID branchId) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return list;
        }

        @Override
        public OwnerEmployeeView createEmployee(String ownerEmail, UUID branchId, CreateEmployeeCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedCreate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }

        @Override
        public OwnerEmployeeView updateEmployee(String ownerEmail, UUID branchId, UUID employeeId, UpdateEmployeeCommand command) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            this.receivedEmployeeId = employeeId;
            this.receivedUpdate = command;
            if (toThrow != null) throw toThrow;
            return single;
        }
    }

    private final StubPortIn stub = new StubPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerEmployeeController(stub))
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

    private static final String BODY = """
            {
              "name": "Anna",
              "surname": "Nowak",
              "phone": "+48700200300",
              "email": "anna@carfix.dev",
              "salary": 6200.50,
              "roles": ["Mechanic", "Welder"],
              "address": {"street": "Wolska", "apartment": "3", "region": "Masovian",
                          "country": "Poland", "postalCode": "01-001"}
            }
            """;

    @Test
    public void getReturnsTheWireShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/{branchId}/employees", BRANCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(EMPLOYEE_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("Anna"))
                .andExpect(jsonPath("$[0].surname").value("Nowak"))
                .andExpect(jsonPath("$[0].phone").value("+48700200300"))
                .andExpect(jsonPath("$[0].email").value("anna@carfix.dev"))
                .andExpect(jsonPath("$[0].salary").value(6200.50))
                .andExpect(jsonPath("$[0].roles[0]").value("Mechanic"))
                .andExpect(jsonPath("$[0].address.street").value("Wolska"))
                .andExpect(jsonPath("$[0].address.apartment").value("3"))
                .andExpect(jsonPath("$[0].address.region").value("Masovian"))
                .andExpect(jsonPath("$[0].address.country").value("Poland"))
                .andExpect(jsonPath("$[0].address.postalCode").value("01-001"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
    }

    @Test
    public void getNormalizesNullContactSalaryAndAddressToNonNullWireTypes() throws Exception {
        stub.list = List.of(new OwnerEmployeeView(EMPLOYEE_ID, "Oleh", "Savchuk", null, null, null,
                EmployeeStatus.ACTIVE, null, null, null, null, null, List.of()));

        mockMvc.perform(get("/api/owner/branches/{branchId}/employees", BRANCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].phone").value(""))
                .andExpect(jsonPath("$[0].email").value(""))
                .andExpect(jsonPath("$[0].salary").value(0))
                .andExpect(jsonPath("$[0].address.street").value(""))
                .andExpect(jsonPath("$[0].address.postalCode").value(""));
    }

    @Test
    public void postCreatesAndReturns201WithTheCommand() throws Exception {
        mockMvc.perform(post("/api/owner/branches/{branchId}/employees", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(EMPLOYEE_ID.toString()))
                .andExpect(jsonPath("$.name").value("Anna"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
        CreateEmployeeCommand c = stub.receivedCreate;
        assertThat(c.name()).isEqualTo("Anna");
        assertThat(c.phone()).isEqualTo("+48700200300");
        assertThat(c.salary()).isEqualByComparingTo("6200.50");
        assertThat(c.roleNames()).containsExactly("Mechanic", "Welder");
        assertThat(c.street()).isEqualTo("Wolska");
        assertThat(c.postalCode()).isEqualTo("01-001");
    }

    @Test
    public void putUpdatesAndReturns200WithTheCommandAndPathId() throws Exception {
        mockMvc.perform(put("/api/owner/branches/{branchId}/employees/{employeeId}", BRANCH_ID, EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EMPLOYEE_ID.toString()));

        assertThat(stub.receivedEmployeeId).isEqualTo(EMPLOYEE_ID);
        assertThat(stub.receivedUpdate.name()).isEqualTo("Anna");
        assertThat(stub.receivedUpdate.roleNames()).containsExactly("Mechanic", "Welder");
    }

    @Test
    public void postWithBlankNameAndBadEmailIs400WithFieldErrors() throws Exception {
        String body = """
                {
                  "name": "  ",
                  "surname": "Nowak",
                  "phone": "+48700200300",
                  "email": "nope",
                  "salary": 6200.50,
                  "roles": ["Mechanic"],
                  "address": {}
                }
                """;

        mockMvc.perform(post("/api/owner/branches/{branchId}/employees", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        assertThat(stub.receivedCreate).isNull();
    }

    @Test
    public void postToForeignOrUnknownBranchIs404BranchNotFound() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(post("/api/owner/branches/{branchId}/employees", BRANCH_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
    }

    @Test
    public void putOfUnknownEmployeeIs404EmployeeNotFound() throws Exception {
        stub.toThrow = new EmployeeNotFoundException(EMPLOYEE_ID);

        mockMvc.perform(put("/api/owner/branches/{branchId}/employees/{employeeId}", BRANCH_ID, EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
    }
}
