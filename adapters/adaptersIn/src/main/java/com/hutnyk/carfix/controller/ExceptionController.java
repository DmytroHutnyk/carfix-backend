package com.hutnyk.carfix.controller;

import com.hutnyk.carfix.exceptions.EmailAlreadyTakenException;
import com.hutnyk.carfix.exceptions.PhoneNumberAlreadyTakenException;
import com.hutnyk.carfix.exceptions.UserAlreadyExistsException;
import com.hutnyk.carfix.user.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionController{



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

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUserExistsException(UserAlreadyExistsException ex){
        ProblemDetail problemDetail = null;
        Map<String, String> errors = new HashMap<>();

        if(ex instanceof EmailAlreadyTakenException){
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
            problemDetail.setTitle("Email is already taken");
            errors.put("email", ex.getRejectedValue().toString());

        }else if(ex instanceof PhoneNumberAlreadyTakenException){
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
            problemDetail.setTitle("Phone number is already taken");
            PhoneNumber phoneNumber = (PhoneNumber) ex.getRejectedValue();
            errors.put("phoneCountryCode", phoneNumber.countryCode());
            errors.put("phoneNumber", phoneNumber.phoneNumber());

        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }


}
