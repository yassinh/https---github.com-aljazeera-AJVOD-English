package com.brightcove.consulting.alj.api.errors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class ApiException extends Exception {

    private String details;

    private Map<String,String> attributes = new HashMap<String,String>();

    public ApiException() {
    }

    public ApiException(String arg0) {
        super(arg0);
    }
    public ApiException(String message, String details) {
        super(message);
        this.details = details;
    }

    public ApiException(Throwable arg0) {
        super(arg0);
    }

    public ApiException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }

    public void addAttribute(String key, String value) {
    	this.attributes.put(key, value);
    }

    public Map<String,String> getAttributes() {
    	return this.attributes;
    }

    public abstract int getStatus();
}
