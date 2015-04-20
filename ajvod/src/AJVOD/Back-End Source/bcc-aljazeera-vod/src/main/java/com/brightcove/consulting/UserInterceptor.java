package com.brightcove.consulting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;

@Component
public class UserInterceptor implements HandlerInterceptor {

	@Autowired
	private UserManager userManager;

	public UserInterceptor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String uid = (String) request.getSession().getAttribute(ModelConstants.SESSION_USER_ID);
		if (uid != null) {
			try {
				User user = userManager.get(uid);
				request.setAttribute(ModelConstants.USER, user);
			} catch (ServiceException se) {
				//
			}
		}
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			User user = (User) request.getAttribute(ModelConstants.USER);
			if (user != null) {
				modelAndView.addObject(ModelConstants.USER, user);
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
