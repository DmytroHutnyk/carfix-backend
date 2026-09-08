package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.UserId;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

public class BranchTest {

    private static final BranchId ID = BranchId.genId();
    private static final UserId OWNER = UserId.genId();

    @Test
    public void test_create_is_active_and_parses_the_zone() {
        Branch branch = Branch.create(ID, "AutoFix", "+48221234567", "kontakt@autofix.pl", "Europe/Warsaw", 5, OWNER);

        assertThat(branch.getId()).isEqualTo(ID);
        assertThat(branch.getStatus()).isEqualTo(BranchStatus.ACTIVE);
        assertThat(branch.getTz()).isEqualTo(ZoneId.of("Europe/Warsaw"));
        assertThat(branch.getAddressId()).isEqualTo(5);
        assertThat(branch.getOwnerId()).isEqualTo(OWNER);
    }

    @Test
    public void test_create_rejects_unknown_zone() {
        assertThatThrownBy(() -> Branch.create(ID, "AutoFix", "+48221234567", "kontakt@autofix.pl", "Mars/Olympus", 5, OWNER))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.INVALID_TIMEZONE, "timezone");
    }

    @Test
    public void test_phone_must_be_international() {
        assertThatThrownBy(() -> Branch.create(ID, "AutoFix", "221234567", "kontakt@autofix.pl", "Europe/Warsaw", 5, OWNER))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.INVALID_PHONE_FORMAT, "phoneNumber");
    }

    @Test
    public void test_of_rehydrates_seed_shaped_row() {
        Branch branch = Branch.of(ID, "Serwis Ursus", "+48224443311", "warsztat@serwis-ursus.pl",
                BranchStatus.SUSPENDED, ZoneId.of("Europe/Warsaw"), 7, OWNER, "desc", CancellationPolicy.STRICT);

        assertThat(branch.getStatus()).isEqualTo(BranchStatus.SUSPENDED);
        assertThat(branch.getPhoneNumber()).isEqualTo("+48224443311");
        assertThat(branch.getDescription()).isEqualTo("desc");
        assertThat(branch.getCancellationPolicy()).isEqualTo(CancellationPolicy.STRICT);
    }

    @Test
    public void test_create_defaults_description_and_policy() {
        Branch branch = Branch.create(ID, "AutoFix", "+48221234567", "kontakt@autofix.pl", "Europe/Warsaw", 5, OWNER);

        assertThat(branch.getDescription()).isNull();
        assertThat(branch.getCancellationPolicy()).isEqualTo(CancellationPolicy.MODERATE);
    }

    @Test
    public void test_rejects_null_cancellation_policy() {
        assertThatThrownBy(() -> Branch.of(ID, "AutoFix", "+48221234567", "kontakt@autofix.pl",
                BranchStatus.ACTIVE, ZoneId.of("Europe/Warsaw"), 5, OWNER, null, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("cancellationPolicy");
    }
}
