package com.hutnyk.carfix.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ValidatorTest {

    @Test
    public void test_notNull_value_and_fieldName_not_null(){
        //given
        Object value = new Object();

        //when
        Object result = Validator.notNull(value, "fieldName");

        //then
        assertThat(result).isSameAs(value);
    }

    @Test
    public void test_notNull_value_not_null_and_fieldName_is_null(){
        //given
        Object value = new Object();

        //when
        Object result = Validator.notNull(value, null);

        //then
        assertThat(result).isSameAs(value);
    }

    @Test
    public void test_notNull_throws_when_value_is_null(){
        //given
        Object object = null;
        String s = "a";


        // when + then
        assertThatThrownBy(() -> Validator.notNull(object, "fieldName"))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @Test
    public void test_notBlank_value_and_fieldName_not_null(){
        //given
        String value = "some not blank string";
        String fieldName = "some field name";

        //when
        String result = Validator.notBlank(value, fieldName);

        //then
        assertThat(result).isEqualTo(value);
    }

    @Test
    public void test_notBlank_value_not_null_and_fieldName_is_null(){
        //given
        String value = "some not blank string";
        String fieldName = null;

        //when
        String result = Validator.notBlank(value, fieldName);

        //then
        assertThat(result).isEqualTo(value);
    }

    @Test
    public void test_notBlank_throws_when_value_is_blank(){
        //given
        String value = "   ";
        String fieldName = "some field name";

        //when + then
       assertThatThrownBy(() -> Validator.notBlank(value, fieldName))
               .isInstanceOf(DomainObjectValidationException.class)
               .extracting("errorType")
               .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_notBlank_throws_when_value_is_empty(){
        //given
        String value = "";
        String fieldName = "some field name";

        //when + then
        assertThatThrownBy(() -> Validator.notBlank(value, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_validateEmail_valid_email_and_fieldName_not_null(){
        //given
        String email = "user123+@example.com";
        String fieldName = "email";

        //when
        String result = Validator.validateEmail(email, fieldName);

        //then
        assertThat(result).isEqualTo(email);
    }

    @Test
    public void test_validateEmail_throws_when_email_is_null(){
        String email = null;
        String fieldName = "email";

        //when + then
        assertThatThrownBy(() -> Validator.validateEmail(email, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ",
            ""
    })
    public void test_validateEmail_throws_when_email_empty_or_blank(String email){
        String fieldName = "email";

        //when + then
        assertThatThrownBy(() -> Validator.validateEmail(email, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "userexample.com",
            "user@",
            "user@example",
            "user@exam ple.com"
    })
    public void test_validateEmail_throws_when_email_wrong_format(String email){
        String fieldName = "email";

        //when + then
        assertThatThrownBy(() -> Validator.validateEmail(email, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_EMAIL_FORMAT);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567890",
            "12345",
            "123456789012345"
    })
    public void test_validatePhoneNumber_valid_phoneNumber_and_fieldName_not_null(String phoneNumber){
        String fieldName = "phoneNumber";

        //when
        String result = Validator.validatePhoneNumber(phoneNumber, fieldName);

        //then
        assertThat(result).isEqualTo(phoneNumber);
    }


    @Test
    public void test_validatePhoneNumber_throws_when_phoneNumber_is_null(){
        //given
        String phoneNumber = null;
        String fieldName = "phoneNumber";

        //when + then
        assertThatThrownBy(() -> Validator.validatePhoneNumber(phoneNumber, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  ",
            ""
    })
    void test_validatePhoneNumber_throws_when_phoneNumber_blank_or_empty(String phoneNumber) {
        String fieldName = "phoneNumber";

        // when + then
        assertThatThrownBy(() -> Validator.validatePhoneNumber(phoneNumber, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234",                  // too short
            "1234567890123456",      // too long
            "12345abc",              // contains letters
            "12345-6789"             // special characters
    })
    void test_validatePhoneNumber_throws_when_phoneNumber_is_wrong_format(String phoneNumber) {
        String fieldName = "phoneNumber";

        assertThatThrownBy(() -> Validator.validatePhoneNumber(phoneNumber, fieldName))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_PHONE_FORMAT);
    }

}
