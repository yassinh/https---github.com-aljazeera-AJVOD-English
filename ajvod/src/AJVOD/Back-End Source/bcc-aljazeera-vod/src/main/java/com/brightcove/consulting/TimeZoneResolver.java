package com.brightcove.consulting;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;

public class TimeZoneResolver {

	private static final Logger logger = LoggerFactory.getLogger(TimeZoneResolver.class);

    @Autowired
    private UserManager userManager;

    @Autowired
    private String defaultUserTimezone;

    // to store per request timezone (so we don't have to pass it around everywhere
    private static ThreadLocal<DateTimeZone> userTimeZone = new ThreadLocal<DateTimeZone>();

	public DateTimeZone resolveTimeZone(HttpServletRequest request) {

        // see if we've already established the user's preferred timezone
        HttpSession session = request.getSession();
        String timeZoneId = (String) session.getAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE);

        DateTimeZone timeZone = null;

        if (!StringUtils.isEmpty(timeZoneId)) {
        	try {
        		timeZone = DateTimeZone.forID(timeZoneId);
        	} catch (Exception e) {
        		logger.warn("invalid session timezone id: " + timeZoneId);
        		timeZoneId = null;
        	}
        }
        if (timeZone != null) {
        	userTimeZone.set(timeZone);
        	return timeZone;
        }

        // otherwise, retrieve the preferred locale of the user
        String uid = (String) session.getAttribute(ModelConstants.SESSION_USER_ID);
        if (uid != null) {
        	try {
				User user = userManager.get(uid);
				if (user != null) {
					Map preferences = user.preferences();
					timeZoneId = (String) preferences.get("timeone");
					if (!StringUtils.isEmpty(timeZoneId)) {
						// throws exception if not correct
						timeZone = DateTimeZone.forID(timeZoneId);
					}
				}
			} catch (ServiceException e) {
				logger.error("Exception occurred while trying to get user timezone "+ 
							timeZoneId + "for user: " + uid, e);
				timeZoneId = null;
			}
        }
        if (timeZone != null) {
        	userTimeZone.set(timeZone);
        	return timeZone;
        }
        
        // set timezone from request locale
        Locale locale = request.getLocale();
        TimeZone tz = Calendar.getInstance(locale).getTimeZone();
        try {
	        timeZone = DateTimeZone.forTimeZone(tz);
	        timeZoneId = timeZone.getID();
        } catch (Exception e) {
        	logger.error("Exception occurred while attempting to set request timezone of " + tz.getDisplayName(), e);
        	timeZoneId = null;
        }
        if (timeZone != null) {
        	userTimeZone.set(timeZone);
        	return timeZone;
        }

        // else default to default user timezone
        try {
        	timeZone = DateTimeZone.forID(defaultUserTimezone);
        } catch (Exception e) {
        	logger.error("Exception occurred while attempting to set defaultUserTimezone of " + defaultUserTimezone, e);
        	timeZoneId = null;
        }
        if (timeZone != null) {
        	userTimeZone.set(timeZone);
        	return timeZone;
        } else {
        	userTimeZone.set(DateTimeZone.UTC);
        	return DateTimeZone.UTC;
        }
	}

	public static DateTimeZone getTimeZone() {
		DateTimeZone tz = userTimeZone.get();
		if (tz == null) {
			tz = DateTimeZone.UTC;
		}
		return tz;
	}
}
