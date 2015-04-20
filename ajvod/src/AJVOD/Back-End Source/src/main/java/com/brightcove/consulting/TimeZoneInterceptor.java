package com.brightcove.consulting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.brightcove.consulting.alj.models.ModelConstants;

public class TimeZoneInterceptor implements HandlerInterceptor {

    @Autowired
    private TimeZoneResolver timeZoneResolver;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession();
		String timeZoneId = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE);
		
		if (StringUtils.isEmpty(timeZoneId)) {
			DateTimeZone timeZone = timeZoneResolver.resolveTimeZone(request);
			session.setAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE, timeZone.getID());
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
