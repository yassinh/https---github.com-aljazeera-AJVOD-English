package com.brightcove.consulting.config.parser;

import org.apache.commons.digester3.Rule;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * Rule to setup a nav item as being part of a designated group in the root parent.
 *
 * @author ssayles
 */
public class SetGroupRule extends Rule {
    private String bodyText;

    public SetGroupRule() {
    }

    @Override
    public void body( String namespace, String name, String text ) {
        bodyText = text.trim();
        bodyText = StringUtils.capitalize(bodyText);
    }

    @Override
    public void end( String namespace, String name ) {
        if (bodyText != null && !StringUtils.EMPTY.equals(bodyText)) {
            // make sure it matches an actual type
            try {
                Class.forName(NavigationItem.class.getPackage().getName() +"."+ bodyText);
            } catch (ClassNotFoundException e) {
                getDigester().getLogger().error("Unknown group type: " + bodyText);
                return;
            }
            getDigester().push("groupKeys", bodyText);
        }
    }
}
