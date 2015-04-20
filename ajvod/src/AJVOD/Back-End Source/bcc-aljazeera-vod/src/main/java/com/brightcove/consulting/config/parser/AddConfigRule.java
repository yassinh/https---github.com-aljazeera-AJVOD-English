package com.brightcove.consulting.config.parser;

import org.apache.commons.digester3.SetNextRule;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * Digester rule for adding a child navigation element. 
 * @author ssayles
 */
public class AddConfigRule extends SetNextRule {

    private String bodyText;

    public AddConfigRule() {
        super("addConfig");// means nothing
    }

    @Override
    public void body( String namespace, String name, String text ) {
        bodyText = text.trim();
        bodyText = StringUtils.capitalize(bodyText);
    }

    @Override
    public void end( String namespace, String name ) {
        Object object = getDigester().peek();
        if (object instanceof NavigationItem) {
            ((NavigationItem)object).addConfig(name,bodyText);
        }
    }

}