package com.hutnyk.carfix.user;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.CityResolver;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.LocationCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.out.user.EmailVerificationCodePortOut;
import com.hutnyk.carfix.out.user.UserNotificationPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.exception.AccountDeletionNotAllowedException;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.user.exception.VerificationCodeAttemptsExceededException;
import com.hutnyk.carfix.user.exception.VerificationCodeExpiredException;
import com.hutnyk.carfix.user.exception.VerificationCodeInvalidException;
import com.hutnyk.carfix.user.exception.VerificationCodeNotFoundException;
import com.hutnyk.carfix.user.exception.VerificationCodeResendTooSoonException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationService
public class UserService implements UserPortIn {

    private final UserPortOut userPortOut;
    private final AddressPortOut addressPortOut;
    private final CityResolver cityResolver;
    private final EmailVerificationCodePortOut verificationCodePortOut;
    private final UserNotificationPortOut userNotificationPortOut;
    private final ReviewPortOut reviewPortOut;
    private final BookingPortOut bookingPortOut;
    private final CarProfilePortOut carProfilePortOut;
    private final CustomerPortOut customerPortOut;
    private final Clock clock;

    public UserService(UserPortOut userPortOut,
                       AddressPortOut addressPortOut,
                       EmailVerificationCodePortOut verificationCodePortOut,
                       UserNotificationPortOut userNotificationPortOut,
                       ReviewPortOut reviewPortOut,
                       BookingPortOut bookingPortOut,
                       CarProfilePortOut carProfilePortOut,
                       CustomerPortOut customerPortOut,
                       Clock clock) {
        this.userPortOut = userPortOut;
        this.addressPortOut = addressPortOut;
        this.cityResolver = new CityResolver(addressPortOut);
        this.verificationCodePortOut = verificationCodePortOut;
        this.userNotificationPortOut = userNotificationPortOut;
        this.reviewPortOut = reviewPortOut;
        this.bookingPortOut = bookingPortOut;
        this.carProfilePortOut = carProfilePortOut;
        this.customerPortOut = customerPortOut;
        this.clock = clock;
    }

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userPortOut.loadUserByEmail(email);
    }

    @Override
    public User updateUser(String email, UpdateUserCommand command) {
        User existing = loadOrThrow(email);
        LocationCommand preferred = command.preferredLocation();
        Integer preferredCityId = preferred == null
                ? null
                : cityResolver.resolveCityId(preferred.city(), preferred.region(),
                        CountryIso.parse(preferred.countryIso()), preferred.latitude(), preferred.longitude());

        User user = User.of(
                existing.getId(), command.name(), command.surname(), existing.getPhoneNumber(), existing.getEmail(),
                existing.getRole(), existing.getPasswordHash(), command.dateOfBirth(), existing.getAddressId(),
                preferredCityId, existing.getEmailVerifiedAt());
        return userPortOut.update(user);
    }

    @Override
    public AddressView updateAddress(String email, UpdateUserAddressCommand command) {
        User existing = loadOrThrow(email);
        CountryIso countryIso = CountryIso.parse(command.countryIso());
        Integer cityId = cityResolver.resolveCityId(command.city(), command.region(), countryIso);

        Address address = Address.of(
                existing.getAddressId(),
                command.streetName(),
                command.buildingNumber(),
                command.flatNumber(),
                command.postalCode(),
                cityId,
                command.latitude(),
                command.longitude(),
                command.googlePlaceId()
        );

        Address saved;
        if (existing.getAddressId() == null) {
            saved = addressPortOut.insert(address);
            userPortOut.update(existing.linkAddress(saved.getId()));
        } else {
            saved = addressPortOut.update(address);
        }

        return addressPortOut.loadView(saved.getId())
                .orElseThrow(() -> new UnexpectedStateException("Address vanished right after write: " + saved.getId()));
    }

    @Override
    public void deleteAddress(String email) {
        User existing = loadOrThrow(email);
        if (existing.getAddressId() == null) {
            return;
        }
        userPortOut.update(existing.unlinkAddress());
        addressPortOut.deleteById(existing.getAddressId());
    }

    @Override
    public void deleteAccount(String email) {
        User user = loadOrThrow(email);
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new AccountDeletionNotAllowedException(user.getRole());
        }
        UUID userId = user.getId().id();

        reviewPortOut.deleteByCustomerId(userId);
        bookingPortOut.deleteAllByCustomerId(userId);
        carProfilePortOut.deleteAllByCustomerId(userId);
        verificationCodePortOut.deleteByUserId(user.getId());
        customerPortOut.deleteByUserId(userId);
        if (user.getAddressId() != null) {
            addressPortOut.deleteById(user.getAddressId());
        }
    }

    @Override
    public void requestEmailVerification(String email) {
        User user = loadOrThrow(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }
        Instant now = clock.instant();
        Optional<EmailVerificationCode> existing = verificationCodePortOut.findByUserId(user.getId());
        existing.filter(code -> !code.canResend(now)).ifPresent(code -> {
            throw new VerificationCodeResendTooSoonException(code.secondsUntilResend(now));
        });
        String plainCode = EmailVerificationCode.generate();
        EmailVerificationCode fresh = EmailVerificationCode.issue(user.getId(), plainCode, now);
        if (existing.isPresent()) {
            verificationCodePortOut.update(fresh);
        } else {
            verificationCodePortOut.insert(fresh);
        }
        userNotificationPortOut.sendEmailVerificationCode(user, plainCode);
    }

    // wrong code must still count: the attempt increment commits despite the exception
    @Override
    @Transactional(noRollbackFor = {VerificationCodeInvalidException.class, VerificationCodeAttemptsExceededException.class})
    public User verifyEmail(String email, String code) {
        User user = loadOrThrow(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email);
        }
        Instant now = clock.instant();
        EmailVerificationCode active = verificationCodePortOut.findByUserId(user.getId())
                .orElseThrow(VerificationCodeNotFoundException::new);
        if (active.isExpired(now)) {
            throw new VerificationCodeExpiredException();
        }
        if (active.attemptsExhausted()) {
            throw new VerificationCodeAttemptsExceededException();
        }
        if (!active.matches(code)) {
            EmailVerificationCode failed = verificationCodePortOut.update(active.registerFailedAttempt());
            if (failed.attemptsExhausted()) {
                throw new VerificationCodeAttemptsExceededException();
            }
            throw new VerificationCodeInvalidException(failed.attemptsLeft());
        }
        verificationCodePortOut.deleteByUserId(user.getId());
        return userPortOut.update(user.verifyEmail(now));
    }

    private User loadOrThrow(String email) {
        return userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(email));
    }
}
