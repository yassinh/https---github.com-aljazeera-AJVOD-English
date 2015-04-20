package com.brightcove.consulting.api.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.consulting.util.WebUtils;

/**
 * 
 * The BrightcoveVideo object encapsulates basic display video functionality. For every client
 * this look and feel might be modified, but the objects main purpose is to provide a simple
 * display concept that is seen in many of the YBTV/Pages implementations. When a project
 * requires mapping of different types -- ie, custom fields, tags, etc. The mapping and formatting
 * of the video response should take place within this DTO. All Brightcove Videos should implement
 * a ItemCollection, {@link ItemCollection} so that they can be returned as part of a Brightcove api
 * response.
 * 
 * @author woladehin
 *
 */
public class BrightcoveVideo implements ItemCollection, Comparable<BrightcoveVideo> {
    
	private static final Logger logger = LoggerFactory.getLogger(BrightcoveVideo.class);

    private String name;
    private long id;
    private String duration;
    private String referenceId;
    private String shortDescription;
    private String longDescription;
    private Date date;
    private String linkUrl;
    private String linkText;
    private String imageUrl;
    private String thumbnailUrl;
    private long videoLength;
    private long views;
    private Map<String, String> customFields;
    private Date lastModifiedDate;

    private Date publishDate;
    
    /**
     * 
     * Default constructor
     *
     */
    public BrightcoveVideo( ) {
        
    }

    /**
     * Constructs this instance by populating fields from the given video.
     *
     * @param video The source video.
     */
    public BrightcoveVideo(Video video) {
        id = video.getId();
        referenceId = video.getReferenceId( ); 
        shortDescription = video.getShortDescription( );
        longDescription = video.getLongDescription( ); 
        date = video.getCreationDate( );
        linkUrl = video.getLinkUrl( );
        linkText = video.getLinkText( ); 
        imageUrl = video.getVideoStillUrl( );
        thumbnailUrl = video.getThumbnailUrl();
        videoLength = video.getLength( );
        name = video.getName( );
        views = video.getPlaysTotal() != null ? video.getPlaysTotal().intValue() : 0;
        duration = String.valueOf((video.getLength()/1000));

        this.customFields = new HashMap<String, String>();
        for(CustomField cf : video.getCustomFields()) {
            this.customFields.put(cf.getName(), cf.getValue());
        }
        this.date = parseOriginalDate(referenceId);

        lastModifiedDate = video.getLastModifiedDate();
        publishDate = video.getPublishedDate();
    }

    /**
     * 
     * The base constructor that can and should be overridden for updating video content.
     * The base constructor sets all of the display fields for videos currently. In later
     * implementations/extentions, other formatting measures may be taken--for example setting
     * all Brightcove fields then calling "format" to format at the end.
     * 
     * @param id the Brightcove id for a video
     * @param referenceId the Brightcove reference id that is manually created by the user
     * @param shortDescription the short description of the video
     * @param longDescription the long description of the video
     * @param date the date that should be displayed in the pages experience (date added,
     * date modified, etc)
     * @param linkUrl the associated related url for a video
     * @param linkText the associated related text for a video
     * @param imageUrl the thumbnail or video still to use for this video
     * @param videoLength the length of the video
     * @param views the number of views for this video (either plays trailing week or total)
     * @param name the name of the video
     * 
     */
    @Deprecated
    public BrightcoveVideo( long id, String referenceId, String shortDescription, String longDescription, 
                    Date date, String linkUrl, String linkText, String imageUrl, long videoLength, 
                    long views, String name, List<CustomField> customFields ) {
        
        this.id = id;
        this.referenceId = referenceId;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.date = date;
        this.linkText = linkText;
        this.linkUrl = linkUrl;
        this.imageUrl = imageUrl;
        this.videoLength = videoLength;
        this.views = views;
        this.name = name;
        this.duration = String.valueOf(videoLength/1000);
        
        if(customFields != null)
        {
        	this.customFields = new HashMap<String, String>();
        	for(CustomField cf : customFields)
        		this.customFields.put(cf.getName(), cf.getValue());
        }
        this.date = parseOriginalDate(referenceId);
    }


    /****************************** Getters ******************************/
    
    public String getName( ) {
        return name;
    }

    public long getId( ) {
        return id;
    }

    public String getReferenceId( ) {
        return referenceId;
    }

    public String getShortDescription( ) {
        return shortDescription;
    }

    public String getLongDescription( ) {
        return longDescription;
    }
    public Date getDate() {
    	return date;
    }

    public String getReleaseDate( ) {
        return new SimpleDateFormat("yyyy-mm-dd").format(date);
    }

    public String getSortableDate( ) {
    	return String.valueOf(date.getTime());
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public String getLinkUrl( ) {
        return linkUrl;
    }


    public String getLinkText( ) {
        return linkText;
    }

    public String getImageUrl( ) {
        return imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Long getVideoLength( ) {
        return videoLength;
    }


    public Long getViews( ) {
        return views;
    }

    public String getDuration(){
    	return duration;
    }

    public String getDisplayLength() {
    	//initialize time properties 
    	long seconds = videoLength / 1000;//converts ms to sec
    	String hours = String.valueOf(seconds / 3600);
    	String min = String.valueOf((seconds % 3600) / 60);
    	String sec = String.valueOf((seconds % 3600) % 60);
    	
    	if(min.length() < 2)
    		min = "0" + min;
    	if(sec.length() < 2)
    		sec = "0" + sec;   	
    	
    	if(!hours.equals("0")){
    		if(hours.length() < 2)
    			hours = "0" + hours;
    		
    		return hours + ":" + min + ":" + sec;   		
    	}
    	
    	return min + ":" + sec;
    }

    public Map<String, String> getCustomFields() {
    	return customFields;
    }
    
    public String getCustomField(String key)
    {
    	if(key == null || key.length() < 1 || customFields == null || !customFields.containsKey(key))
    		return null;
    	
    	return customFields.get(key);
    }

    public String getShowText() {
        String showText = null;
    	if ("programme".equals(getCustomField("video_type"))
    			|| "programme clip".equals(getCustomField("video_type"))
    			||"promo".equals(getCustomField("video_type"))) {
    		showText = getCustomField("programme");
    	}
    	else if (getCustomField("aj_category") != null) {
    		showText = getCustomField("aj_category");
    	}
    	if (showText != null) {
    	    if (showText.toUpperCase().equals("--- N/A ---")) {
    	        showText = null;
    	    }
    	}
    	return showText;
    }

    public String getShowLink() {
        String showText = getShowText();
        if (showText == null) {
            return "#";
        }
    	if ("programme".equals(getCustomField("video_type"))
    			|| "programme clip".equals(getCustomField("video_type"))
    			||"promo".equals(getCustomField("video_type"))) {
    	    return "/channels/" + getCustomField("channel") + "/shows/" + WebUtils.slugify(showText);
    	}
    	else if (getCustomField("aj_category") != null) {
    		return "/channels/" + getCustomField("channel") + "/topics/" + WebUtils.slugify(showText);    		
    	}
    	else {
    		return "#";
    	}
    }
    public String getSlug() {
    	return WebUtils.slugify(name);
    }
    
	public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
	public int compareTo(BrightcoveVideo arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Date parseOriginalDate(String refId) {
		Date origDate = this.date;
		// Get the date from the reference ID
    	if (refId != null && refId.length() > 17) {
    		String rawDate = refId.substring(0, 17);
    		try {
    			DateFormat df = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    			origDate = df.parse(rawDate);
    		}
    		catch (ParseException e) {
    			// Unable to parse date; use BCOV date
    			logger.warn("Unable to parse date from reference ID; using VideoDTO Created Date");
    		}
    	}
    	return origDate;
	}

}
