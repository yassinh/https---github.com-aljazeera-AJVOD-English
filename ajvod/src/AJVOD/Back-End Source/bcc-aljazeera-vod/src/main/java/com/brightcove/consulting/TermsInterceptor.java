package com.brightcove.consulting;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Customized terms interceptor that retrieves the corresponding language terms given the locale.
 *
 * @author jfinlayson
 */
public class TermsInterceptor implements HandlerInterceptor {

	private static Map<String, HashMap<String,String>> languageTerms = new HashMap<String, HashMap<String,String>>();

	/**
	 * The base terms that have been identified as being used by the front-end.
	 */
	@Autowired
	private Properties frontEndTerms;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {

        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    	
    	LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
    	Locale locale = localeResolver.resolveLocale(request);
    	String localeKey = locale.getLanguage();
        
        if(!languageTerms.containsKey(localeKey)){
        	ResourceBundle bundle = ResourceBundle.getBundle("localization/terms", locale);

        	languageTerms.put(localeKey, new HashMap<String, String>());
		    for(Object termKey : frontEndTerms.keySet()){
		    	languageTerms.get(localeKey).put(((String)termKey).replace('.', '_'), bundle.getString((String) termKey));
		    }

        }
    	request.setAttribute("terms", languageTerms.get(localeKey));
        return;
    }

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
}
