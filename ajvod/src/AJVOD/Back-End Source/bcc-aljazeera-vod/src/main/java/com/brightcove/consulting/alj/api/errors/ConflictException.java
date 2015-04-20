package com.brightcove.consulting.alj.api.errors;
import javax.servlet.http.HttpServletResponse;


public class ConflictException extends ApiException {

    public ConflictException() {
        this("There was a conflict with your request.");
    }

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String details ) {
        super(message, (String)null);
    }
    

    @Override
    public int getStatus() {
        return HttpServletResponse.SC_CONFLICT;
    }

}
