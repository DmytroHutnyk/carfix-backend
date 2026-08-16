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

    private static final String EMAIL = "owner@carfix.dev";

    private static Owner owner() {
        User user = User.builder()
                .id(UserId.genId())
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
        return Owner.of(user, "AutoSerwis Kowalski", "5252445567", "146892132");
    }

    private static final class StubOwnerPortOut implements OwnerPortOut {
        Owner owner;
        String receivedEmail;

        @Override
        public Optional<Owner> findOwnerByUsername(String email) {
            this.receivedEmail = email;
            return Optional.ofNullable(owner);
        }
    }

    private final StubOwnerPortOut stub = new StubOwnerPortOut();
    private final OwnerService service = new OwnerService(stub);

    @Test
    public void test_loadByOwnerUsername_returns_the_owner_for_its_email() {
        //given
        stub.owner = owner();

        //when
        Optional<Owner> result = service.loadByOwnerUsername(EMAIL);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getBusinessName()).isEqualTo("AutoSerwis Kowalski");
        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
    }

    @Test
    public void test_loadByOwnerUsername_is_empty_when_no_owner_aggregate() {
        //given
        stub.owner = null;

        //when + then
        assertThat(service.loadByOwnerUsername(EMAIL)).isEmpty();
    }
}
