package com.brightcove.consulting.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

public class WebUtils {

    /**
     * Takes the given text and attempts to convert it into a compatible url
     * slug.  This will normalize all given characters, remove any non-ascii
     * alphanumeric characters, and convert any spaces into '-'. 
     *
     * @param text The text to convert into a slug.
     * @return A slugified version of the given text or null if it is empty.
     */
    public static String slugify(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        text = text.trim();

        //text = normalize(text);
        // collapse duplicate whitespace to single space
        text = text.replaceAll("\\s+", " ");
        text = text.replace(" ", "-");

        text = text.toLowerCase();
        
        if (text.equals("-")) {
            return null;
        }

        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this really shouldn't ever happen
            e.printStackTrace();
        }
        text = text.replace("%2F", "");
        text = text.replace("%26", "and");
        text = text.replace("%27", "");
        return text;
    }

}
