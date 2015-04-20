package com.brightcove.consulting.alj.models;

import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.config.SpringContext;

/**
 * Container for an Al-Jazeera Show construct. Encapsulates data relevant
 * to a given show (programme).
 * 
 * @author jmyatt
 *
 */
public class Show extends BaseNavigationItem {

	private static final String resourceCollection = "shows";
	private static final String menuDisplayName = "Shows";

    public Show() {
        super();
    }

	public Show(String pKey, String pName, String pDescription, String pImageURL, String pCustomFieldValue) {
		this.key = pKey;
		this.name = pName;
		this.description = pDescription;
		this.logoImageURL = pImageURL;
		this.customFieldValue = pCustomFieldValue;
	}

	/**
	 * Returns the channel this show belongs to.
	 * @return The parent channel or null.
	 */
	public Channel getChannel() {
	    return NavUtils.findAncestor(this, Channel.class);
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

	public ShowGenre getShowGenre() {
	    return NavUtils.findAncestor(this, ShowGenre.class);
	}

    @Override
    public NavigationTypeEnum getNavigationType() {
        return NavigationTypeEnum.Show;
    }

    @Override
    protected String getDefaultCustomFieldName() {
        return (String) SpringContext.getBean("showCustomField");
    }
}
