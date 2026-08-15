package com.hutnyk.carfix.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

public class UserServiceTest {

    private static final UserId EXISTING_ID = UserId.genId();
    private static final PhoneNumber EXISTING_PHONE = new PhoneNumber("+48", "123456789");
    private static final PasswordHash EXISTING_HASH = PasswordHash.of("$2a$10$storedhashvalue");

    private static User existingUser() {
        return User.builder()
                .id(EXISTING_ID)
                .name("Old")
                .surname("Name")
                .phoneNumber(EXISTING_PHONE)
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(EXISTING_HASH)
                .dateOfBirth(LocalDate.of(1990, 5, 1))
                .addressId(7)
                .build();
    }

    private static final class StubUserPortOut implements UserPortOut {
        User updated;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.of(existingUser());
        }

        @Override
        public boolean existsByEmail(String email) {
            return true;
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            return true;
        }

        @Override
        public User update(User user) {
            this.updated = user;
            return user;
        }
    }

    private static final class EmptyUserPortOut implements UserPortOut {

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            return false;
        }

        @Override
        public User update(User user) {
            throw new AssertionError("update must not be reached when the user is gone");
        }
    }

    private final StubUserPortOut portOut = new StubUserPortOut();
    private final UserService service = new UserService(portOut);

    @Test
    public void test_updateUser_takes_editable_fields_from_the_command() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", LocalDate.of(2000, 1, 15));

        //when
        User result = service.updateUser("john@example.com", command);

        //then
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSurname()).isEqualTo("Surname");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
    }

    @Test
    public void test_updateUser_preserves_credentials_and_identity_from_the_loaded_user() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(portOut.updated.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(portOut.updated.getEmail()).isEqualTo("john@example.com");
        assertThat(portOut.updated.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(portOut.updated.getPhoneNumber()).isEqualTo(EXISTING_PHONE);
        assertThat(portOut.updated.getId()).isEqualTo(EXISTING_ID);
        assertThat(portOut.updated.getAddressId()).isEqualTo(7);
    }

    @Test
    public void test_updating_a_user_whose_record_vanished_fails_as_authentication() {
        //given
        UserService serviceWithNoUser = new UserService(new EmptyUserPortOut());
        UpdateUserCommand command = new UpdateUserCommand("John", "Doe", LocalDate.of(1990, 5, 1));

        //when + then
        assertThatThrownBy(() -> serviceWithNoUser.updateUser("gone@example.com", command))
                .isInstanceOf(AuthenticatedUserMissingException.class)
                .hasMessage("Authenticated user not found: gone@example.com");
    }

    @Test
    public void test_updateUser_null_dateOfBirth_clears_it_instead_of_keeping_the_old_value() {
        //given
        UpdateUserCommand command = new UpdateUserCommand("New", "Surname", null);

        //when
        service.updateUser("john@example.com", command);

        //then
        assertThat(portOut.updated.getDateOfBirth()).isNull();
    }
}
