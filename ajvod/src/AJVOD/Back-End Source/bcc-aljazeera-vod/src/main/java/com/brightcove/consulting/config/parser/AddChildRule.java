package com.brightcove.consulting.config.parser;

import static com.brightcove.consulting.util.WebUtils.slugify;

import org.apache.commons.digester3.SetNextRule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * Digester rule for adding a child navigation element. 
 * @author ssayles
 */
public class AddChildRule extends SetNextRule {
    public AddChildRule() {
        super("addChild");// means nothing
    }

    @Override
    public void begin( String namespace, String name, Attributes attributes ) throws Exception {
    }
    @Override
    public void end( String namespace, String name ) throws Exception {
        if (getDigester().getCount() <= 1) {
            return;
        }

        ConfigParser parser = getDigester().peek(getDigester().getCount() - 1);

        Object parent = getDigester().peek(1);
        Object child = getDigester().peek();
        if (!(child instanceof NavigationItem)) {
            return;
        }

        NavigationItem childNav = (NavigationItem) child;

        // ensure a key property is established if we're not the root object
        // we assume the key will be set on the root channel object
        // we're also assuming the root element is a channel
        // ... that's a lot of assumptions
        if (StringUtils.isEmpty(childNav.getKey()) && !(parent instanceof ConfigParser)) {
            String key = null;

            String fieldValue = childNav.getCustomFieldValue();
            if (!StringUtils.isEmpty(fieldValue)) {
                key = slugify(fieldValue);
            }
            
            if (StringUtils.isEmpty(key)) {
                // no key!
                getLogger().error("Unable to establish key for navigation item: " +
                        childNav.toString() + ". This item will be ignored.  "+
                        "Please ensure a <customFieldValue> or <key> is defined for this item in file: "+
                        parser.getCurrentFile().getAbsolutePath());
                return;
            } else {
                childNav.setKey(key);
            }
        }

        if (parent instanceof NavigationItem) {
            String groupTypeKey = getDigester().peek("groupType");
            if (groupTypeKey != null) {
                ((NavigationItem)parent).addChild(groupTypeKey, childNav);
            }
        } else if (parent instanceof ConfigParser) {
            ((ConfigParser)parent).addNavigationItem(childNav);
        }

        // see if we need to add this nav item to a group on the root parent
        if (!getDigester().isEmpty("groupKeys") && child instanceof NavigationItem) {
            String groupKey = getDigester().pop("groupKeys");
            // root object on digester object stack
            // we're expecting the first object to be the parser itself
            // and the second is the root nav item
            Object root = getDigester().peek(getDigester().getCount() - 2);
            if (root instanceof NavigationItem) {
                // navitem.addChild() sets the parent so don't use that here
                ((NavigationItem)root).getChildren().get(groupKey).put(childNav.getKey(), childNav);
            }
        }

        
    }

    private Logger getLogger() {
        // root object should be the parser itself
        ConfigParser parser = getDigester().peek(getDigester().getCount() - 1);
        return parser.getLogger();
    }
}