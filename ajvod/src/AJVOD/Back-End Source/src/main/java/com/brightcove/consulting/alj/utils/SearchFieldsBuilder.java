package com.brightcove.consulting.alj.utils;

import static com.brightcove.consulting.config.YBTVConfigHelper.newsCustomFieldValues;
import static com.brightcove.consulting.config.YBTVConfigHelper.showsCustomFieldValues;
import static com.brightcove.consulting.config.YBTVConfigHelper.showGenreCustomField;
import static com.brightcove.consulting.config.YBTVConfigHelper.videotypeCustomField;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.CustomField;

public class SearchFieldsBuilder {

    List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();

    public SearchFieldsBuilder() {}
    
    /**
     * Makes a best guess attempt at what kind of custom fields will get added.
     * This currently handles the values 'news' and 'shows', otherwise, it is
     * assumed the given type is for a show genre.
     *
     * @param type
     * @return
     */
    public SearchFieldsBuilder add(String type) {
        return this.add(type, BrightcoveLogic.ANY);
    }

    public SearchFieldsBuilder add(String type, BrightcoveLogic expr) {
        if (type == null || StringUtils.isEmpty(type)) {
            return this;
        }

        type = type.trim();

        if (type.equals("news")) {
            String[] values = newsCustomFieldValues();
            for (String value : values) {
                searchFields.add(new CustomField(videotypeCustomField(), value, expr));
            }
        } else if (type.equals("shows")) {
            String[] values = showsCustomFieldValues();
            for (String value : values) {
                searchFields.add(new CustomField(videotypeCustomField(), value, expr));
            }            
        } else if (!type.equals("all")) {
            // for now, we're going to assume it's a ShowGenre
            searchFields.add(new CustomField(showGenreCustomField(), type, expr));
        }
        return this;
    }

    public SearchFieldsBuilder add(Channel channel) {
        return this.add(channel, BrightcoveLogic.ANY);
    }

    public SearchFieldsBuilder add(Channel channel, BrightcoveLogic expr) {
        if (channel != null) {
            searchFields.add(new CustomField(channel.getCustomFieldName(),
                    channel.getCustomFieldValue(), expr));
        }
        return this;
    }

    public SearchFieldsBuilder add(NavigationItem navItem) {
        return this.add(navItem, BrightcoveLogic.ANY);
    }

    public SearchFieldsBuilder add(String customFieldName, String customFieldValue, BrightcoveLogic expr) {
        CustomField customField = new CustomField(customFieldName, customFieldValue, expr);
        searchFields.add(customField);
        return this;
    }

    public SearchFieldsBuilder add(NavigationItem navItem, BrightcoveLogic expr) {
        if (navItem != null) {
            searchFields.add(new CustomField(navItem.getCustomFieldName(),
                    navItem.getCustomFieldValue(), expr));
        }
        return this;
    }

    public List<BrightcovePair> getFields() {
        return searchFields;
    }

}
