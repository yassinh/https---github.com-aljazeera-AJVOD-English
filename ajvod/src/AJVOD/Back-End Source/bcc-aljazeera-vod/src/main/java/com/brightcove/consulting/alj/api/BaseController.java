package com.brightcove.consulting.alj.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.services.BrightcoveReadApiService;


public abstract class BaseController {

    @Autowired
    protected String readToken;  

    @Autowired
    protected int pageSize;

	@Autowired
	protected UserManager userManager;
	
	@Autowired
    protected String defaultUserLang;
	
	@Autowired
    protected String defaultUserTimezone;
    
	@Autowired
    protected String defaultUserCountry;

    @Autowired
    protected BrightcoveReadApiService brightcoveReadApiService;

	protected User getUser(HttpServletRequest request) throws ServiceException {
	    User user = (User) request.getAttribute(ModelConstants.USER);
	    if (user != null) {
	        return user;
	    }
	    // make one more attempt to get it
		String uid = (String) request.getSession().getAttribute(ModelConstants.SESSION_USER_ID);
		if (uid != null) {
			return userManager.get(uid);
		}
		return null;
	}

	protected BrightcoveReadApiService readApiService() {
        return brightcoveReadApiService;
    }

	protected Map<String,Object> json(HttpStatus status) {
	    return json(status.value(), null, null);
	}
	protected Map<String,Object> json(HttpStatus status, String message) {
	    return json(status.value(), message, null);
	}
	protected Map<String,Object> json(HttpStatus status, String message, String details) {
	    return json(status.value(), message, details);
	}
	protected Map<String,Object> json(int status, String message, String details) {
	    Map response = new HashMap();
	    response.put("status", status);
	    if (!StringUtils.isEmpty(message)) {
	        response.put("message", message);
	    };
	    if (!StringUtils.isEmpty(details)) {
	        response.put("details", details);
	    }
	    return response;
	}

	protected String jsonp(HttpServletResponse response, String callback, Object jsonObject) {
		response.setContentType("application/javascript; charset=UTF8");
		try {
			return jsonp(response, callback, new JSONObject(jsonObject).toString(4));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	protected DateTimeZone getTimeZone(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String timezoneId = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE);
		if (timezoneId != null) {
			return DateTimeZone.forID(timezoneId);
		}
		return DateTimeZone.UTC;
	}

	private String jsonp(HttpServletResponse response, String callback, String text) {
		String js = "(function() {\n"+
	                "    "+ callback + "(\n" +
			        "        " + text + "\n"+
	                "    );\n" +
			        ")();";
		return text;		
	}

}
