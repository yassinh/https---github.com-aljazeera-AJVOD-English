package com.brightcove.consulting.alj.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.brightcove.consulting.config.SpringContext;

/**
 * Provides support for rendering localized date formats.
 * @author ssayles
 */
public class DateRenderer {

	private static ReloadableResourceBundleMessageSource terms;
	private static Map<Locale, DateRenderer> renderers = new ConcurrentHashMap<Locale, DateRenderer>();

	private Locale locale;

	protected DateRenderer(Locale locale) {
		this.locale = locale;
	}

	public String render(Date date, String termKey) {
		String message = getTerms().getMessage(termKey, null, null, locale);
		if (message == null) {
			return StringUtils.EMPTY;
		}
		DateFormat df = new SimpleDateFormat(message, locale);
		return df.format(date);
	}

	public static DateRenderer getInstance(Locale locale) {
		DateRenderer dateRenderer = renderers.get(locale);
		if (dateRenderer == null) {
			dateRenderer = new DateRenderer(locale);
			renderers.put(locale, dateRenderer);
		}
		return dateRenderer;
	}

	private static ReloadableResourceBundleMessageSource getTerms() {
		if (terms == null) {
			terms = (ReloadableResourceBundleMessageSource) SpringContext.getBean("messageSource");
		}
		return terms;
	}
}
