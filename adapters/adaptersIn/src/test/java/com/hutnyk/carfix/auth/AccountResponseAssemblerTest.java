package com.hutnyk.carfix.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.auth.dto.response.AccountResponse;
import com.hutnyk.carfix.auth.dto.response.OwnerAccountResponse;
import com.hutnyk.carfix.auth.mapper.LoginUserMapperImpl;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.in.customer.CustomerPortIn;
import com.hutnyk.carfix.in.customer.commands.RegisterUserCommand;
import com.hutnyk.carfix.in.owner.OwnerPortIn;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class AccountResponseAssemblerTest {

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

    private static final class StubCustomerPortIn implements CustomerPortIn {
        @Override
        public Customer registerCustomer(RegisterUserCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Customer> loadByCustomerUsername(String email) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubOwnerPortIn implements OwnerPortIn {
        Owner owner;

        @Override
        public Optional<Owner> loadByOwnerUsername(String email) {
            return Optional.ofNullable(owner);
        }
    }

    private final StubOwnerPortIn ownerStub = new StubOwnerPortIn();
    private final AccountResponseAssembler assembler =
            new AccountResponseAssembler(new StubCustomerPortIn(), ownerStub, new LoginUserMapperImpl());

    @Test
    public void test_assemble_owner_returns_owner_account_with_business_tail() {
        //given
        ownerStub.owner = owner();

        //when
        AccountResponse response = assembler.assemble(EMAIL, UserRole.OWNER);

        //then
        assertThat(response).isInstanceOf(OwnerAccountResponse.class);
        OwnerAccountResponse ownerAccount = (OwnerAccountResponse) response;
        assertThat(ownerAccount.role()).isEqualTo(UserRole.OWNER);
        assertThat(ownerAccount.user().email()).isEqualTo(EMAIL);
        assertThat(ownerAccount.businessName()).isEqualTo("AutoSerwis Kowalski");
        assertThat(ownerAccount.vatIn()).isEqualTo("5252445567");
        assertThat(ownerAccount.regon()).isEqualTo("146892132");
    }

    @Test
    public void test_assemble_owner_without_aggregate_fails_authentication() {
        //given
        ownerStub.owner = null;

        //when + then
        assertThatThrownBy(() -> assembler.assemble(EMAIL, UserRole.OWNER))
                .isInstanceOf(AuthenticatedUserMissingException.class);
    }
}
