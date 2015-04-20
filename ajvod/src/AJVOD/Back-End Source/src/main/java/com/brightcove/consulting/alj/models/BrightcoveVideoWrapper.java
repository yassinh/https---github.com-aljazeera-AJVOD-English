package com.brightcove.consulting.alj.models;

import static com.brightcove.consulting.config.YBTVConfigHelper.channelCustomField;
import static com.brightcove.consulting.config.YBTVConfigHelper.navRepository;
import static com.brightcove.consulting.config.YBTVConfigHelper.topicCustomField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brightcove.consulting.TimeZoneResolver;
import com.brightcove.consulting.alj.utils.CustomFieldValuePredicate;
import com.brightcove.consulting.alj.utils.DateRenderer;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.config.SpringContext;

/**
 * Decorator for a BrightcoveVideo that provides accessors to related navigation
 * models and other stuff.  This is specifically geared toward usage in the view.
 *
 * @author ssayles
 */
public class BrightcoveVideoWrapper extends BrightcoveVideo {

    private BrightcoveVideo video;
    private Channel channel;
    private boolean topicLookedUp;
    private Topic topic;
    private String topicUrl;
	private Locale locale;

	private static final Logger logger = LoggerFactory.getLogger(BrightcoveVideoWrapper.class);

    public BrightcoveVideoWrapper(BrightcoveVideo video) {
        this.video = video;
    }

    public Channel getChannel() {
        if (channel == null) {
            String channelId = video.getCustomField(channelCustomField());
            channel = navRepository().getChannel(channelId);
        }
        return channel;
    }

    public String getChannelUrl() {
        Channel channel = getChannel();
        if (channel != null) {
            // TODO: URL generation should happen somewhere else
            return "/channels/" + channel.getKey();
        }
        return null;
    }

    public Topic getTopic() {
        if (topic == null && !topicLookedUp) {
            Channel channel = getChannel();
            if (channel != null) {
                String topicFieldValue = video.getCustomField(topicCustomField());
                if (topicFieldValue != null) {
                    Predicate search = new CustomFieldValuePredicate(topicCustomField(), topicFieldValue, Topic.class);
                    topic = NavUtils.findDescendant(channel, search);
                }
            }
            topicLookedUp = true;
        }
        return topic;
    }

    /**
     * Alias for showLink.
     * @return
     */
    public String getShowUrl() {
        return video.getShowLink();
    }

    public String getTopicUrl() {
        if (topicUrl == null) {
            Channel channel = getChannel();
            if (channel != null) {
                Topic topic = getTopic();
                if (topic != null) {
                    // TODO: URL generation should happen somewhere else
                    topicUrl = "/channels/"+channel.getKey()+"/topics/"+topic.getKey();
                }
            }

        }
        return topicUrl;
    }

    public String getUrl() {
        Channel channel = getChannel();
        if (channel != null) {
            // TODO: URL generation should happen somewhere else
            return "/channels/" + channel.getKey() + "/videos/" + getSlug() + "/" + getId();
        }
        return null;
    }

    // DELEGATED METHODS TO VIDEO

    public String getName() {
        return video.getName();
    }

    public long getId() {
        return video.getId();
    }

    public String getReferenceId() {
        return video.getReferenceId();
    }

    public String getShortDescription() {
        return video.getShortDescription();
    }

    public String getLongDescription() {
        return video.getLongDescription();
    }

    public Date getDate() {
        return video.getDate();
    }

	public List<String> getTags() {
		return video.getTags();
	}

    public String getDisplayDate() {
    	Date date = getDate();
    	if (date != null) {
    		DateTimeZone timeZone = getTimeZone();
    		if (timeZone != null) {
    			date = new DateTime(date).withZone(timeZone).toDate();
    		}
	    	DateRenderer renderer = DateRenderer.getInstance(getLocale());
	    	return renderer.render(date, "video.dateFormat");
    	}
    	return null;
    }
    
    public String getReleaseDate() {
        return video.getReleaseDate();
    }

    public String getEventName(){
    	return video.getCustomField("eventname");
    }
    
    public String getSortableDate() {
        return video.getSortableDate();
    }

    public String getLinkUrl() {
        return video.getLinkUrl();
    }

    public String getLinkText() {
        return video.getLinkText();
    }

    public String getImageUrl() {
        return video.getImageUrl();
    }

    public String getThumbnailUrl() {
        return video.getThumbnailUrl();
    }

    public Long getVideoLength() {
        return video.getVideoLength();
    }

    public Long getViews() {
        return video.getViews();
    }

    public String getDuration() {
        return video.getDuration();
    }

    public String getDisplayLength() {
        return video.getDisplayLength();
    }

    public Map<String, String> getCustomFields() {
        return video.getCustomFields();
    }

    public String getEventDate() {
    	Map<String, String> customFields = getCustomFields();
    	if (customFields != null) {
    		return customFields.get("eventdate");
    	}
    	return null;
    }

    public String getEventDisplayDate() {
    	String text = this.getEventDate();
    	if (text == null) {
    		return null;
    	}
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
    	Date date = null;
    	try {
			date = sdf.parse(text);
		} catch (ParseException e) {
			logger.error("Exception occurred while parsing enddate '"+ text +"' for video " + this.getId(), e);
		}

    	if (date != null) {
    		DateTimeZone timeZone = getTimeZone();
    		if (timeZone != null) {
    			date = new DateTime(date).withZone(timeZone).toDate();
    		}
	    	DateRenderer renderer = DateRenderer.getInstance(getLocale());
	    	return renderer.render(date, "video.dateFormat");
    	}
    	return null;
    }

    public String getCustomField(String key) {
        return video.getCustomField(key);
    }

    public String getShowText() {
        return video.getShowText();
    }

    public String getShowLink() {
        return video.getShowLink();
    }

    public String getSlug() {
        return video.getSlug();
    }

    
    @SuppressWarnings("unused")
	private String webRoot() {
        return (String) SpringContext.getBean("webRoot");
    }

    private Locale getLocale() {
    	if (locale == null) {
	    	Channel channel = getChannel();
	    	if (channel != null) {
	    		locale = new Locale(channel.getLanguage());
	    	} else {
	    		String lang = (String) SpringContext.getBean("defaultUserLang");
	    		if (lang != null) {
	    			locale = new Locale(lang);
	    		} else {
	    			locale = Locale.getDefault();
	    		}
	    	}
    	}
    	return locale;
    }

    private DateTimeZone getTimeZone() {
    	return TimeZoneResolver.getTimeZone();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<BrightcoveVideoWrapper> convert(List<BrightcoveVideo> videos) {
        List wrappers = new ArrayList(videos);
        CollectionUtils.transform(wrappers, new Transformer());
        return (List<BrightcoveVideoWrapper>)wrappers;
    }

    public static class Transformer implements org.apache.commons.collections.Transformer {
        @Override
        public Object transform(Object input) {
            if (input instanceof BrightcoveVideo) {
                return new BrightcoveVideoWrapper((BrightcoveVideo) input);
            }
            return null;
        }
        
    }
}
