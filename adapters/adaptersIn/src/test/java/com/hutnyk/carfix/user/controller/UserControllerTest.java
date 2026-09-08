package com.hutnyk.carfix.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.address.AddressPortIn;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.address.query.LocationView;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserResponseAssembler;
import com.hutnyk.carfix.user.UserRole;
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
import java.time.LocalDate;
import java.util.Optional;

public class UserControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final AddressView WARSAW_ADDRESS = new AddressView(7, "Marszałkowska", "10", "3A", "00-001",
            "Warsaw", "Masovian Voivodeship", CountryIso.PL, "Poland",
            new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJ_place");
    private static final LocationView WARSAW_CITY = new LocationView(11, "Warsaw", "Masovian Voivodeship",
            CountryIso.PL, new BigDecimal("52.229700"), new BigDecimal("21.012200"));
    private static final String CORE_BODY =
            "{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":\"1990-05-01\",\"preferredLocation\":null}";
    private static final String ADDRESS_BODY = "{\"streetName\":\"Marszałkowska\",\"buildingNumber\":\"10\","
            + "\"flatNumber\":\"3A\",\"postalCode\":\"00-001\",\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\","
            + "\"countryIso\":\"PL\",\"latitude\":52.2297,\"longitude\":21.0122,\"googlePlaceId\":\"ChIJ_place\"}";

    private static final class StubUserPortIn implements UserPortIn {
        UpdateUserCommand received;
        UpdateUserAddressCommand receivedAddress;
        String receivedEmail;
        String deletedFor;
        String deletedAccountFor;
        Integer addressIdOfUser;
        Integer preferredCityIdOfUser = 11;
        int calls;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User updateUser(String email, UpdateUserCommand command) {
            this.receivedEmail = email;
            this.received = command;
            this.calls++;
            // Builder tolerates new optional fields; new required fields still fail loudly.
            return User.builder()
                    .id(UserId.genId())
                    .name(command.name())
                    .surname(command.surname())
                    .phoneNumber(new PhoneNumber("+48", "123456789"))
                    .email(email)
                    .role(UserRole.CUSTOMER)
                    .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                    .dateOfBirth(command.dateOfBirth())
                    .addressId(addressIdOfUser)
                    .preferredCityId(command.preferredLocation() == null ? null : preferredCityIdOfUser)
                    .build();
        }

        @Override
        public AddressView updateAddress(String email, UpdateUserAddressCommand command) {
            this.receivedEmail = email;
            this.receivedAddress = command;
            this.calls++;
            return WARSAW_ADDRESS;
        }

        @Override
        public void deleteAddress(String email) {
            this.deletedFor = email;
            this.calls++;
        }

        @Override
        public void deleteAccount(String email) {
            this.deletedAccountFor = email;
            this.calls++;
        }

        @Override
        public void requestEmailVerification(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User verifyEmail(String email, String code) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubAddressPortIn implements AddressPortIn {
        @Override
        public Optional<AddressView> loadAddressView(Integer addressId) {
            return addressId == 7 ? Optional.of(WARSAW_ADDRESS) : Optional.empty();
        }

        @Override
        public Optional<LocationView> loadCityView(Integer cityId) {
            return cityId == 11 ? Optional.of(WARSAW_CITY) : Optional.empty();
        }
    }

    private final StubUserPortIn stub = new StubUserPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(stub, new UserResponseAssembler(new StubAddressPortIn())))
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
                .withUsername(EMAIL).password("x").roles("CUSTOMER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities()));
    }

    @AfterEach
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }


    @Test
    public void test_put_me_returns_200_with_the_updated_core() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.phoneCountryCode").value("+48"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-01"))
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.preferredLocation").doesNotExist())
                .andExpect(jsonPath("$.emailVerifiedAt").doesNotExist());
    }

    @Test
    public void test_put_me_passes_the_principal_email_and_command_fields_verbatim() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORE_BODY))
                .andExpect(status().isOk());

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.received.name()).isEqualTo("John");
        assertThat(stub.received.surname()).isEqualTo("Doe");
        assertThat(stub.received.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(stub.received.preferredLocation()).isNull();
    }

    @Test
    public void test_put_me_null_dateOfBirth_reaches_the_port_as_null() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfBirth").doesNotExist());

        assertThat(stub.received.dateOfBirth()).isNull();
    }

    @Test
    public void test_put_me_with_preferred_location_echoes_it_and_passes_it_to_the_port() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null,"
                                + "\"preferredLocation\":{\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\","
                                + "\"countryIso\":\"PL\",\"latitude\":52.2297,\"longitude\":21.0122}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredLocation.city").value("Warsaw"))
                .andExpect(jsonPath("$.preferredLocation.region").value("Masovian Voivodeship"))
                .andExpect(jsonPath("$.preferredLocation.countryIso").value("PL"))
                .andExpect(jsonPath("$.preferredLocation.latitude").value(52.2297))
                .andExpect(jsonPath("$.preferredLocation.longitude").value(21.0122));

        assertThat(stub.received.preferredLocation().city()).isEqualTo("Warsaw");
        assertThat(stub.received.preferredLocation().countryIso()).isEqualTo("PL");
        assertThat(stub.received.preferredLocation().latitude()).isEqualByComparingTo("52.2297");
    }

    @Test
    public void test_put_me_returns_the_assembled_address_when_the_user_has_one() throws Exception {
        stub.addressIdOfUser = 7;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.id").value(7))
                .andExpect(jsonPath("$.address.streetName").value("Marszałkowska"))
                .andExpect(jsonPath("$.address.buildingNumber").value("10"))
                .andExpect(jsonPath("$.address.flatNumber").value("3A"))
                .andExpect(jsonPath("$.address.postalCode").value("00-001"))
                .andExpect(jsonPath("$.address.city").value("Warsaw"))
                .andExpect(jsonPath("$.address.region").value("Masovian Voivodeship"))
                .andExpect(jsonPath("$.address.countryIso").value("PL"))
                .andExpect(jsonPath("$.address.countryName").value("Poland"))
                .andExpect(jsonPath("$.address.googlePlaceId").value("ChIJ_place"));
    }

    @Test
    public void test_put_me_with_a_dangling_address_id_is_a_500() throws Exception {
        stub.addressIdOfUser = 999;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORE_BODY))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void test_put_me_with_a_dangling_preferred_city_id_is_a_500() throws Exception {
        stub.preferredCityIdOfUser = 999;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null,"
                                + "\"preferredLocation\":{\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\","
                                + "\"countryIso\":\"PL\",\"latitude\":null,\"longitude\":null}}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void test_put_me_bad_country_code_format_returns_400_with_the_dotted_field() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null,"
                                + "\"preferredLocation\":{\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\","
                                + "\"countryIso\":\"pol\","
                                + "\"latitude\":null,\"longitude\":null}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['preferredLocation.countryIso']").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_put_me_preferred_location_without_city_returns_400() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null,"
                                + "\"preferredLocation\":{\"city\":\" \",\"region\":\"Masovian Voivodeship\",\"countryIso\":\"PL\","
                                + "\"latitude\":null,\"longitude\":null}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['preferredLocation.city']").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_put_me_blank_name_returns_400_and_never_reaches_the_port() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_put_me_absent_surname_returns_400() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"dateOfBirth\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.surname").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_patch_me_is_no_longer_mapped() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    public void test_put_me_address_returns_200_with_the_assembled_address() throws Exception {
        mockMvc.perform(put("/api/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADDRESS_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.streetName").value("Marszałkowska"))
                .andExpect(jsonPath("$.buildingNumber").value("10"))
                .andExpect(jsonPath("$.flatNumber").value("3A"))
                .andExpect(jsonPath("$.postalCode").value("00-001"))
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andExpect(jsonPath("$.region").value("Masovian Voivodeship"))
                .andExpect(jsonPath("$.countryIso").value("PL"))
                .andExpect(jsonPath("$.countryName").value("Poland"))
                .andExpect(jsonPath("$.latitude").value(52.2297))
                .andExpect(jsonPath("$.longitude").value(21.0122))
                .andExpect(jsonPath("$.googlePlaceId").value("ChIJ_place"));

        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.receivedAddress.streetName()).isEqualTo("Marszałkowska");
        assertThat(stub.receivedAddress.buildingNumber()).isEqualTo("10");
        assertThat(stub.receivedAddress.flatNumber()).isEqualTo("3A");
        assertThat(stub.receivedAddress.postalCode()).isEqualTo("00-001");
        assertThat(stub.receivedAddress.city()).isEqualTo("Warsaw");
        assertThat(stub.receivedAddress.region()).isEqualTo("Masovian Voivodeship");
        assertThat(stub.receivedAddress.countryIso()).isEqualTo("PL");
        assertThat(stub.receivedAddress.latitude()).isEqualByComparingTo("52.2297");
        assertThat(stub.receivedAddress.googlePlaceId()).isEqualTo("ChIJ_place");
    }

    @Test
    public void test_put_me_address_null_optionals_reach_the_port_as_null() throws Exception {
        mockMvc.perform(put("/api/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"streetName\":\"Marszałkowska\",\"buildingNumber\":\"10\",\"flatNumber\":null,"
                                + "\"postalCode\":\"00-001\",\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\","
                                + "\"countryIso\":\"PL\",\"latitude\":null,\"longitude\":null,\"googlePlaceId\":null}"))
                .andExpect(status().isOk());

        assertThat(stub.receivedAddress.flatNumber()).isNull();
        assertThat(stub.receivedAddress.latitude()).isNull();
        assertThat(stub.receivedAddress.longitude()).isNull();
        assertThat(stub.receivedAddress.googlePlaceId()).isNull();
    }

    @Test
    public void test_put_me_address_missing_required_fields_returns_400_listing_each() throws Exception {
        mockMvc.perform(put("/api/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"streetName\":\" \",\"buildingNumber\":\"10\",\"postalCode\":\"\","
                                + "\"city\":\"Warsaw\",\"countryIso\":\"P\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.streetName").exists())
                .andExpect(jsonPath("$.errors.postalCode").exists())
                .andExpect(jsonPath("$.errors.region").exists())
                .andExpect(jsonPath("$.errors.countryIso").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_put_me_address_out_of_range_coordinates_return_400() throws Exception {
        mockMvc.perform(put("/api/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"streetName\":\"Marszałkowska\",\"buildingNumber\":\"10\",\"postalCode\":\"00-001\","
                                + "\"city\":\"Warsaw\",\"region\":\"Masovian Voivodeship\",\"countryIso\":\"PL\","
                                + "\"latitude\":91,\"longitude\":21}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.latitude").exists());

        assertThat(stub.calls).isZero();
    }


    @Test
    public void test_delete_me_address_returns_204_and_passes_the_principal() throws Exception {
        mockMvc.perform(delete("/api/users/me/address"))
                .andExpect(status().isNoContent());

        assertThat(stub.deletedFor).isEqualTo(EMAIL);
    }


    @Test
    public void test_delete_me_returns_204_and_deletes_the_account_of_the_principal() throws Exception {
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());

        assertThat(stub.deletedAccountFor).isEqualTo(EMAIL);
    }
}
