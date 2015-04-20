package com.brightcove.consulting.alj.api.errors;

import org.springframework.http.HttpStatus;

public class NotAuthorizedException extends ApiException {

    public NotAuthorizedException() {
        this("Not authorized.");
    }

    public NotAuthorizedException(String message) {
        super(message);
        setDetails("You must first login to access this resource.");
    }

    @Override
    public int getStatus() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}
