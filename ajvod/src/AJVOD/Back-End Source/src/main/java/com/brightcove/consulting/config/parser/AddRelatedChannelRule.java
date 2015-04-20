package com.brightcove.consulting.config.parser;

import org.apache.commons.digester3.Rule;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;

public class AddRelatedChannelRule extends Rule {

    private String bodyText;

    public AddRelatedChannelRule() {
    }

    @Override
    public void body( String namespace, String name, String text ) {
        bodyText = text.trim();
    }

    @Override
    public void end( String namespace, String name ) {
        if (bodyText != null && !StringUtils.isEmpty(bodyText)) {
            NavigationItem item = getDigester().peek();
            if (item instanceof Channel) {
                ((Channel)item).addRelatedChannel(bodyText);
            }
        }
    }
}
