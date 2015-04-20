package com.brightcove.consulting.alj.json;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.Show;
import com.brightcove.consulting.alj.utils.CustomFieldValuePredicate;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.config.SpringContext;

/**
 * Json response specifically for displaying a list of videos that will link
 * to the correlating video page.
 *
 * @author ssayles
 */
public class VideoJson {

    private static String UrlFormat = "/channels/{0}/videos/{1}/{2}";

    private BrightcoveVideoWrapper video;

    private Channel channel;

    private String showTitle;
    
    private String urlRoot;

    protected VideoJson(BrightcoveVideoWrapper video, Channel channel) {
        this.video = video;
        this.channel = channel;
        this.urlRoot = (String) SpringContext.getBean("webRoot");
    }

    public String getThumbnail() {
        return video.getImageUrl();
    }

    public String getTitle() {
        return video.getName();
    }

    public String getDescription() {
        return video.getShortDescription();
    }

    public Long getDate() {
        return video.getDate() == null ? null : video.getDate().getTime(); 
    }

    public String getDisplayDate() {
    	return video.getDisplayDate();
    }

    public Long getDuration() {
        return video.getVideoLength();
    }
    
    public String getShowLink() {
    	return video.getShowLink();
    }

    public String getShow() {
        String customFieldName = (String) SpringContext.getBean("showCustomField");
        String showFieldValue = video.getCustomField(customFieldName);
        if (showTitle == null && showFieldValue != null) {
            Show show = null;
            if (channel != null) {

                Predicate searchPredicate = new CustomFieldValuePredicate(customFieldName, showFieldValue, Show.class);
                show = NavUtils.findDescendant(channel, searchPredicate);
            }
            if (show != null) {
                showTitle = show.getName(); 
            }
        }
        return showTitle;
    }

    public String getUrl() {
        //TODO: get custom field for channel from configuration
        String cid = channel == null ? video.getCustomField("channel") : channel.getKey();
        if (cid == null) {
            return null;
        }
        String url = urlRoot + MessageFormat.format(UrlFormat, cid, video.getSlug(), String.valueOf(video.getId()));
        // It should be safe to not URL encode this because we are controlling / preparing all dynamic
        // values that can make their way into a URL
        /*
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this really shouldn't ever happen
        }
        */
        return url;
    }


    public static Collection<VideoJson> convert(Collection<BrightcoveVideo> videos) {
        return convert(videos, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection<VideoJson> convert(Collection<BrightcoveVideo> videos, Channel channel) {
        return CollectionUtils.collect(
        		BrightcoveVideoWrapper.convert(new ArrayList(videos)), new Transformer(channel));
    }

    /**
     * commons-collections transformer that will create a VideoJson object for
     * a given BrightcoveVideo object.
     *
     * @author ssayles
     */
    public static class Transformer implements org.apache.commons.collections.Transformer {
        private Channel channel;
        public Transformer() {
        }
        public Transformer(Channel channel) {
            this.channel = channel;
        }
        @Override
        public Object transform(Object input) {
            if (input != null) {
                return new VideoJson((BrightcoveVideoWrapper)input, channel);
            }
            return null;
        }
    }
}
