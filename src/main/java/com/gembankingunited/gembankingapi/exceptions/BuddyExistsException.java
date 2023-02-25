package com.gembankingunited.gembankingapi.exceptions;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@AllArgsConstructor
public class BuddyExistsException extends Exception {
    public BuddyExistsException(String message) {
        super(message);
    }
}
