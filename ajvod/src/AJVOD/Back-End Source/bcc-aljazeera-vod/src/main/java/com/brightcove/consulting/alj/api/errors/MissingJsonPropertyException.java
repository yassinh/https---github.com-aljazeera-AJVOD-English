package com.brightcove.consulting.alj.api.errors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class MissingJsonPropertyException extends ApiException {

    public MissingJsonPropertyException() {
        super("Missing JSON data.");
    }

    public MissingJsonPropertyException(String...properties) {
        super("Missing required JSON properties: " + StringUtils.join(properties));
    }

    @Override
    public int getStatus() {
        // TODO Auto-generated method stub
        return HttpServletResponse.SC_BAD_REQUEST;
    }

}
