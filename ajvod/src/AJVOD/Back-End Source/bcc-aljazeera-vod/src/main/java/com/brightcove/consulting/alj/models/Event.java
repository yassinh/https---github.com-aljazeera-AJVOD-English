package com.brightcove.consulting.alj.models;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Event extends BaseNavigationItem {

    private static final Logger logger = LoggerFactory.getLogger(Event.class);

    private static final String resourceCollection = "events";
    private static final String menuDisplayName = "Events";


    public Date getEventDate() {
        return null;
    }

    @Override
    public String getResourceCollection() {
        return resourceCollection;
    }

    @Override
    public String getMenuDisplayName() {
        return menuDisplayName;
    }

    @Override
    protected String getDefaultCustomFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigationTypeEnum getNavigationType() {
        // TODO Auto-generated method stub
        return null;
    }

}
