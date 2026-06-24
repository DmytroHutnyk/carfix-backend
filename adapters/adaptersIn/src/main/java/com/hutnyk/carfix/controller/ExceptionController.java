package com.hutnyk.carfix.controller;

import com.hutnyk.carfix.exception.CustomerNotFoundException;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exceptions.EmailAlreadyTakenException;
import com.hutnyk.carfix.exceptions.PhoneNumberAlreadyTakenException;
import com.hutnyk.carfix.exceptions.UserAlreadyExistsException;
import com.hutnyk.carfix.user.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionController{

    /**
     * Handles exceptions thrown by {@link jakarta.validation.constraints} validation annotations or custom validation annotations.
     * <p>
     * Primary those, that are thrown in DTOs.
     * @param ex
     * @return {@code ResponseEntity<ProblemDetail>} with all the details why the exception occurred
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleDtoValidation(MethodArgumentNotValidException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for request DTO");
        problemDetail.setTitle("Validation error");

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }


    /**
     * Handles exceptions of type <code>UserAlreadyExistsException</code> when user with such email or phone number already exists.
     *
     * @param ex
     * @return {@code ResponseEntity<ProblemDetail>} with all the details why the exception occurred
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUserExistsException(UserAlreadyExistsException ex){
        ProblemDetail problemDetail = null;
        Map<String, String> errors = new HashMap<>();

        if(ex instanceof EmailAlreadyTakenException){
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
            errors.put("email", "User with " + ex.getRejectedValue().toString() + " email already exists");

        }else if(ex instanceof PhoneNumberAlreadyTakenException){
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
            PhoneNumber phoneNumber = (PhoneNumber) ex.getRejectedValue();
            errors.put("phoneCountryCodeAndPhoneNumber",
                    "User with " + phoneNumber.phoneNumber() + " phone number already exists");

        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        problemDetail.setTitle("User creation failed");
        problemDetail.setDetail("User creation failed");
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handles {@link DomainObjectValidationException} thrown when a domain object's invariants are violated
     * (e.g. a value that passes DTO bean-validation but is still rejected by the domain). Maps to 400 with
     * the offending field under the {@code errors} property.
     *
     * @param ex
     * @return {@code ResponseEntity<ProblemDetail>} with the field that failed domain validation
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleDomainValidation(DomainObjectValidationException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Domain validation failed");
        problemDetail.setTitle("Validation error");

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getFieldName(), ex.getMessage());
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleLoginFailure(BadCredentialsException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authorization failed");
        problemDetail.setTitle("Authorization failed");
//todo Authentication
        problemDetail.setProperty("authorization", "Wrong email or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleLoginFailure(CustomerNotFoundException ex){
        //todo Authentication
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Authorization failed");
        problemDetail.setTitle("Authorization failed");
        problemDetail.setProperty("authorization", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        problemDetail.setTitle("Access denied");
        problemDetail.setProperty("authorization", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

}
