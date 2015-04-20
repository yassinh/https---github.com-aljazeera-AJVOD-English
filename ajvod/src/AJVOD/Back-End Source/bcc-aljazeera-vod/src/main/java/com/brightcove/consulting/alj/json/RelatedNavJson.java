package com.brightcove.consulting.alj.json;

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.config.SpringContext;

public class RelatedNavJson {
    private NavigationItem navItem;

    private String url;

    private String urlRoot;

    private static final String URL_FORMAT = "/channels/{0}/{1}/{2}";

    public RelatedNavJson(NavigationItem item) {
        this.navItem = item;
        this.urlRoot = (String) SpringContext.getBean("webRoot");
        if (urlRoot == null) {
            urlRoot = StringUtils.EMPTY;
        }
    }

    public String getTitle() {
        return navItem.getName();
    }

    public String getUrl() {
        if (url == null) {
            Channel channel;
            if (navItem instanceof Channel) {
                channel = (Channel) navItem;
            } else {
                channel = NavUtils.findAncestor(navItem, Channel.class);
            }
    
            if (channel == null) {
                return null;
            }
    
            String cid = channel.getKey();
            String resourceName = navItem.getClass().getSimpleName().toLowerCase();
            resourceName = resourceName.replaceAll("/y$/", "ie");
            resourceName += "s";
    
            url = urlRoot + MessageFormat.format(URL_FORMAT, cid, resourceName, navItem.getKey());
        }
        return url;
    }

    @SuppressWarnings("unchecked")
    public static Collection<RelatedNavJson> convert(Collection<NavigationItem> items) {
        return CollectionUtils.collect(items, new Transformer());
    }

    public static class Transformer implements org.apache.commons.collections.Transformer {
        @Override
        public Object transform(Object input) {
            return new RelatedNavJson((NavigationItem)input);
        }
    }
}
