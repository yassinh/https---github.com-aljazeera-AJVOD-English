package com.brightcove.consulting.alj.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PlaylistIndex {

	private String uid;

	private List<String> playlistIds = new ArrayList<String>();

	private boolean dirty;

	public PlaylistIndex() {
	}

	/**
	 * Add the given playlist to the index.  This will only add if the given playlist
	 * has a valid id value, has not already been indexed, and has a matching uid.
	 *
	 * @param playlist
	 * @return
	 */
	public boolean add(Playlist playlist) {
		if (!StringUtils.isEmpty(playlist.getId()) && !StringUtils.isEmpty(playlist.getUid())) {
			// we already have this playlist or the playlist doesn't belong to this user
			if (this.playlistIds.contains(playlist.getId()) || !StringUtils.equals(this.uid, playlist.getUid())) {
				return false;
			}

			boolean added = this.playlistIds.add(playlist.getId());
			if (added) {
				this.dirty = true;
				return added;
			}
		}
		return false;
	}

	/**
	 * Remove the given playlist from the index.  This will only remove if the
	 * given playlist is contained in the index and has a matching uid.
	 *
	 * @param playlist
	 * @return
	 */
	public boolean remove(Playlist playlist) {
		if (!StringUtils.isEmpty(playlist.getId()) && StringUtils.equals(this.uid, playlist.getUid())) {
			boolean removed = this.playlistIds.remove(playlist.getId());
			if (removed) {
				this.dirty = true;
				return removed;
			}
		}
		return false;
	}

	/**
	 * Set the playlist at the given position.  This will only occur if the playlist
	 * exists in the index, has a matching uid, and is not already in the given position.
	 *
	 * @param playlist
	 * @param position
	 * @return
	 */
	public boolean set(Playlist playlist, int position) {
		if (!StringUtils.isEmpty(playlist.getId()) && StringUtils.equals(this.uid, playlist.getUid())) {
			int currentPos = playlistIds.indexOf(playlist.getId());
			if (currentPos >= 0 && currentPos != position) {
				playlistIds.remove(currentPos);
				playlistIds.add(position, playlist.getId());
				this.dirty = true;
				return true;
			}
		}
		return false;
	}

	public boolean reorder(String[] ids) {
		List<String> idList = Arrays.asList(ids);
		if (idList.equals(this.playlistIds)) {
			// no reordering to do
			return false;
		}
		if (!playlistIds.containsAll(idList)) {
			return false;
		}
		boolean removed = playlistIds.removeAll(idList);
		if (removed) {
			playlistIds.addAll(0, idList);
			this.dirty = true;
			return true;
		}
		// if nothing was removed, then nothing was reordered
		return false;
	}

	/**
	 * Given a list of playlists, create a new list in order of the contained
	 * index.  This will potentially update the internal index if any mismatches
	 * are found between the internal index and the given list. This assumes the
	 * given list is the complete list to sync with. The <code>dirty</code>
	 * property can be checked to see if the internal list was modified as a
	 * result of calling this method.
	 *
	 * @param playlists
	 * @return
	 */
	public List<Playlist> orderByIndexAndSync(List<Playlist> playlists) {
		// recreate playlist and id lists so we can manipulate them
		List<Playlist> _playlists = new ArrayList<Playlist>(playlists);
		List<String> _playlistIds = new ArrayList<String>(this.playlistIds);

		// populate the intended ordered list of playlists
		// and remove from the lists as we find them (so we can track any leftovers)
		List<Playlist> orderedPlaylists = new ArrayList<Playlist>();
		for (String playlistId: this.playlistIds) {
			for (Playlist playlist : playlists) {
				if (StringUtils.equals(playlist.getId(), playlistId)) {
					_playlists.remove(playlist);
					_playlistIds.remove(playlistId);
					orderedPlaylists.add(playlist);
					break;
				}
			}
		}

		// if there any playlists left, then we don't have those indexed
		// go ahead and index and append to the ordered list
		if (!_playlists.isEmpty()) {
			for (Playlist playlist : _playlists) {
				this.playlistIds.add(playlist.getId());
			}
			orderedPlaylists.addAll(_playlists);
			this.dirty = true;
		}

		// if there are any ids left, then we have some playlists that no longer
		// exist
		if (!_playlistIds.isEmpty()) {
			this.playlistIds.removeAll(_playlistIds);
			this.dirty = true;
		}
		for (int i = 0; i < orderedPlaylists.size(); i++) {
			orderedPlaylists.get(i).setPosition(i);
		}
		return orderedPlaylists;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<String> getPlaylistIds() {
		return playlistIds;
	}

	/**
	 * Resets the internal index to the given list.  This will not set the dirty
	 * flag.
	 *
	 * @param playlistIds
	 */
	public void setPlaylistIds(List<String> playlistIds) {
		this.playlistIds = playlistIds;
	}

	/**
	 * Returns whether or not the internal index has been modified.  Don't use
	 * standard bean naming pattern so we can avoid json serialization and storage
	 * into the backend data store.
	 *
	 * @return
	 */
	public boolean dirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
