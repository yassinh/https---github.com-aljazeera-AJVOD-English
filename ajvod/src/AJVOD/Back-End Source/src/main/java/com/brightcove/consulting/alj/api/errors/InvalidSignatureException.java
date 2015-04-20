package com.brightcove.consulting.alj.api.errors;

import org.springframework.http.HttpStatus;


/**
 * Exception thrown upon login if the user's account could not be validated.
 *
 * @author ssayles
 */
public class InvalidSignatureException extends ApiException {

    public InvalidSignatureException() {
        this("Invalid login.");
    }

    public InvalidSignatureException(String message) {
        super(message);
        setDetails("The account uid signature could not be validated. Try logging in again.");
    }

    @Override
    public int getStatus() {
        return HttpStatus.FORBIDDEN.value();
    }

}
