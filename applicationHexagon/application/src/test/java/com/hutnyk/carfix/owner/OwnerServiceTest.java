package com.hutnyk.carfix.owner;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.out.owner.OwnerPortOut;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class OwnerServiceTest {

    private static Owner owner(String email) {
        User user = User.builder()
                .id(UserId.genId()).name("Marek").surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200")).email(email).role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue")).dateOfBirth(null).addressId(null)
                .build();
        return Owner.of(user, "AutoSerwis Kowalski", "5252445567", "146892132");
    }

    private static final class StubOwnerPortOut implements OwnerPortOut {
        Owner owner;
        String receivedEmail;

        @Override
        public Optional<Owner> loadOwnerByUsername(String email) {
            this.receivedEmail = email;
            return Optional.ofNullable(owner);
        }
    }

    @Test
    public void test_loadByOwnerUsername_passes_email_and_result_through() {
        //given
        StubOwnerPortOut port = new StubOwnerPortOut();
        port.owner = owner("owner@carfix.dev");

        //when
        Optional<Owner> result = new OwnerService(port).loadByOwnerUsername("owner@carfix.dev");

        //then
        assertThat(port.receivedEmail).isEqualTo("owner@carfix.dev");
        assertThat(result).isPresent();
        assertThat(result.get().getBusinessName()).isEqualTo("AutoSerwis Kowalski");
    }

    @Test
    public void test_loadByOwnerUsername_is_empty_when_no_owner_row() {
        //when + then
        assertThat(new OwnerService(new StubOwnerPortOut()).loadByOwnerUsername("x@y.z")).isEmpty();
    }
}
