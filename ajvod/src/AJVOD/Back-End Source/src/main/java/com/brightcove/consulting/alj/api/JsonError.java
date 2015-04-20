package com.brightcove.consulting.alj.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public class JsonError {

	private int status;
	private String message;
	private String details;
	private Map<String, String> attributes;

	public JsonError(int status, String message, String details) {
		this.status = status;
		this.message = message;
		this.details = details;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ModelAndView toModelAndView() {
		MappingJacksonJsonView jsonView = new MappingJacksonJsonView();
		Map json = new HashMap();
		json.put("status", this.status);
		if (!StringUtils.isEmpty(this.message)) { 
			json.put("message", this.message);
		}
		if (!StringUtils.isEmpty(this.details)) {
			json.put("details", details);
		}
		if (attributes != null  && !attributes.isEmpty()) {
			for (String key : attributes.keySet()) {
				json.put(key, attributes.get(key));
			}
		}
		return new ModelAndView(jsonView, json);
	}
}
