package com.brightcove.consulting.alj.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brightcove.consulting.alj.services.PlaylistManager;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.config.SpringContext;
import com.google.gson.Gson;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6598495647324065211L;

	private static final Logger logger = LoggerFactory.getLogger(User.class);

	/*
	 * A unique ID for the person represented by this object. The UID is
	 * guaranteed to be consistent over time for users who were authenticated on
	 * the site by using either login or notifyLogin. Note that if notifyLogin
	 * was used then UID will return the same ID provided by the site. The UID
	 * String must be encoded using the encodeURIComponent() function, before
	 * sending it to your server.
	 */
	private String UID;

	/**
	 * If the global configuration object's "signIDs" field is set to 'true',
	 * Gigya "signs" the User object (when returned by an API method) with a
	 * cryptographic signature, to prevent fraud. This field will hold the
	 * signature, that should be used for verification*.
	 */
	private String UIDSignature;
	
	/**
	 * If the global configuration object's "signIDs" field is set to 'true',
	 * Gigya "signs" the User object (when returned by an API method) with a
	 * cryptographic signature, to prevent fraud. This field will hold the
	 * timestamp that should be used for verification*. The timestamp is the GMT
	 * time of the response in UNIX time format (i.e. the number of seconds
	 * since Jan. 1st 1970).
	 */
	private Long signatureTimestamp;

	/* The name of the provider that the user used in order to login. */
	private String loginProvider;

	/*
	 * Indicates whether the user is registered. The user is registered once his
	 * registration has been finalized.
	 */
	private Boolean isRegistered;

	/*
	 * Indicates whether the account is active. The account is active once the
	 * user creates it even without finalizing it. The account can be
	 * deactivated, but it will still be registered if the registration process
	 * has been finalized.
	 */
	private Boolean isActive;

	/* Indicates whether the account email is verified. */
	private Boolean isVerified;

	/*
	 * Influencer rank of the user. The iRank is a number between 1-99, which
	 * denotes the percentile location of the user in comparison to all other
	 * site users as a site influencer. For example, if a user's iRank equals
	 * 60, this means that 60% of the site users influence less than this user,
	 * or in other words, this user is in the top 40% of site influencers.
	 */
	private Integer iRank;

	/*
	 * The time of the last login of the user in Java time format, i.e.
	 * milliseconds since Jan. 1st 1970.
	 */
	private Long lastLoginTimestamp;

	/*
	 * The time of the last login of the user in ISO 8601 format, e.g.
	 * "1997-07-16T19:20:30Z".
	 */
	private String lastLogin;

	/*
	 * The time when the oldest data of the object was refreshed in Java time
	 * format, i.e. milliseconds since Jan. 1st 1970.
	 */
	private Long oldestDataUpdatedTimestamp;

	/*
	 * The time when the oldest data of the object was refreshed in ISO 8601
	 * format, e.g. "1997-07-16T19:20:30Z".
	 */
	private String oldestDataUpdated;

	/*
	 * The time when the last update of the object occurred (either full or
	 * partial update) in Java time, i.e. milliseconds since Jan. 1st 1970.
	 */
	private Long lastUpdatedTimestamp;

	/*
	 * The time when the last update of the object occurred (either full or
	 * partial update) in ISO 8601 format, e.g. "1997-07-16T19:20:30Z".
	 */
	private String lastUpdated;

	/*
	 * The time the account was created in Java time format, i.e. milliseconds
	 * since Jan. 1st 1970.
	 */
	private Long createdTimestamp;

	/*
	 * The time the account was created in ISO 8601 format, e.g.
	 * "1997-07-16T19:20:30Z".
	 */
	private String created;

	/* The user's login identifiers. This includes the following fields: */
	@SuppressWarnings("rawtypes")
    private Map loginIDs;

	/*
	 * The email addresses belonging to the user. This includes the following
	 * fields:
	 */
	@SuppressWarnings("rawtypes")
    private Map emails;

	/*
	 * socialProviders string object 's profile information as described in the
	 * Profile object. The profile is returned in the response by default, but
	 * if the include parameter is used to specify other fields that should be
	 * provided in the response, the profile must also be specified explicitly
	 * in the include parameter.A comma-separated list of the names of the
	 * providers to which the user is connected/logged in.
	 */
	@SuppressWarnings("rawtypes")
    private Map profile;

	/*
	 * Any data that you want to store regarding the user that isn't part of the
	 * Profile object.
	 */
	@SuppressWarnings("rawtypes")
    private Map data;

	/*
	 * An array of Identity objects, each object represents a user's social
	 * identity. Each Identity object contains imported data from a social
	 * network that the user has connected to. Note: identities must be
	 * specified explicitly in the include parameter in order to be included in
	 * the response (either identities-active or identities-all).
	 */
	@SuppressWarnings("rawtypes")
    private Map[] identities;

	private transient PlaylistIndex playlistIndex;

	public String getUID() {
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}

	public String getUIDSignature() {
		return UIDSignature;
	}

	public void setUIDSignature(String uIDSignature) {
		UIDSignature = uIDSignature;
	}

	public Long getSignatureTimestamp() {
		return signatureTimestamp;
	}

	public void setSignatureTimestamp(Long signatureTimestamp) {
		this.signatureTimestamp = signatureTimestamp;
	}

	public String getLoginProvider() {
		return loginProvider;
	}

	public void setLoginProvider(String loginProvider) {
		this.loginProvider = loginProvider;
	}

	public Boolean getIsRegistered() {
		return isRegistered;
	}

	public void setIsRegistered(Boolean isRegistered) {
		this.isRegistered = isRegistered;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}

	public Integer getiRank() {
		return iRank;
	}

	public void setiRank(Integer iRank) {
		this.iRank = iRank;
	}

	public Long getLastLoginTimestamp() {
		return lastLoginTimestamp;
	}

	public void setLastLoginTimestamp(Long lastLoginTimestamp) {
		this.lastLoginTimestamp = lastLoginTimestamp;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Long getOldestDataUpdatedTimestamp() {
		return oldestDataUpdatedTimestamp;
	}

	public void setOldestDataUpdatedTimestamp(Long oldestDataUpdatedTimestamp) {
		this.oldestDataUpdatedTimestamp = oldestDataUpdatedTimestamp;
	}

	public String getOldestDataUpdated() {
		return oldestDataUpdated;
	}

	public void setOldestDataUpdated(String oldestDataUpdated) {
		this.oldestDataUpdated = oldestDataUpdated;
	}

	public Long getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	@SuppressWarnings("rawtypes")
    public Map getLoginIDs() {
		return loginIDs;
	}

	@SuppressWarnings("rawtypes")
    public void setLoginIDs(Map loginIDs) {
		this.loginIDs = loginIDs;
	}

	@SuppressWarnings("rawtypes")
    public Map getEmails() {
		return emails;
	}

	@SuppressWarnings("rawtypes")
    public void setEmails(Map emails) {
		this.emails = emails;
	}

	@SuppressWarnings("rawtypes")
    public Map getProfile() {
		return profile;
	}

	@SuppressWarnings("rawtypes")
    public void setProfile(Map profile) {
		this.profile = profile;
	}

	@SuppressWarnings("rawtypes")
    public Map getData() {
	    if (data == null) {
	        data = new HashMap();
	    }
		return data;
	}

	@SuppressWarnings("rawtypes")
    public void setData(Map data) {
		this.data = data;
	}

	@SuppressWarnings("rawtypes")
    public Map[] getIdentities() {
		return identities;
	}

	@SuppressWarnings("rawtypes")
    public void setIdentities(Map[] identities) {
		this.identities = identities;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    public Map vod() {
	    Map data = getData();
	    Map vod = (Map) data.get("vod");

	    if (vod == null) {
	        vod = new HashMap();
	        data.put("vod", vod);
	    }
	    return vod;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map preferences() {
	    Object value = this.vod().get("preferences");
	    if (value == null || !(value instanceof Map)) {
	    	value = new HashMap();

	    	this.vod().put("preferences", value);
	    }
	
		this.vod().put("preferences", value);
		return (Map) value;
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map preferences(String defaultLang, String defaultLocale, String defaultTimezone) {
	    Object value = this.vod().get("preferences");
	    if (value == null || !(value instanceof Map)) {
	    	value = new HashMap();
	    	
	    	//put in default preferences
	    	((HashMap) value).put("locale", defaultLocale);
	    	((HashMap) value).put("lang", defaultLang);
	    	((HashMap) value).put("timezone", defaultTimezone);
	    	
	    	this.vod().put("preferences", value);
	    }
	
		this.vod().put("preferences", value);
		return (Map) value;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean setPreferences(Map pPreferences) {
    	if (pPreferences == null || pPreferences.isEmpty()) {
    		return false;
    	}

    	Map newPreferences = new HashMap();
    	newPreferences.putAll(preferences());

    	// if anything is out of the ordinary, be on the safe side and forget
    	// about trying to save any preferences. Here we'll make sure there are
    	// only the preferences we expect and in the format we expect them, with
    	// some allowances for case
    	Set keys = pPreferences.keySet();
    	for (String key : (Set<String>)keys) {

    		key = key.toLowerCase().trim();
			if (!key.matches("lang|country|timezone")) {
				return false;
			}

			Object value = pPreferences.get(key);

			// and ensure the value is in a valid format
			if ("lang".equals(key)) {
				if (!(value instanceof String)) {
					return false;
				}
				String lang = (String) value;
				lang = lang.toLowerCase().trim();
				if (lang.matches("^[a-z]{2}$")) {
					newPreferences.put(key, lang);
				} else {
					// bad lang value
					return false;
				}
			} else if ("country".equals(key)) {
				if (!(value instanceof String)) {
					return false;
				}
				String country = (String) value;
				country = country.toUpperCase().trim();
				if (Arrays.binarySearch(Locale.getISOCountries(), country) >= 0) {
					newPreferences.put(key, country);
				} else {
					return false;
				}
			} else if ("timezone".equals(key)){
				String tzId = (String) value;
				try {
					DateTimeZone timeZone = DateTimeZone.forID(tzId);
					newPreferences.put(key, timeZone.toString());
				} catch (IllegalArgumentException e) {
					return false;
				}
			}
		}

    	// if we're still here, then we've updated one or more preferences
    	this.vod().put("preferences", newPreferences);
    	return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map[] subscriptions() {
	    Object value = this.vod().get("subscriptions");
	    if (value != null) {
	        if (value instanceof List) {
	            value = ((List)value).toArray(new Map[] {});
	        } else if (!value.getClass().isArray()) {
	            throw new IllegalStateException("Expected subscriptions to be an array or List of Map.");
	        }
	        return (Map[])value;
	    }

		value = new Map[] {};
		this.vod().put("subscriptions", value);
		return (Map[]) value;
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List videoPositions() {
	    Object value = this.vod().get("autoresume");
	    if (value != null) {
	        if (value.getClass().isArray()) {
	        	value = (List) Arrays.asList((Map[])value);
	        }
	        return (List)value;
	    }
	
		value = new ArrayList<Map>();
		this.vod().put("autoresume", value);
		return (List) value;
	}
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean setVideoPosition(String videoId, String position){
		if(StringUtils.isEmpty(position) || StringUtils.isEmpty(videoId))
			return false;
		
		Map videoPosition = new HashMap();
		videoPosition.put("id", videoId);
		videoPosition.put("pos", position);
		
		List<Map> positions = videoPositions();	
		// if there is an existing position, update that first instead.
		for (Map savedPosition : positions) {
            if (StringUtils.equals(videoId, (String) savedPosition.get("id"))) {
                savedPosition.put("pos", position);
                return true;
            }
        }

		// otherwise, we're adding a new video
		positions.add(0, videoPosition);
		// keep the queue to the last 10 videos, if the one we just added
		// makes for 11 or more, then prune down to the first 10
		if(positions.size() >= 11){
			positions = positions.subList(0, 10);
		}
	
		this.vod().put("autoresume", positions);
	    return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean removeVideoPosition(String videoId){
		if(StringUtils.isEmpty(videoId)) {
			return false;
		}
		
		List<Map> positions = videoPositions();
		Map position = null;
		boolean removed = false;
		// if there is an existing position, update that first instead.
		for (Map savedPosition : positions) {
			if (StringUtils.equals(videoId, (String) savedPosition.get("id"))) {
				position = savedPosition;
			}
		}

		if (position != null) {
			removed = positions.remove(position);
			if (removed) {
				this.vod().put("autoresume", positions);
			}
		}
		
		return removed;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getVideoPosition(String videoId){
		List<Map> positions = videoPositions();
		
		for (Map map : positions) {
			if (StringUtils.equals(videoId, (String) map.get("id"))) 
	        	return (String) map.get("pos");
		}
	
		return "0";
	}

	/**
	 * Reorders the subscriptions collection based on the given array of ids.
	 * This will skip any id that doesn't match an existing subscription, and
	 * will ensure that all current subscriptions are returned regardless of
	 * what ids are given.
	 *
	 * @param ids Ordered list of subscription ids.
	 * @return <code>true</code> if the subscriptions list was modified.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean orderSubscriptions(final String[] ids) {
	    boolean modified = false;

	    Map[] subscriptions = this.subscriptions();
	    List<Map> orderedList = new ArrayList<Map>();
	    
	    // for each id given
	    for (int i = 0; i < ids.length; i++) {
	        String id = ids[i];
	        // find the matching subscription with the same id
	        // and add it to our new ordered list
	        for (int j = 0; j < subscriptions.length; j++) {
                Map subscription = subscriptions[j];
                if (StringUtils.equals((String) subscription.get("id"), id)) {
                    orderedList.add(subscription);
                    // if the indexes are different, then we actually did change
                    // the order
                    if (i != j) {
                        modified = true;
                    }
                    break;
                }
            }
        }
	    
	    // if we didn't actually change anything, then there's leave
	    // the subscriptions list alone.
	    if (!modified) {
	        return false;
	    }

	    // if anything leftover, tack onto the end
	    // if the original list and new ordered list are the same length, then
	    // don't bother
	    if (orderedList.size() != subscriptions.length) {
	        List<Map> originalList = Arrays.asList(subscriptions);
	        // find all the subscriptions in the original list that don't have
	        // and id in the given ids list
	        Collection missingSubs = CollectionUtils.selectRejected(originalList, new Predicate() {
                public boolean evaluate(Object object) {
                    Map subscription = (Map) object;
                    String id = (String) subscription.get("id");
                    if (ArrayUtils.indexOf(ids, id) >= 0) {
                        return true;
                    }
                    return false;
                }
            });
	        if (!missingSubs.isEmpty()) {
	            orderedList.addAll(missingSubs);
	        }
	    }
	    
	    // if we got here, then something was actually changed so go ahead and
	    // update
	    this.vod().put("subscriptions", subscriptions);
	    saveSubscriptions(orderedList.toArray(new Map[] {}));
	    return true;
	}

	/**
	 * Create and add a new subscription based on the given data.
	 *
	 * @param subscription Subscription data.
	 * @return The new subscription id.  Otherwise, return null if the an
	 *    existing subscription of the same data already exists.
	 * @throws JSONException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public String addSubscription(Map subscription) throws JSONException {
	    if (subscription.isEmpty()) {
	        return null;
	    }
	    // ensure we can convert to json, as well make a copy that we'll
	    // potentially modify
	    Gson gson = new Gson();
	    subscription = gson.fromJson(new JSONObject(subscription).toString(), Map.class);
	    subscription.remove("id");

	    Map[] subscriptions = this.subscriptions();

	    // this will help to ensure the data is correct
	    Subscription subscriptionObject = new Subscription(subscription);
	    
	    // create an id for the new subscription
	    String text = subscriptionObject.toJson();
	    text += this.getUID();
	    String id = DigestUtils.md5Hex(text);

	    // see if there already is a subscription with the same
	    // id or possibly a matching query
	    for (Map map : subscriptions) {
            if (StringUtils.equals((String) map.get("id"), id)) {
                // found a matching subscription, don't add
                return null;
            }
            if (!StringUtils.isEmpty((String)map.get("query"))) {
                Query savedQuery = new Query((String) map.get("query"));
                if (savedQuery.equals(subscriptionObject.getQuery())) {
                    // found a subscription with a matching query
                    return null;
                }
            }
        }

	    // go ahead and add it
	    subscription.put("id", id);
	    subscriptions = (Map[]) ArrayUtils.add(subscriptions, subscription);
	    this.saveSubscriptions(subscriptions);

	    return id;
	}

	/**
	 * Remove subscription with given id.
	 * @param id
	 * @return <code>true</code> if the subscription was removed.
	 */
	@SuppressWarnings("rawtypes")
    public boolean removeSubscription(String id) {
	    Map[] subscriptions = subscriptions();
	    for (int i = 0; i < subscriptions.length; i++) {
	        if (StringUtils.equals(id, (String) subscriptions[i].get("id"))) {
	            subscriptions = (Map[]) ArrayUtils.remove(subscriptions, i);
	            this.saveSubscriptions(subscriptions);
	            return true;
	        }
        }
	    return false;
	}
	
	@SuppressWarnings("rawtypes")
    public Subscription getMatchingSubscription(Query query) {
	    Map[] subscriptions = this.subscriptions();
	    if (subscriptions != null && subscriptions.length > 0) {
	        for (int i = 0; i < subscriptions.length; i++) {
	            try {
	                Subscription subscription = new Subscription(subscriptions[i]);
	                Query subQuery = subscription.getQuery();
	                if (subQuery.equals(query)) {
	                    return subscription;
	                }
	            } catch (JSONException e) {
	                logger.error("Error parsing subscription '" 
	                        + subscriptions[i] + "' for user " + this.getUID());
	            }
	        }
	    }
	    return null;
	}

	/**
	 * Returns the user's playlist index.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PlaylistIndex playlistIndex() {
		if (playlistIndex != null) {
			return playlistIndex;
		}

		List<String> list = (List<String>) this.vod().get("playlistIndex");
		if (list == null) {
			list = new ArrayList<String>();
			this.vod().put("playlistIndex", list);
		}

		PlaylistIndex index = new PlaylistIndex();
		index.setPlaylistIds(list);
		index.setUid(this.getUID());
		this.playlistIndex = index;
		return index;
	}

	/**
	 * Returns any existing playlists matching the given name with ignored case.
	 *
	 * @param name
	 * @return A matching Playlist.
	 */
	public Playlist findPlaylistByName(String name) {
		if (StringUtils.isEmpty(this.UID)) {
			return null;
		}
		List<Playlist> playlists = getPlaylists();
		for (Playlist playlist : playlists) {
			if (StringUtils.equalsIgnoreCase(name, playlist.getName())) {
				return playlist;
			}
		}
		return null;
	}

	public Playlist findPlaylistById(String id) {
		if (StringUtils.isEmpty(this.UID)) {
			return null;
		}
		List<Playlist> playlists = getPlaylists();
		for (Playlist playlist : playlists) {
			if (StringUtils.equals(id, playlist.getId())) {
				return playlist;
			}
		}
		return null;
	}

	/**
	 * Returns the first playlist found that contains the given video based on
	 * it's id.
	 *
	 * @param video
	 * @return A matching playlist or null.
	 */
	public Playlist findPlaylistWithVideo(BrightcoveVideo video) {
		String videoId = video.getId()+"";
		return findPlaylistWithVideoId(videoId);
	}

	public Playlist findPlaylistWithVideoId(String videoId) {
		List<Playlist> playlists = getPlaylists();
		for (Playlist playlist : playlists) {
			List<Video> videos = playlist.getVideos();
			for (Video _video : videos) {
				if (StringUtils.equals(_video.getId(), videoId)) {
					return playlist;
				}
			}
		}
		return null;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private void saveSubscriptions(Map[] subscriptions) {
	    this.vod().put("subscriptions", subscriptions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((UID == null) ? 0 : UID.hashCode());
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
		User other = (User) obj;
		if (UID == null) {
			if (other.UID != null)
				return false;
		} else if (!UID.equals(other.UID))
			return false;
		return true;
	}

	public User clone() {
	    ObjectOutputStream oos = null;
	    ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bis);
            User clone = (User) ois.readObject();
            return clone;
        } catch (IOException e) {
            logger.error("IOException occurred while cloning user " + this.getUID());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            logger.error("IOException occurred while cloning user " + this.getUID());
            throw new RuntimeException(e);
        } finally {
            if (oos != null) {
                try {oos.close();} catch (IOException e) {}
            }
            if (ois != null) {
                try {ois.close();} catch (IOException e) {}
            }
        }
	}

	@SuppressWarnings("unchecked")
	private List<Playlist> getPlaylists() {
		PlaylistManager playlistManager = (PlaylistManager) SpringContext.getBean("playlistManager");
		try {
			List<Playlist> playlists = playlistManager.getPlaylists(this.getUID());
			return playlists;
		} catch (ServiceException e) {
			logger.error("Exception occurred while trying to get playlists for user "+ this.UID + "'", e);
			return ListUtils.EMPTY_LIST;
		}
	}

	

}
