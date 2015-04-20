package com.brightcove.consulting.alj.utils;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.NavigationItem;

public class CustomFieldValuePredicate implements Predicate {

    private String fieldName;
    private String fieldValue;
    private Class<? extends NavigationItem> type;

    public CustomFieldValuePredicate(String fieldName, String fieldValue, Class<? extends NavigationItem> type) {
        this.fieldName = StringUtils.lowerCase(fieldName);
        this.fieldValue = StringUtils.lowerCase(fieldValue);
        this.type = type;
    }

    @Override
    public boolean evaluate(Object object) {
        if (!(object instanceof NavigationItem)) {
            return false;
        }
        if (type.isAssignableFrom(object.getClass())) {
            NavigationItem nav = (NavigationItem)object;
            String navFieldValue = StringUtils.lowerCase(nav.getCustomFieldValue());
            String navFieldName = StringUtils.lowerCase(nav.getCustomFieldName());
            
            return (StringUtils.equals(this.fieldName, navFieldName) && StringUtils.equals(this.fieldValue,navFieldValue));
        }
        return false;
    }

}
