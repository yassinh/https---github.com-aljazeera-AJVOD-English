package com.brightcove.consulting.alj.models;

import static com.brightcove.consulting.config.YBTVConfigHelper.navUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.config.SpringContext;

/**
 * Container for a generic Al-Jazeera Site Navigation construct. 
 * 
 * @author jmyatt
 *
 */
public abstract class BaseNavigationItem implements NavigationItem {

    // The key (id) for this item to be used in URLs and lookups
	protected String key;
	// The display name to be used in the view
	protected String name;
	// The description for this item
	protected String description;
	// The URL (relative or absolute) of the display logo for this item
	protected String logoImageURL;
	protected String backgroundImageURL;
    // The URL (relative or absolute) of the large image for this item
    protected String largeImageURL;
	// The associated custom field value representing this item in video metadata
	protected String customFieldValue;
	// The associated custom field name representing this item in video metadata
	protected String customFieldName;
	// The parent navigation item.
	protected NavigationItem parent;
	// The id for the featured playlist
	protected String featuredPlaylistId;
	// Optional configuration settings
	protected Map<String,String> config;


    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, NavigationItem>> children = 
            MapUtils.lazyMap(new HashMap<String, Map<String, NavigationItem>>(), navChildrenFactory);

	// used in the lazy construction of navChildren maps.
	protected static final Factory navChildrenFactory = new Factory() {
        @Override
        public Object create() {
            return new HashMap<String, NavigationItem>();
        }
	};

	public BaseNavigationItem() {
	}

	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getResourceCollection()
     */
	@Override
    public abstract String getResourceCollection() ;
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getMenuDisplayName()
     */
	@Override
    public abstract String getMenuDisplayName() ;

	/**
	 * This should support any additional configurations that may be needed.
	 * Just keep in mind that this should be considered only suitable 
	 * for data that is not specific to what the NavigationItem represents from
	 * the perspective of the domain model. For example, 'twitter account id'
	 * for the config might be appropriate, but something like
	 * 'channel affiliate name' would not.
	 */
	public void addConfig(String name, String value) {
	    if (this.config == null) {
	        this.config = new HashMap<String,String>();
	    }
	    this.config.put(name, value);
	}

	/**
	 * Return any value associated with the config name.
	 *
	 * @name Case sensitive name for a config value.
	 * @return Config value or null.
	 */
	public String getConfig(String name) {
	    if (this.config != null) {
	        return this.config.get(name);
	    }
	    return null;
	}

	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#addChild(java.lang.String, com.brightcove.consulting.alj.models.BaseNavigationItem)
     */
	@Override
    public void addChild(String groupKey, NavigationItem navItem) {
	    if (navItem == null || navItem.getKey() == null) {
	        throw new IllegalArgumentException("A child item must be supplied with a key.");
	    }
	    if (navItem == this) {
	        throw new IllegalArgumentException("A navigation item cannot be added to itself as a child.");
	    }
	    Map<String, NavigationItem> group = children.get(groupKey);
	    group.put(navItem.getKey(), navItem);
	    navItem.setParent(this);
	}

	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getChildren(java.lang.String)
     */
	@Override
    public Map<String, ? extends NavigationItem> getChildren(String groupKey) {
	    Map<String, NavigationItem> group = children.get(groupKey);
	    return group;
	}
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getChildren(java.lang.Class)
     */
    @Override
    public Map<String, ? extends NavigationItem> getChildren(Class<? extends NavigationItem> groupKey) {
        Map<String, NavigationItem> group = children.get(groupKey.getSimpleName());
        return group;
    }

	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getChildren()
     */
	@Override
    public Map<String, Map<String, NavigationItem>> getChildren() {
        return children;
    }
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setChildren(java.util.Map)
     */
    @Override
    public void setChildren(Map<String, Map<String, NavigationItem>> children) {
        this.children = children;
    }
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getKey()
     */
    @Override
    public String getKey() {
		return key;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setKey(java.lang.String)
     */
	@Override
    public void setKey(String str) {
		this.key = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getName()
     */
	@Override
    public String getName() {
		return name;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setName(java.lang.String)
     */
	@Override
    public void setName(String str) {
		this.name = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getFeaturedPlaylistId()
     */
	@Override
    public String getFeaturedPlaylistId() {
		return featuredPlaylistId;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setFeaturedPlaylistId(java.lang.String)
     */
	@Override
    public void setFeaturedPlaylistId(String str) {
		this.featuredPlaylistId = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getDescription()
     */
	@Override
    public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setDescription(java.lang.String)
     */
	@Override
    public void setDescription(String str) {
		this.description = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getLogoImageURL()
     */
	@Override
    public String getLogoImageURL() {
		return logoImageURL;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setLogoImageURL(java.lang.String)
     */
	@Override
    public void setLogoImageURL(String str) {
		this.logoImageURL = str;
	}
     /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getBackgroundImageURL()
     */
	@Override
    public String getBackgroundImageURL() {
		return backgroundImageURL;
	}
     /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setBackgroundImageURL(java.lang.String)
     */
	@Override
    public void setBackgroundImageURL(String str) {
		this.backgroundImageURL = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getLargeImageURL()
     */
	@Override
    public String getLargeImageURL() {
        return largeImageURL;
    }
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setLargeImageURL(java.lang.String)
     */
    @Override
    public void setLargeImageURL(String largeImageURL) {
        this.largeImageURL = largeImageURL;
    }
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getCustomFieldValue()
     */
    @Override
    public String getCustomFieldValue() {
		return customFieldValue;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setCustomFieldValue(java.lang.String)
     */
	@Override
    public void setCustomFieldValue(String str) {
		this.customFieldValue = str;
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getCustomFieldName()
     */
	@Override
    public String getCustomFieldName() {
	    if (this.customFieldName != null) {
	        return this.customFieldName;
	    } else {
	        return getDefaultCustomFieldName();
	    }
	}
	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setCustomFieldName(java.lang.String)
     */
	@Override
    public void setCustomFieldName(String str) {
	    this.customFieldName = str;
	}
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getParent()
     */
    @Override
    public NavigationItem getParent() {
        return parent;
    }
    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#setParent(com.brightcove.consulting.alj.models.NavigationItem)
     */
    @Override
    public void setParent(NavigationItem parent) {
        if (parent == this) {
            throw new IllegalArgumentException("A navigation item cannot be set as it's own parent.");
        }
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#hasVideos()
     */
    @Override
    public boolean hasVideos() {
        return navUtils().hasVideos(this);
    }

    /**
     * Alias for {@link #hasVideos()}.
     */
    // because jstl is lame
    public boolean getHasVideos() {
        return hasVideos();
    }

	/**
	 * All NavigationItems have a customFieldName matching video metadata.
	 * @return The default custom field name.
	 */
	protected abstract String getDefaultCustomFieldName();

	/* (non-Javadoc)
     * @see com.brightcove.consulting.alj.models.NavigationItem#getNavigationType()
     */
	@Override
    public abstract NavigationTypeEnum getNavigationType();

	protected NavRepository getNavRepository() {
	    return (NavRepository) SpringContext.getBean("navRepository");
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((customFieldName == null) ? 0 : customFieldName.hashCode());
        result = prime
                * result
                + ((customFieldValue == null) ? 0 : customFieldValue.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseNavigationItem other = (BaseNavigationItem) obj;
        if (customFieldName == null) {
            if (other.customFieldName != null)
                return false;
        } else if (!customFieldName.equals(other.customFieldName))
            return false;
        if (customFieldValue == null) {
            if (other.customFieldValue != null)
                return false;
        } else if (!customFieldValue.equals(other.customFieldValue))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String toString() {
        String text = "{";
        text += "key: "+ nullOrJs(this.key);
        text += ", name: "+ nullOrJs(this.name);
        text += ", description: "+ nullOrJs(this.description);
        text += ", logoImageURL: "+ nullOrJs(this.logoImageURL);
        text += ", customFieldValue: "+ nullOrJs(this.customFieldValue);
        text += ", customFieldName: "+ nullOrJs(this.getCustomFieldName());
        Set<String> groups = getChildren().keySet();
        if (!groups.isEmpty()) {
            Collection groupNames = CollectionUtils.collect(groups, new Transformer() {
                public Object transform(Object input) {
                    return BaseNavigationItem.this.nullOrJs((String)input);
                }
            });
            text += ", childGroups: ["+ StringUtils.join(groupNames, ',') + "]"; 
        }

        text += "}";
        return text;
    }
    private String nullOrJs(String value) {
	    if (value == null) {
	        return "null";
	    } else {
	        return "'" + StringEscapeUtils.escapeJavaScript(value) + "'";
	    }
	}
}
