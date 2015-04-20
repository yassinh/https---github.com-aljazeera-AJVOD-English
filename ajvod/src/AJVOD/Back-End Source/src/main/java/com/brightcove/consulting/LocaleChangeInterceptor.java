package com.brightcove.consulting;

import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Customized locale change interceptor that checks to see if a given locale is
 * supported before allowing it to be set.
 *
 * @author ssayles
 */
public class LocaleChangeInterceptor extends org.springframework.web.servlet.i18n.LocaleChangeInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {
        if (request.getRequestURI().indexOf("/resources") >= 0) {
            return true;
        }
        String newLocale = request.getParameter(getParamName());
        if (newLocale != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);

            if (localeResolver == null) {
                throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
            }
            Locale locale = StringUtils.parseLocaleString(newLocale);

            if (isSupported(localeResolver, locale)) {
                localeResolver.setLocale(request, response, locale);
            }
        }
        // Proceed in any case.
        return true;
    }

    private boolean isSupported(LocaleResolver localeResolver, Locale locale) {
        if (localeResolver instanceof com.brightcove.consulting.LocaleResolver) {
            List<String> supportedLanguages = ((com.brightcove.consulting.LocaleResolver)localeResolver).getSupportedLanguages();
            return supportedLanguages.contains(locale.getLanguage());
        }
        return false;
    }
}
