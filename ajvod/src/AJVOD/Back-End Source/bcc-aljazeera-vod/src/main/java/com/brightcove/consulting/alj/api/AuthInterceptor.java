package com.brightcove.consulting.alj.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.brightcove.consulting.alj.api.errors.NotAuthorizedException;
import com.brightcove.consulting.alj.models.ModelConstants;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (isApiRequest(request)) {
        	HandlerMethod method = (HandlerMethod) handler;
        	
        	NoAuthorization annotation = method.getMethodAnnotation(NoAuthorization.class);
        	if (annotation != null) {
        		return true;
        	}

            String uid = (String) request.getSession().getAttribute(ModelConstants.SESSION_USER_ID);
            if (StringUtils.isEmpty(uid)) {
                throw new NotAuthorizedException();
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
        
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/");
    }
}
