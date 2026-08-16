package com.hutnyk.carfix.branch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.user.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalTime;

public class OwnerBranchControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final BranchId BRANCH_ID = BranchId.genId();

    private static final class StubOwnerBranchPortIn implements OwnerBranchPortIn {
        String receivedEmail;
        RegisterBranchCommand received;

        @Override
        public Branch registerBranch(String ownerEmail, RegisterBranchCommand command) {
            this.receivedEmail = ownerEmail;
            this.received = command;
            return Branch.create(BRANCH_ID, command.name(), command.phoneNumber(), command.email(),
                    command.timezone(), 55, UserId.genId());
        }
    }

    private final StubOwnerBranchPortIn stub = new StubOwnerBranchPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OwnerBranchController(stub))
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
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL).password("x").roles("OWNER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities()));
    }

    @AfterEach
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private static final String FULL_BODY = """
            {
              "name": "AutoFix — Nowogrodzka",
              "phoneNumber": "+48221234567",
              "email": "kontakt@autofix.pl",
              "timezone": "Europe/Warsaw",
              "address": {"streetName": "Nowogrodzka", "buildingNumber": "10", "flatNumber": null, "postalCode": "00-511",
                          "city": "Warsaw", "region": "Masovian Voivodeship", "countryIso": "PL",
                          "latitude": 52.2297, "longitude": 21.0122, "googlePlaceId": "ChIJx"},
              "openingHours": [{"dayOfWeek": "MONDAY", "opensAt": "09:00", "closesAt": "17:00", "mode": "OPEN"},
                               {"dayOfWeek": "SATURDAY", "opensAt": "09:00", "closesAt": "14:00", "mode": "BY_APPOINTMENT"}],
              "carBrandIds": [1, 2],
              "serviceBayTypes": ["Basic", "With lift"],
              "serviceBays": [{"name": "Bay 1", "type": "Basic"}],
              "equipmentTypes": ["2-post lift"],
              "equipment": [{"name": "2-post lift #1", "type": "2-post lift"}],
              "roles": ["Mechanic", "EV high-voltage"],
              "employees": [{"firstName": "Oleh", "lastName": "Savchuk", "roles": ["EV high-voltage"]}],
              "services": [{"name": "Oil & filter change", "description": null, "durationMinutes": 30, "price": 150.00,
                            "categoryId": 2, "status": "ACTIVE", "bayTypes": ["Basic"],
                            "employeeRequirements": [{"name": "Mechanic", "roles": ["Mechanic", "EV high-voltage"]}],
                            "equipmentRequirements": [{"name": "Lift", "types": ["2-post lift"]}]}]
            }
            """;

    @Test
    public void test_post_returns_201_with_id_name_and_status_and_maps_every_nested_field() throws Exception {
        //when + then
        mockMvc.perform(post("/api/owner/branches").contentType(MediaType.APPLICATION_JSON).content(FULL_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BRANCH_ID.id().toString()))
                .andExpect(jsonPath("$.name").value("AutoFix — Nowogrodzka"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        RegisterBranchCommand c = stub.received;
        assertThat(c.timezone()).isEqualTo("Europe/Warsaw");
        assertThat(c.address().countryIso()).isEqualTo("PL");
        assertThat(c.address().latitude()).isEqualByComparingTo("52.2297");
        assertThat(c.openingHours()).hasSize(2);
        assertThat(c.openingHours().get(1).dayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(c.openingHours().get(1).opensAt()).isEqualTo(LocalTime.of(9, 0));
        assertThat(c.openingHours().get(1).mode()).isEqualTo(OpeningHoursMode.BY_APPOINTMENT);
        assertThat(c.carBrandIds()).containsExactlyInAnyOrder(1, 2);
        assertThat(c.serviceBayTypes()).containsExactly("Basic", "With lift");
        assertThat(c.serviceBays().get(0).type()).isEqualTo("Basic");
        assertThat(c.equipment().get(0).name()).isEqualTo("2-post lift #1");
        assertThat(c.employees().get(0).roles()).containsExactly("EV high-voltage");
        assertThat(c.services()).singleElement().satisfies(s -> {
            assertThat(s.durationMinutes()).isEqualTo((short) 30);
            assertThat(s.price()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(s.categoryId()).isEqualTo(2);
            assertThat(s.status()).isEqualTo(ServiceStatus.ACTIVE);
            assertThat(s.bayTypes()).containsExactly("Basic");
            assertThat(s.employeeRequirements().get(0).roles()).containsExactly("Mechanic", "EV high-voltage");
            assertThat(s.equipmentRequirements().get(0).types()).containsExactly("2-post lift");
        });
    }

    @Test
    public void test_missing_top_level_fields_are_400_validation_failed_with_field_errors() throws Exception {
        //when + then
        mockMvc.perform(post("/api/owner/branches").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"phoneNumber\":\"221234567\",\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.timezone").exists())
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.serviceBays").exists());
        assertThat(stub.received).isNull();
    }

    @Test
    public void test_nested_list_violations_are_reported_with_indexed_paths() throws Exception {
        //given — a service without bay types and an employee without roles
        String body = FULL_BODY
                .replace("\"bayTypes\": [\"Basic\"]", "\"bayTypes\": []")
                .replace("\"roles\": [\"EV high-voltage\"]}", "\"roles\": []}");

        //when + then
        mockMvc.perform(post("/api/owner/branches").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['services[0].bayTypes']").exists())
                .andExpect(jsonPath("$.errors['employees[0].roles']").exists());
        assertThat(stub.received).isNull();
    }

    @Test
    public void test_malformed_time_is_400_malformed_request() throws Exception {
        //when + then
        mockMvc.perform(post("/api/owner/branches").contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_BODY.replace("\"opensAt\": \"09:00\", \"closesAt\": \"17:00\"", "\"opensAt\": \"9am\", \"closesAt\": \"17:00\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
