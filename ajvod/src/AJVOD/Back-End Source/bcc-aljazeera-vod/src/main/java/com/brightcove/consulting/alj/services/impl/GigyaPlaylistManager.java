package com.brightcove.consulting.alj.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com.brightcove.consulting.alj.models.Playlist;
import com.brightcove.consulting.alj.models.PlaylistIndex;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.PlaylistManager;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.alj.services.gigya.Accounts;
import com.brightcove.consulting.alj.services.gigya.DataStore;
import com.brightcove.consulting.alj.services.gigya.DataStore.Delete;
import com.brightcove.consulting.alj.services.gigya.DataStore.Search;
import com.brightcove.consulting.alj.services.gigya.DataStore.Store;
import com.brightcove.consulting.alj.services.gigya.JSObject;
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;

/**
 * A Gigya data store backed playlist manager.  
 * <p>
 * <b>Warning</b>: Calls to getPlaylists, create, delete, and reorder may result
 * in saved updates to the associated user in order to update the corresponding
 * playlistIndex.
 * </p>
 * @author ssayles
 */
@Component("playlistManager")
public class GigyaPlaylistManager implements PlaylistManager {

//	private static Logger logger = LoggerFactory.getLogger(GigyaPlaylistManager.class);

	@Autowired
	private Accounts gigyaAccountsApi;

	@Autowired
	private DataStore gigyaDataStoreApi;

	@Autowired
	private String gigyaSecret;

	@Autowired
	private UserManager userManager;

	@Autowired
	private CacheManager cacheManager;

	/**
	 * Returns an ordered list of playlists for the given user.  Warning, this
	 * may update the user object in order to update the playlist index.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Playlist> getPlaylists(String uid) throws ServiceException {

		// first, see if there's is a cached list
		// and create a cloned list if so
		Cache cache = cacheManager.getCache("playlists");
		ValueWrapper valueWrapper = cache.get(uid);
		if (valueWrapper != null) {
			List<Playlist> playlists = (List<Playlist>) valueWrapper.get();
			return cloneList(playlists);
		}

		// no cached copy of the playlists, fetch from the remote datastore
		Search search = gigyaDataStoreApi.Search();
		search.setQuery("select * from playlist where playlist.uid = '"+ uid +"'");
		GSResponse response = search.submit();

		// convert them into Playlist objects
		GSObject data = response.getData();
		GSArray results = data.getArray("results", null);
		List<Playlist> playlists = new ArrayList<Playlist>();

		if (results != null) {
			for (int i = 0; i < results.length(); i++) {
				GSObject obj = results.getObject(i);
				//[{"lastUpdatedTime":"2013-08-15T22:17:43Z",
				//      "createdTime":"2013-08-15T22:17:43Z", "created":1376605063000,
				//      "oid":"289f7494ee174d8fa628306745101cd0", "UID":"_guid_TXxBdoebAUE5c7GXstNcNw==",
				//      "data":{"uid":"_guid_TXxBdoebAUE5c7GXstNcNw==","videos":[{"id":"123456"}],"name":"test3"},"lastUpdated":1376605063000},
				//  {"lastUpdatedTime":"2013-08-15T21:38:15Z","createdTime":"2013-08-15T21:38:15Z","created":1376602695000,
				//      "oid":"68ea90dd609749f293fe2c1bd445ea21","UID":"_guid_TXxBdoebAUE5c7GXstNcNw==",
				//      "data":{"uid":"_guid_TXxBdoebAUE5c7GXstNcNw==","videos":[{"id":"12345"}],"name":"test2"},"lastUpdated":1376602695000},
				//  {"lastUpdatedTime":"2013-08-15T20:22:59Z","createdTime":"2013-08-15T20:22:59Z","created":1376598179000,
				//      "oid":"8db245fb51f845c3ad5b329cda70d1b2","UID":"_guid_TXxBdoebAUE5c7GXstNcNw==",
				//      "data":{"uid":"_guid_TXxBdoebAUE5c7GXstNcNw==","videos":[{"id":"1234"}],"name":"test"},"lastUpdated":1376598179000}
				//]
				// TODO: get dates
				GSObject playlistData = obj.getObject("data", null);
				if (playlistData != null) {
					Playlist playlist = new JSObject(playlistData).toObject(Playlist.class);
					// oid is not part of the data of the persisted object
					// so grab this from the result object
					String oid = obj.getString("oid", null);
					playlist.setOid(oid);
					playlists.add(playlist);
				}
			}

			
		}

		// if we found playlists, make sure to sync with the index
		if (!playlists.isEmpty()) {
			User user = userManager.get(uid);
			PlaylistIndex index = user.playlistIndex();
			List<Playlist> orderedList = index.orderByIndexAndSync(playlists);
			if (index.dirty()) {
				userManager.save(user);
				index.setDirty(false);
			}
			playlists = cloneList(orderedList);
		}

		cache.put(uid, playlists);

		return playlists;
	}

	@Override
	public String create(Playlist playlist) throws ServiceException {

		// if there is an oid, then the playlist is considered as already being
		// persisted, so no need to create.
		String oid = playlist.getOid();
		if (oid != null) {
			return null;
		}

		// cannot save a playlist if it is not associated with a user
		String uid = playlist.getUid();
		if (StringUtils.isEmpty(uid)) {
			return null;
		}

		// remove non-persisted fields
		// use a clone so we don't modify the original playlist object
		Playlist clone = playlist.clone();
		clone.setPosition(null);
		clone.setCreatedAt(null);
		clone.setUpdatedAt(null);


		// store the new playlist first
		Store storeReq = gigyaDataStoreApi.Store();
		storeReq.setData(clone);
		storeReq.setType("playlist");
		storeReq.setUid(uid);
		storeReq.setOid("auto");
		GSResponse response = storeReq.submit();
		evict(uid);

		oid = response.getString("oid", null);
		// playlist index will check for oid when adding, so set this
		clone.setOid(oid);

		// now update the playlist index
		User user = userManager.get(uid);
		PlaylistIndex index = user.playlistIndex();
		if (index.add(clone)) {
			if (index.dirty()) {
				userManager.save(user);
				index.setDirty(false);
			}
		}

		return oid;
	}


	/**
	 * TODO: store the object, but update the cache ourselves instead of
	 * causing the system to query for all playlists again.
	 */
	@Override
	@CacheEvict(value="playlists", key="#playlist.getUid()")
	public void update(Playlist playlist) throws ServiceException {
		if (StringUtils.isEmpty(playlist.getOid()) || StringUtils.isEmpty(playlist.getUid())) {
			return;
		}

		Playlist clone = playlist.clone();

		String oid = playlist.getOid();
		// unset these as they are derived from gigya datastore and we don't
		// actually want to pass them in
		clone.setOid(null);
		clone.setCreatedAt(null);
		clone.setUpdatedAt(null);
		clone.setPosition(null);

		Store storeReq = gigyaDataStoreApi.Store();
		storeReq.setOid(oid);
		storeReq.setType("playlist");
		storeReq.setUid(playlist.getUid());
		storeReq.setData(clone);
		storeReq.setUpdateBehavior("arraySet");
		storeReq.submit();

		// nothing left to do
		// if the response was an error, a ServiceException would be thrown
	}


	@Override
	public boolean delete(Playlist playlist) throws ServiceException {
		String uid = playlist.getUid();
		Delete deleteReq = gigyaDataStoreApi.Delete();
		deleteReq.setOid(playlist.getId());
		deleteReq.setType("playlist");
		deleteReq.setUID(playlist.getUid());
		deleteReq.submit();
		evict(uid);


		User user = userManager.get(uid);
		PlaylistIndex playlistIndex = user.playlistIndex();
		boolean removed = playlistIndex.remove(playlist);
		if (removed && playlistIndex.dirty()) {
			userManager.save(user);
			playlistIndex.setDirty(false);
		}

		return true;
	}

	public boolean reorder(String uid, String[] ids) throws ServiceException {
		User user = userManager.get(uid);
		PlaylistIndex index = user.playlistIndex();
		boolean reordered = index.reorder(ids);
		if (reordered) {
			evict(uid);
			userManager.save(user);
		}
		return reordered;
	}
	private void evict(String uid) {
		Cache cache = cacheManager.getCache("playlists");
		cache.evict(uid);
	}

	private List<Playlist> cloneList(List<Playlist> playlists) {
		List<Playlist> clone = new ArrayList<Playlist>();
		for (Playlist playlist : playlists) {
			clone.add(playlist.clone());
		}
		return clone;
	}
}
