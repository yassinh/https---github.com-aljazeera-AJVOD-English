package com.brightcove.consulting.alj.models;


import static com.brightcove.consulting.alj.utils.NavKeyPredicate.matchKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.SetUtils;

import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.config.SpringContext;

/**
 * Container for an Al-Jazeera Channel construct. Encapsulates data relevant
 * to a given channel.
 * 
 * @author jmyatt
 *
 */
public class Channel extends BaseNavigationItem {
	
    private static final String resourceCollection = "channels";
    private static final String menuDisplayName = "Channel";
    
    private String language;

	private Set<String> relatedChannelIds = new HashSet<String>(); 
	// cached set of related channels
	private Set<Channel> relatedChannels = null;

	private String epgFeedUrl = null;

	private boolean rtl;

	public Channel(String pKey, String pName, String pDescription, String pImageURL, String pCustomFieldValue, String pBackgroundURL) {
		this.key = pKey;
		this.name = pName;
		this.description = pDescription;
		this.logoImageURL = pImageURL;
		this.backgroundImageURL = pBackgroundURL;
		this.customFieldValue = pCustomFieldValue;
	}
	
	public Channel() {
        super();
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean getRtl() {
    	return rtl;
    }

    public void setRtl(boolean rtl) {
    	this.rtl = rtl;
    }

	@SuppressWarnings("unchecked")
    public Set<Topic> getTopics() {
	    Map<String, Topic> children = (Map<String, Topic>) getChildren(Topic.class);
	    if (children.isEmpty()) {
	        return SetUtils.EMPTY_SET;
	    } else {
	        return SetUtils.unmodifiableSet(new HashSet<Topic>(children.values()));
	    }
	}

	public Show getShow(String showId) {
	    return NavUtils.findByPath(this, showId, ShowGenre.class, Show.class);
	}

	@SuppressWarnings("unchecked")
    public Set<Topic> getSubTopics() {
	    Set<Topic> topics = getTopics();
	    if (topics.isEmpty()) {
	        return SetUtils.EMPTY_SET;
	    }
	    
	    Set<Topic> subTopics = new HashSet<Topic>();
	    for (Topic topic : topics) {
            subTopics.addAll(topic.getSubTopics().values());
        }
	    return subTopics;
	}

	@Override
	public String getResourceCollection() {
		return resourceCollection;		
	}
	@Override
	public String getMenuDisplayName() {
		return menuDisplayName;		
	}
	
    @Override
    public NavigationTypeEnum getNavigationType() {
        return NavigationTypeEnum.Channel;
    }

    @Override
    protected String getDefaultCustomFieldName() {
        return (String) SpringContext.getBean("channelCustomField");
    }

    public void addRelatedChannel(String channelId) {
        relatedChannelIds.add(channelId);
    }

    @SuppressWarnings("unchecked")
    public Set<Channel> getRelatedChannels() {
        if (relatedChannels != null) {
            return relatedChannels;
        }
        if (getNavRepository() != null && !relatedChannelIds.isEmpty()) {
            Set<Channel> channels = new HashSet<Channel>();
            for (String id : relatedChannelIds) {
                Channel channel = getNavRepository().getChannel(id);
                if (channel != null) {
                    channels.add(channel);
                }
            }
            this.relatedChannels = channels;
            return this.relatedChannels;
        }
        return SetUtils.EMPTY_SET;
    }

	public String getEpgFeedUrl() {
		return epgFeedUrl;
	}

	public void setEpgFeedUrl(String epgFeedUrl) {
		this.epgFeedUrl = epgFeedUrl;
	}

}
