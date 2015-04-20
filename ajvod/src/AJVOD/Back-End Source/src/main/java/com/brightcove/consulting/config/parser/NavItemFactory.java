package com.brightcove.consulting.config.parser;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * A factory used by Digester to create NavigationItem instances.
 *
 * @author ssayles
 */
public class NavItemFactory implements ObjectCreationFactory<NavigationItem> {

    private Digester digester;


    @Override
    public NavigationItem createObject(Attributes attributes)
            throws Exception {
        
        // if there is a groupType, then we are creating a child of a specified
        // type in the parent <children> element.
        // we'll be ignoring any specified type in the item
        String groupType = null;
        if (!getDigester().isEmpty("groupType")) {
            groupType = this.getDigester().peek("groupType");
        }

        NavigationItem navItem;
        if (groupType != null) {
            navItem = createInstance(groupType);
        } else {
            // this is a top level NavigationItem so it should have a 'type' attribute
            String typeName = attributes.getValue("type");
            if (typeName == null) {
                throw new IllegalStateException("Top level item element does not have a type attribute.");
            }
            navItem = createInstance(typeName);
        }
        return navItem;
    }

    @Override
    public Digester getDigester() {
        return this.digester;
    }

    @Override
    public void setDigester(Digester digester) {
        this.digester = digester;
    }
    
    @SuppressWarnings("unchecked")
    private NavigationItem createInstance(String classShortName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String className = "com.brightcove.consulting.alj.models."+classShortName;
        Class<NavigationItem> clazz = (Class<NavigationItem>) Class.forName(className);
        return clazz.newInstance();
    }
}