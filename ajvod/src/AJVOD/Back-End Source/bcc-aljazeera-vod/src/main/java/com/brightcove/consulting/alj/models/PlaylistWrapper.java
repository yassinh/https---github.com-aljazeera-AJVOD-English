package com.brightcove.consulting.alj.models;

import java.util.ArrayList;
import java.util.List;

public class PlaylistWrapper {

	private Playlist playlist;

	private List<BrightcoveVideoWrapper> videos;  
	
	public PlaylistWrapper() {
	}

	public String getName() {
		return playlist.getName();
	}
	
	public String getId() {
		return playlist.getId();
	}
	
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}
	
	public Playlist getPlaylist() {
		return playlist;
	}
	
	public List<BrightcoveVideoWrapper> getVideos() {
		if (videos != null) {
			return videos;
		}
		videos = new ArrayList<BrightcoveVideoWrapper>();
		return videos;
	}

	public void setVideos(List<BrightcoveVideoWrapper> videos) {
		this.videos = videos;
	}

}
