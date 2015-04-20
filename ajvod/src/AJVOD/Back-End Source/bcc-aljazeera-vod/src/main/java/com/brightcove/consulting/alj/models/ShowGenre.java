package com.brightcove.consulting.alj.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.brightcove.consulting.alj.utils.HasVideosPredicate;
import com.brightcove.consulting.config.SpringContext;

/**
 * Container for an Al-Jazeera Show Genre construct. Encapsulates data relevant
 * to a given genre (show type).
 * 
 * @author jmyatt
 *
 */
public class ShowGenre extends BaseNavigationItem {

	private static final String resourceCollection = "genres";
	private static final String menuDisplayName = "Shows";


	public ShowGenre() {
	    super();
	}

	public ShowGenre(String pKey, String pName, String pDescription, String pImageURL, String pCustomFieldValue) {
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

	/**
	 * Returns only shows that have videos.
	 * @return Shows mapped by key.
	 */
	@SuppressWarnings("unchecked")
    public Map<String, Show> getShowsWithVideos() {
	    Map<String, Show> showsMap = (Map<String, Show>) this.getChildren(Show.class);
	    Map<String, Show> returnValue = new HashMap<String,Show>();

	    Collection<Show> shows = CollectionUtils.select(showsMap.values(), new HasVideosPredicate());

	    for (Show show : shows) {
            if (show.hasVideos()) {
                returnValue.put(show.getKey(), show);
            }
        }
	    return returnValue;
	}

    @Override
    public NavigationTypeEnum getNavigationType() {
        return NavigationTypeEnum.ShowGenre;
    }

    @Override
    protected String getDefaultCustomFieldName() {
        return (String) SpringContext.getBean("showGenreCustomField");
    }
}
