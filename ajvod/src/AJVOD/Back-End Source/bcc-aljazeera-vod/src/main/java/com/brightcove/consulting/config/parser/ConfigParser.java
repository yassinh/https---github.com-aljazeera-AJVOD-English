package com.brightcove.consulting.config.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ExtendedBaseRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.apache.commons.digester3.RegexRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.brightcove.consulting.AllShowsController;
import com.brightcove.consulting.alj.models.NavigationItem;

/**
 * Handles parsing and unmarshalling of navigation configuration XML into 
 * NavigationItem objects.  All top level &lt;item&gt; elements are returned as
 * a collection from the {@link #parseConfig(File)} method.
 * 
 * @author ssayles
 */
public class ConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(AllShowsController.class);


    private List<NavigationItem> items = new ArrayList<NavigationItem>();
    private Digester digester;


    private File currentFile;

    /**
     * Parses the given file with the assumption that it contains XML for navigation
     * configuration.
     */
    public List<NavigationItem> parseConfig(File configFile) throws IOException, SAXException {
        initDigester();
        getLogger().debug("Parsing navigation configuration file: " + configFile.getAbsolutePath());
        digester.parse(configFile);
        currentFile = configFile;
        return items;
    }

    public void clear() {
        digester.clear();
        items.clear();
        currentFile = null;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    private void initDigester() {
        digester = new Digester();
        digester.setRules(new ExtendedBaseRules());

        digester.setValidating(false);

        digester.push(this);
        digester.addFactoryCreate("*/item", NavItemFactory.class);
        digester.addBeanPropertySetter("*/item/key");
        digester.addBeanPropertySetter("*/item/description");
        digester.addBeanPropertySetter("*/item/logoImageURL");
        digester.addBeanPropertySetter("*/item/name");
        digester.addBeanPropertySetter("*/item/rtl"); // only for channel
        digester.addBeanPropertySetter("*/item/language");// only for channel
        digester.addBeanPropertySetter("*/item/epgFeedUrl");// only for channel
        digester.addRule("*/item/group", new SetGroupRule());
        digester.addBeanPropertySetter("*/item/featuredPlaylistId");
        digester.addBeanPropertySetter("*/item/customFieldName");
        digester.addBeanPropertySetter("*/item/customFieldValue");
        digester.addBeanPropertySetter("*/item/backgroundImageURL");
        digester.addRule("*/item", new AddChildRule());
        digester.addRule("*/config/?", new AddConfigRule());

        digester.addRule("*/item/children", new GetChildrenRule());

        digester.addRule("*/item/relatedChannels/channel", new AddRelatedChannelRule());
    }

    /**
     * Callback used by the AddChildRule to pass root level navigation items.
     * Note that root level is simply a first level item element in the xml.
     *
     * @param navItem A root level NavigationItem.
     */
    protected void addNavigationItem(NavigationItem navItem) {
        logger.debug("Added root nav item: " + navItem);
        items.add(navItem);
    }

    public static void main(String[] args) throws IOException, SAXException {
        File file = new File("/Users/ssayles/src/Al-Jazeera-VOD/bcc-aljazeera-vod/src/main/resources/channels/ara/channel-config.xml");
        ConfigParser parser = new ConfigParser();
        List<NavigationItem> results = parser.parseConfig(file);
        System.out.println(results);
    }
}

