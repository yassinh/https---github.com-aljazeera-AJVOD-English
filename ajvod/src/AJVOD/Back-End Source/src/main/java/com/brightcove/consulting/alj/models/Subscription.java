package com.brightcove.consulting.alj.models;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.brightcove.consulting.alj.utils.EncryptionHandler;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.config.SpringContext;
import com.brightcove.consulting.services.BrightcoveReadApiService;

/**
* A subscription represents a specific query use to retrieve videos on behalf of
* a user.  Users will think of it as a bookmark or an rss feed. Currently,
* subscriptions are persisted in the backend for a specifc user.
*/
public class Subscription {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private String id;
    private String name;
    private String url;
    private Query query;
    private List<? extends BrightcoveVideo> videos;
    private EncryptionHandler encryptionHandler;

    @Autowired
    private BrightcoveReadApiService brightcoveReadApiService;
    
    private String queryJson;

    @SuppressWarnings("rawtypes")
    public Subscription(Map json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("id")) {
            id = jsonObject.getString("id");
        }
        if (jsonObject.has("name")) {
            name = jsonObject.getString("name");
        }
        if (jsonObject.has("url")) {
            url = jsonObject.getString("url");
        }
        if (jsonObject.has("query")) {
            query = new Query(jsonObject.getString("query"));
            queryJson = query.toJson();
        }
    }

    public Subscription() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getQueryJson2() {
        return queryJson;
    }

    public String getQueryJson() {
        Query query = getQuery();
        if (query != null) {
                EncryptionHandler eh = getEncryptionHandler();
                if (eh.isUseEncryption()) {
                        return eh.encrypt(query.toJson());
                } else {
                        return query.toJson();
                }
        }
        return null;
    }

    public void setQueryJson(String queryJson) {
        this.queryJson = queryJson;
    }

    public List<? extends BrightcoveVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<? extends BrightcoveVideo> videos) {
        this.videos = videos;
    }

    public Channel getChannel() {
        if (this.query != null) {
            String channelId = this.query.getChannelId();
            if (channelId != null) {
                NavRepository navRepository = (NavRepository) SpringContext.getBean("navRepository");
                if (navRepository != null) {
                    return navRepository.getChannel(channelId);
                }
            }
        }
        return null;
    }

    public String toJson() {
        JSONObject obj = new JSONObject();
        addToJson("name", name, obj);
        addToJson("id", id, obj);
        addToJson("url", url, obj);
        
        if (query != null) {
            addToJson("query", query.toJson(), obj);
        }

        return obj.toString();
    }

    private void addToJson(String key, Object value, JSONObject jsonObj) {
        if (key != null && value != null) {
            try {
                jsonObj.put(key, value);
            } catch (JSONException e) {
                logger.warn("Exception occurred while trying to populate subscription json object", e);
            }
        }
    }

    private EncryptionHandler getEncryptionHandler() {
        if (encryptionHandler != null) {
                return encryptionHandler;
        }

        encryptionHandler = (EncryptionHandler)
                        SpringContext.getBean("encryptionHandler");

        return encryptionHandler;

    }
}
