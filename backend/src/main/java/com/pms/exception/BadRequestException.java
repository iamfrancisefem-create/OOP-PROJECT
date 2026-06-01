package com.pms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
// OOP — INHERITANCE: extends RuntimeException
public class BadRequestException extends RuntimeException {
    // OOP — ENCAPSULATION: hides the RuntimeException constructor call
    public BadRequestException(String message) {
        super(message);
    }
}
