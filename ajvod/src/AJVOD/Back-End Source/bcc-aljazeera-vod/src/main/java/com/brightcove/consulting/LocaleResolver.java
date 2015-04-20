package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;

public class LocaleResolver extends SessionLocaleResolver {

	private static final Logger logger = LoggerFactory.getLogger(LocaleResolver.class);

    private List<String> supportedLanguages = new ArrayList<String>();

    @Autowired
    private UserManager userManager;
    
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
    
    @SuppressWarnings("rawtypes")
	public Locale resolveLocale(HttpServletRequest request) {
        // if the current locale was overridden (like for a specific channel)
        // then use that locale
        Locale locale = (Locale) request.getAttribute(ModelConstants.REQUEST_LOCALE_OVERRIDE);
        if (locale != null) {
            return locale;
        }

        // see if we've already established the user's preferred locale
        HttpSession session = request.getSession();
        String localeValue = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_LOCALE);
        if (!StringUtils.isEmpty(localeValue)) {
        	return LocaleUtils.toLocale(localeValue);
        }

        // otherwise, retrieve the preferred locale of the user
        String uid = (String) session.getAttribute(ModelConstants.SESSION_USER_ID);
        if (uid != null) {
        	try {
				User user = userManager.get(uid);
				if (user != null) {
					Map preferences = user.preferences();
					String prefValue = (String) preferences.get("locale");
					if (!StringUtils.isEmpty(prefValue)) {
						locale = LocaleUtils.toLocale(prefValue);
						session.setAttribute(ModelConstants.SESSION_PREFERRED_LOCALE, prefValue);
						return locale;
					}
					// if no locale value, see if there is one for lang
					prefValue = (String) preferences.get("lang");
					if (!StringUtils.isEmpty(prefValue)) {
						locale = LocaleUtils.toLocale(prefValue);
						session.setAttribute(ModelConstants.SESSION_PREFERRED_LOCALE, prefValue);
						return locale;
					}
				}
			} catch (ServiceException e) {
				logger.error("Exception occurred while trying to get user: " + uid, e);
			}
        }

        return super.resolveLocale(request);
    }
    
    /**
     * Called if a locale has not been established by the user.  This will
     * assign the locale to the requested locale if it matches a supported
     * language.  Otherwise, the default locale will be used.
     */
    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale locale = request.getLocale();
        String language = locale.getLanguage();
        if (supportedLanguages.contains(language)) {
            return locale;
        }
        locale = getDefaultLocale();
        return locale;
    }
    
}
