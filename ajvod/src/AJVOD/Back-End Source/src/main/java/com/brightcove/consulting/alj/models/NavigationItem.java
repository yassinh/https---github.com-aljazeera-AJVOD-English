package com.brightcove.consulting.alj.models;

import java.util.Map;

/**
 * 
 * @author ssayles
 */
public interface NavigationItem {

    public abstract String getResourceCollection();

    public abstract String getMenuDisplayName();

    /**
     * Adds a child NavigationItem to the given group by name and establish the
     * instance as the parent of the given child.
     *
     * @param groupKey Name of the group to add to.
     * @param navItem The NavigationItem to add.
     */
    public abstract void addChild(String groupKey, NavigationItem navItem);

    /**
     * Returns a subset of children associated with the given group.
     *
     * @param groupKey The name of the child group.
     * @return A map of child navigation items indexed by their key.
     */
    public abstract Map<String, ? extends NavigationItem> getChildren(
            String groupKey);

    /**
     * Returns a subset of children associated with the given group identity.
     *
     * @param groupKey The name of the child group.
     * @return A map of child navigation items indexed by their key.
     */
    public abstract Map<String, ? extends NavigationItem> getChildren(Class<? extends NavigationItem> groupKey);

    /**
     * Returns all children associated with this instance.
     * @return A map of child NavigationItems keyed by a group name.
     */
    public abstract Map<String, Map<String, NavigationItem>> getChildren();

    public abstract void setChildren(
            Map<String, Map<String, NavigationItem>> children);

    public abstract String getKey();

    public abstract void setKey(String str);

    public abstract String getName();

    public abstract void setName(String str);

    public abstract String getFeaturedPlaylistId();

    public abstract void setFeaturedPlaylistId(String str);

    public abstract String getDescription();

    public abstract void setDescription(String str);

    public abstract String getLogoImageURL();

    public abstract String getBackgroundImageURL();
    
    public abstract void setBackgroundImageURL(String str);
    
    public abstract void setLogoImageURL(String str);

    public abstract String getLargeImageURL();

    public abstract void setLargeImageURL(String largeImageURL);

    public abstract String getCustomFieldValue();

    public abstract void setCustomFieldValue(String str);

    public abstract String getCustomFieldName();

    public abstract void setCustomFieldName(String str);

    public abstract NavigationItem getParent();

    public abstract void setParent(NavigationItem parent);

    /**
     * Subclasses should be mapped to a NavigationTypeEnum.
     * @return A NavigationTypeEnum value.
     */
    public abstract NavigationTypeEnum getNavigationType();

    /**
     * Tests whether or not the navigation item has associated videos. This is
     * mainly used to help filter out things like shows or show genres that may
     * not actually have any videos yet.
     *
     * @return <code>true</code> if there are associated videos.
     */
    public abstract boolean hasVideos();

    /**
     * Adds additional configuration properties.
     *
     * @param name
     * @param value
     */
    public abstract void addConfig(String name, String value);

    public abstract String getConfig(String name);
}
