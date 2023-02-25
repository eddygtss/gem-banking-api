package com.gembankingunited.gembankingapi.exceptions;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@AllArgsConstructor
public class RequestNotFoundException extends Exception {
    public RequestNotFoundException(String message) {
        super(message);
    }
}
