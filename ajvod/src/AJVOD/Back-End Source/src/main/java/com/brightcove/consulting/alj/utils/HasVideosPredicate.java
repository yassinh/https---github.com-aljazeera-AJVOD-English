package com.brightcove.consulting.alj.utils;

import org.apache.commons.collections.Predicate;

import com.brightcove.consulting.alj.models.NavigationItem;

public class HasVideosPredicate implements Predicate {

    @Override
    public boolean evaluate(Object object) {
        if (!(object instanceof NavigationItem)) {
            return false;
        }

        return ((NavigationItem)object).hasVideos();
    }

}
