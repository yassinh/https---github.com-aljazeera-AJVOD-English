package com.brightcove.consulting.alj.services;

import java.util.List;

import com.brightcove.consulting.alj.models.Playlist;

public interface PlaylistManager {

	List<Playlist> getPlaylists(String uid) throws ServiceException;

	/**
	 * Creates a new persisted playlist.
	 * 
	 * @param playlist
	 * @return The id of the newly stored playlist.
	 * @throws ServiceException
	 */
	String create(Playlist playlist) throws ServiceException;

	void update(Playlist playlist) throws ServiceException;

	boolean delete(Playlist playlist) throws ServiceException;

	public boolean reorder(String uid, String[] ids) throws ServiceException;
}
