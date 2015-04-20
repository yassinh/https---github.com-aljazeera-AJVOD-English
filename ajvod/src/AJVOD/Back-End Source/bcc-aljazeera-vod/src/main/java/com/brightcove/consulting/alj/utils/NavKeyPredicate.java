package com.brightcove.consulting.alj.utils;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * Simple predicate that evaluates whether or not a given BaseNavigationItem has
 * a matching &quot;key&quot; value.
 *
 * @author ssayles
 *
 */
public class NavKeyPredicate implements Predicate {

    private String key;
    public NavKeyPredicate(String key) {
        this.key = key;
    }
    @Override
    public boolean evaluate(Object object) {
        return StringUtils.equals(((NavigationItem)object).getKey(),key); 
    }

    public static NavKeyPredicate matchKey(String key) {
        return new NavKeyPredicate(key);
    }
}
