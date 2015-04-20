package com.brightcove.consulting.alj.models;

import java.util.Map;

import com.brightcove.consulting.config.SpringContext;

/**
 * Container for an Al-Jazeera Topic construct. Encapsulates data relevant
 * to a given topic.
 * 
 * @author jmyatt
 *
 */
public class Topic extends BaseNavigationItem implements NewsClip {

	private String smallImageURL;

	private static final String resourceCollection = "topics";
	private static final String menuDisplayName = "Topics";

	public Topic() {
	    super();
	}

	public Topic(String pKey, String pName, String pDescription, String pImageURL, String pCustomFieldValue) {
		this.key = pKey;
		this.name = pName;
		this.description = pDescription;
		this.logoImageURL = pImageURL;
		this.customFieldValue = pCustomFieldValue;
	}
	
	public String getImageUrl(){
		return this.logoImageURL;
	}
	
	@Override
	public String getResourceCollection() {
		return resourceCollection;		
	}
	@Override
	public String getMenuDisplayName() {
		return menuDisplayName;		
	}

	public String getSmallImageURL() {
		return smallImageURL;
	}
	public void setSmallImageURL(String str) {
		this.smallImageURL = str;
	}

    @SuppressWarnings("unchecked")
    public Map<String, Topic> getSubTopics() {
        return (Map<String, Topic>) getChildren(this.getClass().getSimpleName());
    }

    @Override
    public NavigationTypeEnum getNavigationType() {
        return NavigationTypeEnum.Topic;
    }

    @Override
    protected String getDefaultCustomFieldName() {
        return (String) SpringContext.getBean("topicCustomField");
    }
}
