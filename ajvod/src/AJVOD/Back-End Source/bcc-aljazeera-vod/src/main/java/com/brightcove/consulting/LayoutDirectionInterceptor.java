package com.brightcove.consulting;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;

/**
 * Establishes a request attribute indicating if the layout direction should be
 * right-to-left based on the session or request locale.
 *
 * @author ssayles
 */
public class LayoutDirectionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        // TODO: consider canceling if the request is not for html
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

        if (request.getRequestURI().indexOf("/resources") >= 0) {
            return;
        }

        String language = null;
        // see if locale was set for the session
        Locale locale = (Locale) request.getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);

        // set the direction of the current channel if it exists.
        // this takes precedence over locale language
        Channel channel = null;
        if(modelAndView != null)
        {
        	channel = (Channel) modelAndView.getModel().get(ModelConstants.CURRENT_CHANNEL);
        }
        
        if (channel != null) {
            language = channel.getLanguage();
        }

        if (language == null) {
            // otherwise, use the request locale
            // Ideally, the localeResolver should have set a locale regardless
            // but let's make sure.
            if (locale == null) {
            	locale = request.getLocale();
            }
    
            if (locale != null) {
                language = locale.getLanguage();
            }
        } else {
            // override the locale
        	locale = new Locale(language);
            request.setAttribute(ModelConstants.REQUEST_LOCALE_OVERRIDE, locale);
        }
        request.setAttribute("language", language);

        setDirection(request, channel);
        return;
    }

    protected void setDirection(HttpServletRequest request, Channel channel) {
        if (channel != null && channel.getRtl()) {
            request.setAttribute(ModelConstants.LAYOUT_DIRECTION, ModelConstants.LAYOUT_DIRECTION_RIGHT_TO_LEFT);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

}
