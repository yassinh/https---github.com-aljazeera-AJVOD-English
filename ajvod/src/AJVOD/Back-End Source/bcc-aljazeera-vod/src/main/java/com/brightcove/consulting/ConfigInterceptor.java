package com.brightcove.consulting;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.config.GigyaConfig;

public class ConfigInterceptor implements HandlerInterceptor {

    @Autowired
    private LocaleResolver localeResolver;
    @Autowired
    private GigyaConfigResolver gigyaConfigResolver;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (request.getRequestURI().indexOf("/resources") >= 0 || modelAndView == null) {
            return;
        }
        Locale locale = localeResolver.resolveLocale(request);
        GigyaConfig config = gigyaConfigResolver.resolveConfig(locale);
        
        Channel channel = (Channel) modelAndView.getModel().get(ModelConstants.CURRENT_CHANNEL);
        String channelLocale = "en_US";
        if(channel != null){
        	channelLocale = channel.getConfig("locale");
        	String[] localeCode = channelLocale.split("_");
        	channelLocale = localeCode[0].toLowerCase() + "_" + localeCode[1];
        }

        if(request.getSession() != null && channel != null){
        	request.getSession().setAttribute(ModelConstants.SESSION_CHANNEL_ID, channel.getKey());
        }
        else if(channel == null){
        	request.getSession().setAttribute(ModelConstants.SESSION_CHANNEL_ID, locale.getLanguage());
        }
        

        if (config != null) {
            modelAndView.addObject(ModelConstants.GIGYA_CONFIG, config);
        }
    }

	@Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
        
    }
}
