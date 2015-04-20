package com.brightcove.consulting.alj.models;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.brightcove.consulting.config.parser.ConfigParser;
import com.brightcove.consulting.util.FileScanner;
import com.brightcove.consulting.util.ScanReceiver;


/**
 * A NavRespository is responsible for maintaining and providing NavigationItems
 * that have been parsed from configuration files.  The lifecycle method
 * {@link NavRepository#init()} should be called after instantiation and before
 * use.
 *
 * @author ssayles
 */
public class NavRepository {

    private static final Logger logger = LoggerFactory.getLogger(NavRepository.class);

    /** Whether or not the NavRepository has been initialized. */
    private volatile boolean initialized;

    /** Channels keyed by their id.*/
    private Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    @Autowired
    protected FileScanner channelConfigScanner;

    public NavRepository() {
    }

    /**
     * Performs initial loading of navigation configuration.  This should only
     * be called once per instance.
     */
    @PostConstruct
    public void init() {
        if (initialized) {
            logger.warn("init called on NavRepository but it has already been initialized.");
            return;
        }

        channelConfigScanner.register(new ScanReceiver() {
            public void filesUpdated(Collection<File> files) {
                loadChannelConfigurations(files);
            }
        });

        checkForUpdates();

        initialized = true;
}

    /**
     * Reloads all configurations.
     * 
     * @TODO: this can miss removed channels.
     */
    public synchronized void refresh() {
        channelConfigScanner.clear();
        checkForUpdates();
    }


    /**
     * Returns all channels indexed by their key.
     *
     * @return Map of channel ids to Channels.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Channel> getChannels() {
        return MapUtils.unmodifiableMap(channels);
    }

    public Channel getChannel(String key) {
    	
    	if(StringUtils.isEmpty(key))
    		return null;
    	
    	for(Channel channel : channels.values()){
    		String channelLocale = channel.getConfig("locale").toLowerCase();
        	
    		if(channelLocale.startsWith(key)){
        		return channel;
        	}
        }
    	
    	if (key != null && key.length() > 0) { 
            return channels.get(key);
        }
        
        return null;
    }

    /**
     * Attempts to look for any configuration file updates and process them.
     */
    //@Scheduled(fixedDelay=60000)
    public void checkForUpdates() {
        logger.debug("Scanning for configuration updates.");
        channelConfigScanner.performScan();
    }

    private synchronized void loadChannelConfigurations(Collection<File> files) {
        ConfigParser parser = new ConfigParser();
        int removed = 0;
        int updated = 0;
        int added = 0;
        for (File file : files) {
            String channelId = getChannelIdFromConfigPath(file);
            // if the file doesn't exist, then the configuration was possibly removed
            if (!file.exists()) {
                channels.remove(channelId);
                removed += 1;
                continue;
            }
            try {
                List<NavigationItem> configurations = parser.parseConfig(file);
                if (configurations.size() > 1) {
                    logger.warn("Channel configuration file "+ file.getAbsolutePath() + " contains more than one top level item.");
                }
                if (configurations.isEmpty()) {
                    logger.warn("Channel configuration file " + file.getAbsolutePath() + " does not contain a channel configuration.");
                    continue;
                }
                NavigationItem baseNavigationItem = configurations.get(0);
                if (baseNavigationItem instanceof Channel) {
                    if (channels.containsKey(channelId)) {
                        updated += 1;
                    } else {
                        added += 1;
                    }
                    baseNavigationItem.setKey(channelId);
                    channels.put(channelId, (Channel)baseNavigationItem);
                } else {
                    logger.warn("Channel configuration file " + file.getAbsolutePath() + " does not contain a top level Channel item.");
                }
            } catch (Exception e) {
                logger.error("Unable to load configuration from " + file.getAbsolutePath());
                logger.error("Exception occurred while parsing file.", e);
            }
            parser.clear();
        }
    }

    private String getChannelIdFromConfigPath(File configFile) {
        // get the parent directory name that represents the channel id
        String path = configFile.getAbsolutePath();
        // filename
        path = path.substring(0, path.lastIndexOf(File.separatorChar));
        // directory name
        String channelId = path.substring(path.lastIndexOf(File.separatorChar)+1);
        return channelId;
    }


}


