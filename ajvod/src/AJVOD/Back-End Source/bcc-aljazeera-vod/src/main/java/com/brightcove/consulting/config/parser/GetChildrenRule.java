package com.brightcove.consulting.config.parser;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * Digester rule used to help assign nested child navigation items into the
 * correct grouping.
 * @author ssayles
 */
public class GetChildrenRule extends Rule {
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String groupKey = attributes.getValue("type");
        getDigester().push("groupType", groupKey);
    }
    @Override
    public void end(String namespace, String name) throws Exception {
        getDigester().pop("groupType");
    }

}