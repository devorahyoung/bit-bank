package com.poalim.mybank.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ActionNotAllowedException extends RuntimeException {

    public ActionNotAllowedException(Long id, String message) {
        super("Account id: " + id + ", Action not allowed: " + message);
    }
}
