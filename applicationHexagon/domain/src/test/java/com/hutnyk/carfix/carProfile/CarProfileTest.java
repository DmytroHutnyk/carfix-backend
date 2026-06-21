package com.hutnyk.carfix.carProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

public class CarProfileTest {

    private CarProfile createCarProfileWithVin(String vin) {
        return CarProfile.create(
                CarProfileId.genId(),
                "Test Car",
                vin,
                null,
                null,
                null,
                UserId.genId(),
                null,
                1
        );
    }

    private CarProfile createCarProfileWithPlates(String plates) {
        return CarProfile.create(
                CarProfileId.genId(),
                "Test Car",
                null,
                plates,
                null,
                null,
                UserId.genId(),
                null,
                1
        );
    }

    private CarProfile createCarProfileWithServiceDate(LocalDate serviceDate) {
        return CarProfile.create(
                CarProfileId.genId(),
                "Test Car",
                null,
                null,
                serviceDate,
                null,
                UserId.genId(),
                null,
                1
        );
    }

    private CarProfile createCarProfileWithInsuranceDate(LocalDate insuranceDate) {
        return CarProfile.create(
                CarProfileId.genId(),
                "Test Car",
                null,
                null,
                null,
                insuranceDate,
                UserId.genId(),
                null,
                1
        );
    }

    // Tests for validateVin
    @ParameterizedTest
    @ValueSource(strings = {
            "1HGBH41JXMN109186",
            "WBA3A5C58EF123456",
            "5YJSA1E14HF123456",
            "1G1ZT51826F123456",
            "JH4KA8260MC123456"
    })
    public void test_validateVin_valid_vin_and_fieldName_not_null(String vin) {
        //when
        CarProfile result = createCarProfileWithVin(vin);

        //then
        assertThat(result.getVin()).isEqualTo(vin);
    }

    @Test
    public void test_validateVin_returns_null_when_vin_is_null() {
        //when
        CarProfile result = createCarProfileWithVin(null);

        //then
        assertThat(result.getVin()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1HGBH41JXMN10918",      // too short (16 chars)
            "1HGBH41JXMN1091867",     // too long (18 chars)
            "1HGBH41JXMN10918I",      // contains I (invalid character)
            "1HGBH41JXMN-09186",      // contains hyphen
            "1HGBH41JXMN 09186",      // contains space
            "1HGBH41JXMN10918a",      // contains lowercase
            ""                         // empty string
    })
    public void test_validateVin_throws_when_vin_invalid_format(String vin) {
        //when + then
        assertThatThrownBy(() -> createCarProfileWithVin(vin))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_VIN_FORMAT);
    }

    // Tests for validatePlates
    @ParameterizedTest
    @ValueSource(strings = {
            "ABC123",
            "AB1234",
            "A1B2C3",
            "ABC-123",
            "AB 1234",
            "A1B2C3D4",
            "ABCD12345",
            "A12345"
    })
    public void test_validatePlates_valid_plates_and_fieldName_not_null(String plates) {
        //when
        CarProfile result = createCarProfileWithPlates(plates);

        //then
        assertThat(result.getPlates()).isEqualTo(plates);
    }

    @Test
    public void test_validatePlates_returns_null_when_plates_is_null() {
        //when
        CarProfile result = createCarProfileWithPlates(null);

        //then
        assertThat(result.getPlates()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ABCD",                   // too short (less than 5 chars)
            "ABCD1234567",            // too long (more than 10 chars)
            "ABCDE",                  // no digits
            "12345",                  // no letters
            "abc123",                 // contains lowercase
            "ABC@123",                // contains special character
            "ABC.123",                // contains special character
            ""                        // empty string
    })
    public void test_validatePlates_throws_when_plates_invalid_format(String plates) {
        //given
        //when + then
        assertThatThrownBy(() -> createCarProfileWithPlates(plates))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_PLATES_FORMAT);
    }

    // Tests for validateDate
    @Test
    public void test_validateDate_valid_serviceCertificateValidUpTo_and_fieldName_not_null() {
        //given
        LocalDate serviceDate = LocalDate.now().minusYears(10);

        //when
        CarProfile result = createCarProfileWithServiceDate(serviceDate);

        //then
        assertThat(result.getServiceCertificateDate()).isEqualTo(serviceDate);
    }

    @Test
    public void test_validateDate_valid_insuranceValidUpTo_and_fieldName_not_null() {
        //given
        LocalDate insuranceDate = LocalDate.now().minusYears(5);

        //when
        CarProfile result = createCarProfileWithInsuranceDate(insuranceDate);

        //then
        assertThat(result.getInsuranceDate()).isEqualTo(insuranceDate);
    }

    @Test
    public void test_validateDate_valid_date_exactly_20_years_ago() {
        //given
        LocalDate serviceDate = LocalDate.now().minusYears(20);

        //when
        CarProfile result = createCarProfileWithServiceDate(serviceDate);

        //then
        assertThat(result.getServiceCertificateDate()).isEqualTo(serviceDate);
    }

    @Test
    public void test_validateDate_returns_null_when_serviceCertificateValidUpTo_is_null() {
        //when
        CarProfile result = createCarProfileWithServiceDate(null);

        //then
        assertThat(result.getServiceCertificateDate()).isNull();
    }

    @Test
    public void test_validateDate_returns_null_when_insuranceValidUpTo_is_null() {
        //when
        CarProfile result = createCarProfileWithInsuranceDate(null);

        //then
        assertThat(result.getInsuranceDate()).isNull();
    }

    @Test
    public void test_validateDate_throws_when_serviceCertificateValidUpTo_too_old() {
        //given
        LocalDate serviceDate = LocalDate.now().minusYears(21);

        //when + then
        assertThatThrownBy(() -> createCarProfileWithServiceDate(serviceDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EXPIRATION_DATE_TOO_OLD);
    }

    @Test
    public void test_validateDate_throws_when_insuranceValidUpTo_too_old() {
        //given
        LocalDate insuranceDate = LocalDate.now().minusYears(25);

        //when + then
        assertThatThrownBy(() -> createCarProfileWithInsuranceDate(insuranceDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EXPIRATION_DATE_TOO_OLD);
    }

    @Test
    public void test_validateDate_throws_when_serviceCertificateValidUpTo_more_than_20_years_ago() {
        //given
        LocalDate serviceDate = LocalDate.now().minusYears(50);

        //when + then
        assertThatThrownBy(() -> createCarProfileWithServiceDate(serviceDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EXPIRATION_DATE_TOO_OLD);
    }

    @Test
    public void test_validateDate_throws_when_insuranceValidUpTo_more_than_20_years_ago() {
        //given
        LocalDate insuranceDate = LocalDate.now().minusYears(100);

        //when + then
        assertThatThrownBy(() -> createCarProfileWithInsuranceDate(insuranceDate))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EXPIRATION_DATE_TOO_OLD);
    }

    // of() reconstitutes from DB but still enforces structural invariants (VIN/plates format),
    // Only creation-time business rules (dates) are skipped.
    @Test
    public void test_of_validates_vin_format() {
        //given an invalid VIN that must be rejected even when loaded from persistence
        //when + then
        assertThatThrownBy(() -> CarProfile.of(
                CarProfileId.genId(), "Test Car", "not-a-valid-vin", null,
                null, null, UserId.genId(), null, 1))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_VIN_FORMAT);
    }

    @Test
    public void test_of_skips_date_validation_for_too_old_date() {
        //given a date older than create() would allow
        LocalDate tooOld = LocalDate.now().minusYears(50);

        //when
        CarProfile result = CarProfile.of(
                CarProfileId.genId(), "Test Car", null, null,
                tooOld, null, UserId.genId(), null, 1);

        //then
        assertThat(result.getServiceCertificateDate()).isEqualTo(tooOld);
    }
}

