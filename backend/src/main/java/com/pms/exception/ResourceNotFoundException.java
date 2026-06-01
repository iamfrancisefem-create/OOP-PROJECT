package com.pms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
// OOP — INHERITANCE: extends RuntimeException — inherits all exception behaviour (message, stack trace)
public class ResourceNotFoundException extends RuntimeException {
    // OOP — ENCAPSULATION: constructor chains to parent via super(), hiding the exception-building logic
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
