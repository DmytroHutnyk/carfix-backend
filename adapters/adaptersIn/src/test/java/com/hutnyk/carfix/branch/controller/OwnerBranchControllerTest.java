package com.hutnyk.carfix.branch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.query.BranchBrandView;
import com.hutnyk.carfix.in.branch.query.BranchOpeningHoursView;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchOpeningHoursExceptionView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class OwnerBranchControllerTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final BranchId CREATED_BRANCH_ID = BranchId.genId();
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID REVIEW_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

    private static OwnerBranchSummaryView summary() {
        return new OwnerBranchSummaryView(
                BRANCH_ID, "AutoSerwis Kowalski Mokotow", BranchStatus.ACTIVE,
                "Pulawska", "145", "Warsaw",
                new BigDecimal("4.7"), 3,
                true, 12, 7, 3, 5,
                List.of(new BranchReviewView(
                        REVIEW_ID, 5, "Great", Instant.parse("2026-08-10T10:00:00Z"), "Anna", "Nowak")));
    }

    private static OwnerBranchDetailView detail() {
        return new OwnerBranchDetailView(
                BRANCH_ID, "AutoSerwis Kowalski Mokotow", BranchStatus.ACTIVE,
                "A workshop.", CancellationPolicy.MODERATE,
                "+48221234567", "kontakt@autofix.pl", "Europe/Warsaw",
                new AddressView(55, "Pulawska", "145", null, "02-515", "Warsaw", "Masovian Voivodeship",
                        CountryIso.PL, "Poland",
                        new BigDecimal("52.180000"), new BigDecimal("21.020000"), "ChIJx"),
                List.of(new BranchBrandView(7, "BMW")),
                List.of(new BranchOpeningHoursView(
                        DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), OpeningHoursMode.OPEN)),
                List.of(new OwnerBranchOpeningHoursExceptionView(
                        3, LocalDate.of(2026, 12, 24), null, null, false, "Christmas Eve")));
    }

    private static final class StubOwnerBranchPortIn implements OwnerBranchPortIn {
        String receivedEmail;
        RegisterBranchCommand received;
        List<OwnerBranchSummaryView> toReturn = List.of(summary());
        RuntimeException toThrow;
        UUID receivedBranchId;
        OwnerBranchDetailView detail = detail();

        @Override
        public List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail) {
            this.receivedEmail = ownerEmail;
            if (toThrow != null) throw toThrow;
            return toReturn;
        }

        @Override
        public OwnerBranchDetailView getMyBranch(String ownerEmail, UUID branchId) {
            this.receivedEmail = ownerEmail;
            this.receivedBranchId = branchId;
            if (toThrow != null) throw toThrow;
            return detail;
        }

        @Override
        public Branch registerBranch(String ownerEmail, RegisterBranchCommand command) {
            this.receivedEmail = ownerEmail;
            this.received = command;
            return Branch.create(CREATED_BRANCH_ID, command.name(), command.phoneNumber(), command.email(),
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
        UserDetails principal = User.withUsername(EMAIL).password("irrelevant").roles("OWNER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    public void clearContext() {
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
                .andExpect(jsonPath("$.id").value(CREATED_BRANCH_ID.id().toString()))
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

    @Test
    public void test_null_list_element_is_400_not_500() throws Exception {
        //given
        String body = FULL_BODY.replace("\"equipmentRequirements\": [{\"name\": \"Lift\", \"types\": [\"2-post lift\"]}]",
                "\"equipmentRequirements\": [null]");

        //when + then
        mockMvc.perform(post("/api/owner/branches").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors['services[0].equipmentRequirements[0]']").exists());
        assertThat(stub.received).isNull();
    }

    @Test
    public void getMyBranchesReturnsSummaryShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchId").value(BRANCH_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("AutoSerwis Kowalski Mokotow"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].streetName").value("Pulawska"))
                .andExpect(jsonPath("$[0].buildingNumber").value("145"))
                .andExpect(jsonPath("$[0].city").value("Warsaw"))
                .andExpect(jsonPath("$[0].rating").value(4.7))
                .andExpect(jsonPath("$[0].reviewCount").value(3))
                .andExpect(jsonPath("$[0].openNow").value(true))
                .andExpect(jsonPath("$[0].bookingsToday").value(12))
                .andExpect(jsonPath("$[0].completedToday").value(7))
                .andExpect(jsonPath("$[0].employeesOnDutyToday").value(3))
                .andExpect(jsonPath("$[0].employeesTotal").value(5))
                .andExpect(jsonPath("$[0].latestReviews[0].reviewId").value(REVIEW_ID.toString()))
                .andExpect(jsonPath("$[0].latestReviews[0].starsNumber").value(5))
                .andExpect(jsonPath("$[0].latestReviews[0].contents").value("Great"))
                .andExpect(jsonPath("$[0].latestReviews[0].createdAt").value("2026-08-10T10:00:00Z"))
                .andExpect(jsonPath("$[0].latestReviews[0].customerName").value("Anna"))
                .andExpect(jsonPath("$[0].latestReviews[0].customerSurname").value("Nowak"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
    }

    @Test
    public void getMyBranchesReturnsEmptyArrayForOwnerWithoutBranches() throws Exception {
        stub.toReturn = List.of();

        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getMyBranchesMissingPrincipalRowIs401WithCode() throws Exception {
        stub.toThrow = AuthenticatedUserMissingException.forEmail(EMAIL);

        mockMvc.perform(get("/api/owner/branches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATED_USER_MISSING"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    public void getMyBranchReturnsTheOverviewShape() throws Exception {
        mockMvc.perform(get("/api/owner/branches/" + BRANCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(BRANCH_ID.toString()))
                .andExpect(jsonPath("$.name").value("AutoSerwis Kowalski Mokotow"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.description").value("A workshop."))
                .andExpect(jsonPath("$.cancellationPolicy").value("MODERATE"))
                .andExpect(jsonPath("$.phoneNumber").value("+48221234567"))
                .andExpect(jsonPath("$.timezone").value("Europe/Warsaw"))
                .andExpect(jsonPath("$.address.streetName").value("Pulawska"))
                .andExpect(jsonPath("$.address.countryIso").value("PL"))
                .andExpect(jsonPath("$.address.countryName").value("Poland"))
                .andExpect(jsonPath("$.brands[0].carBrandId").value(7))
                .andExpect(jsonPath("$.openingHours[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.openingHours[0].startTime").value("09:00"))
                .andExpect(jsonPath("$.openingHoursExceptions[0].id").value(3))
                .andExpect(jsonPath("$.openingHoursExceptions[0].date").value("2026-12-24"))
                .andExpect(jsonPath("$.openingHoursExceptions[0].isOpen").value(false))
                .andExpect(jsonPath("$.openingHoursExceptions[0].reason").value("Christmas Eve"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedBranchId).isEqualTo(BRANCH_ID);
    }

    @Test
    public void getMyBranchOfAnotherOwnerIs404WithCode() throws Exception {
        stub.toThrow = new BranchNotFoundException(BRANCH_ID);

        mockMvc.perform(get("/api/owner/branches/" + BRANCH_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
