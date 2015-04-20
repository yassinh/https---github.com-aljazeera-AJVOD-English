package com.brightcove.consulting.alj.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.LocaleResolver;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.config.SpringContext;
import com.brightcove.consulting.util.ExposedReloadableResourceBundleMessageSource;


@Controller
@RequestMapping("/api/preferences")
public class PreferencesController extends BaseController {

//	private static final Logger logger = LoggerFactory.getLogger(PreferencesController.class);

	@Autowired
	UserManager userManager;
	
	@Autowired
	CacheManager cacheManager;

	@Autowired
	LocaleResolver localeResolver;

	@Autowired
	ExposedReloadableResourceBundleMessageSource countryMessages;

	@Autowired
	ExposedReloadableResourceBundleMessageSource timezoneMessages;

	Map<String, List<Map<String, String>>> countriesByLang = new HashMap<String, List<Map<String, String>>>();

	Map<String, List<Map<String, String>>> timezonesByLang = new HashMap<String, List<Map<String, String>>>();

	private ArrayList<Map<String, String>> languages;

	@NoAuthorization
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value="/get", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String,Object> get(HttpServletRequest request) throws ServiceException {

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);
		Map preferences = null;

		String viewLanguage = defaultUserLang;

		if (user != null) {

			preferences = user.preferences(defaultUserLang, defaultUserCountry, defaultUserTimezone);

		} else {

			preferences = new HashMap();

			HttpSession session = request.getSession();
			String lang = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_LOCALE);
			if (!StringUtils.isEmpty(lang)) {
				preferences.put("lang", lang.split("_")[0]); // make sure to leave off country in case it makes it's way in here
			} else {
				preferences.put("lang", defaultUserLang);
			}

			String country = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_COUNTRY);
			if (!StringUtils.isEmpty(country)) {
				preferences.put("country", country.split("_")[0]);
			} else {
				preferences.put("country", defaultUserCountry);
			}

			String timezone = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE);
			if (!StringUtils.isEmpty(timezone)) {
				preferences.put("timezone", timezone);
			} else {
				preferences.put("timezone", defaultUserTimezone);
			}
			
		}
		
		viewLanguage = (String) preferences.get("lang");
		Locale locale = new Locale(viewLanguage);

		jsonResponse.put("preferences", preferences);
		jsonResponse.put("countries", getCountries(locale));
		jsonResponse.put("timezones", getTimezones(locale));
		jsonResponse.put("languages", getLanguages());


		return jsonResponse;
	}

	@NoAuthorization
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value="/set", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String,Object> set(HttpServletRequest request,
			@RequestParam(required=false) String country,
			@RequestParam(required=false) String lang,
			@RequestParam(required=false) String timezone) throws ServiceException {

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);
		Map prefs = new HashMap();
		boolean updated = false;
		Map<String, String> sessionAttrs = new HashMap<String, String>();
		if (!StringUtils.isEmpty(lang)) { 
			prefs.put("lang", lang);
			sessionAttrs.put(ModelConstants.SESSION_PREFERRED_LOCALE, lang);
			updated = true;
		}
		if (!StringUtils.isEmpty(country)) { 
			prefs.put("country", country);
			sessionAttrs.put(ModelConstants.SESSION_PREFERRED_COUNTRY, country);
			updated = true;
		}
		if (!StringUtils.isEmpty(timezone)) { 
			prefs.put("timezone", timezone);
			sessionAttrs.put(ModelConstants.SESSION_PREFERRED_TIMEZONE, timezone);
			updated = true;
		}

		if (user != null) {
			updated = user.setPreferences(prefs);
			if (updated) {
				userManager.save(user);
			}
		}
		jsonResponse.put("updated", updated);

		HttpSession session = request.getSession();
		for (String key : sessionAttrs.keySet()) {
			session.setAttribute(key, sessionAttrs.get(key));
		}

		return jsonResponse;
	}

	@NoAuthorization
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value="/locales", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> locales(HttpServletRequest request) {
		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		ValueWrapper data = cacheManager.getCache("preferences").get("localesJson");
		if (data != null) {
			Map locales = (Map) data.get();
			jsonResponse.put("locales", locales);
			return jsonResponse;
		}

		LocaleResolver localeResolver = (LocaleResolver) SpringContext.getBean("localeResolver");
		List<String> supportedLangs = localeResolver.getSupportedLanguages();

		// {status: ok, locales: {'en': {name: 'English', locales:[{locale:'en_US', name: 'United States'}] }} }
		//
		Map<String, Object> localesJson = new HashMap<String, Object>();

		Locale[] locales = Locale.getAvailableLocales();
		for (int i = 0; i < supportedLangs.size(); i++) {
		
			String supportedLang = supportedLangs.get(i);
			for (Locale locale : locales) {
				// the locale matches language
				if (StringUtils.equals(supportedLang, locale.getLanguage())) {
					// {name: 'English', locales: []}
					Map<String, Object> localeData = (Map<String, Object>) localesJson.get(supportedLang);
					List<Map<String,Object>> localeListJson;
					if (localeData == null) {
						localeListJson = new ArrayList<Map<String,Object>>();
						localeData = new HashMap<String, Object>();
						localeData.put("name", locale.getDisplayLanguage(locale));
						localeData.put("locales", localeListJson);
						localesJson.put(supportedLang, localeData);
					} else {
						localeListJson = (List<Map<String, Object>>) localeData.get("locales");
					}
					// {locale: 'en_US', name: 'United States'}
					Map<String,Object> localeJson = new HashMap<String,Object>();
					localeJson.put("locale", locale.toString());
					localeJson.put("name", locale.getDisplayCountry(locale));
					localeListJson.add(localeJson);
				}
			}
			cacheManager.getCache("preferences").put("localesJson", localesJson);
		}


		jsonResponse.put("locales", localesJson);
		return jsonResponse;
	}
	
	/**
	 * Returns a list of country values to render as part of a json response.
	 * This will order by country name based on the given locale.
	 *
	 * @param locale
	 * @return
	 */
	private List<Map<String, String>> getCountries(Locale locale) {
		List<Map<String, String>> countries = countriesByLang.get(locale.getLanguage());
		if (countries == null) {
			countries = new ArrayList<Map<String, String>>();
			Set<Object> keys = countryMessages.getKeys();
			for (Object key : keys) {
				String countryName = countryMessages.getMessage((String)key, null, locale);
				Map<String, String> country = new HashMap<String,String>();
				country.put("code", (String) key);
				country.put("name", countryName);
				countries.add(country);
			}
			Collections.sort(countries, new Comparator<Map<String,String>>() {
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return o1.get("name").compareTo(o2.get("name"));
				}
			});
		}
		return countries;
	}

	private List<Map<String, String>> getTimezones(Locale locale) {
		List<Map<String, String>> timezones = timezonesByLang.get(locale.getLanguage());
		if (timezones == null) {
			timezones = new ArrayList<Map<String, String>>();
			Set<Object> keys = timezoneMessages.getKeys();
			for (Object key : keys) {
				String timezoneName = timezoneMessages.getMessage((String)key, null, locale);
				Map<String, String> country = new HashMap<String,String>();
				country.put("id", (String) key);
				country.put("name", timezoneName);
				timezones.add(country);
			}
			Collections.sort(timezones, new Comparator<Map<String,String>>() {
				public int compare(Map<String, String> o1,
						Map<String, String> o2) {
					return o1.get("name").compareTo(o2.get("name"));
				}
			});
		}
		return timezones;
	}

	private List<Map<String, String>> getLanguages() {
		if (this.languages == null) {
			languages = new ArrayList<Map<String,String>>();
			List<String> supportedLanguages = localeResolver.getSupportedLanguages();
			for (String lang : supportedLanguages) {
				Locale locale = new Locale(lang.toLowerCase().trim());
				String langName = locale.getDisplayLanguage(locale);
				Map<String, String> langJson = new HashMap<String, String>();
				langJson.put("code", lang.toLowerCase().trim());
				langJson.put("name", langName);
				languages.add(langJson);
			}
		}
		return languages;
	}
}
