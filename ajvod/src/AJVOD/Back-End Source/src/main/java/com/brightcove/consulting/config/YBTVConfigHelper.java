package com.brightcove.consulting.config;

import com.brightcove.consulting.alj.models.NavRepository;
import com.brightcove.consulting.alj.utils.NavUtils;

/**
 * Provides convenience methods for accessing configuration values.  This is
 * particularly useful in non-spring-managed beans.
 *
 * @author ssayles
 */
public class YBTVConfigHelper {

    public static String topicCustomField() {
        return (String) SpringContext.getBean("topicCustomField");
    }

    public static String channelCustomField() {
        return (String) SpringContext.getBean("channelCustomField");
    }

    public static String showGenreCustomField() {
        return (String) SpringContext.getBean("showGenreCustomField");
    }

    public static String[] showsCustomFieldValues() {
        return (String[]) SpringContext.getBean("showsCustomFieldValues");
    }

    public static String[] newsCustomFieldValues() {
        return (String[]) SpringContext.getBean("newsCustomFieldValues");
    }

    public static String videotypeCustomField() {
        return (String) SpringContext.getBean("videotypeCustomField");
    }

    public static NavRepository navRepository() {
        return (NavRepository)SpringContext.getBean("navRepository");
    }

    public static NavUtils navUtils() {
        return (NavUtils) SpringContext.getBean("navUtils");
    }
}
