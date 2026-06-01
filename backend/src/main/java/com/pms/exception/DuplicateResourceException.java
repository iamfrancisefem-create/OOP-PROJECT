package com.pms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
// OOP — INHERITANCE: extends RuntimeException
public class DuplicateResourceException extends RuntimeException {
    // OOP — ENCAPSULATION: hides parent constructor behind domain-specific exception name
    public DuplicateResourceException(String message) {
        super(message);
    }
}
