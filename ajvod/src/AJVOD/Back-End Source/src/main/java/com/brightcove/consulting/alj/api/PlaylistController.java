package com.brightcove.consulting.alj.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.brightcove.consulting.alj.api.errors.ConflictException;
import com.brightcove.consulting.alj.models.Playlist;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.models.Video;
import com.brightcove.consulting.alj.services.PlaylistManager;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.mediaapi.exceptions.BrightcoveException;


@Controller
@RequestMapping("/api/playlists")
public class PlaylistController extends BaseController {

	@Autowired
	private PlaylistManager playlistManager;

	@Autowired
	private int maxPlaylists;

	@Autowired
	private int maxPlaylistVideos;

	@RequestMapping(value="/get", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String,Object> get(HttpServletRequest request) throws ServiceException {

		User user = getUser(request);

		List<Playlist> playlists = playlistManager.getPlaylists(user.getUID());

		Map<String,Object> jsonResponse = json(HttpStatus.OK);
		List<Map<String, String>> jsonData = new ArrayList<Map<String, String>>();
		for (Playlist playlist : playlists) {
			Map<String,String> json = new HashMap<String, String>();
			json.put("id", playlist.getId());
			json.put("name", playlist.getName());
			jsonData.add(json);
		}
		jsonResponse.put("playlists", jsonData);

		return jsonResponse;
	}

	@RequestMapping(value="/create", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String,Object> create(HttpServletRequest request,
			@RequestParam(required=true) String name,
			@RequestParam(required=false) String videoID) throws ServiceException, BrightcoveException {

		User user = getUser(request);

		Playlist playlist = user.findPlaylistByName(name);

		// if there is already a playlist by the given name, then do nothing
		if (playlist != null) {
			Map<String, Object> jsonResponse = json(HttpStatus.OK);
			jsonResponse.put("id", playlist.getId());
			return jsonResponse;
		}

		List<Playlist> playlists = playlistManager.getPlaylists(user.getUID());
		if (playlists.size() >= maxPlaylists) {
			return json(HttpStatus.CONFLICT, "Cannot create more than "+ maxPlaylists + " playlists.");
		}

		playlist = new Playlist();
		playlist.setName(name.trim());
		playlist.setUid(user.getUID());

		if (!StringUtils.isEmpty(videoID)) {
			Video video = new Video();
			video.setId(videoID);
			playlist.addVideo(video);
		}

		// TODO: populate once user preferences are merged in from epic-9
//		playlist.setLocale(user.get)
//		playlist.setLang(user.get)

		String oid = playlistManager.create(playlist);

		Map<String, Object> jsonResponse = json(HttpStatus.CREATED);
		jsonResponse.put("id", oid);
		
		return jsonResponse;
	}

	@RequestMapping(value="/add", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> add(HttpServletRequest request,
			@RequestParam(required=true) String videoId,
			@RequestParam(required=true) String playlistId) throws ServiceException, ConflictException {

		Map<String, Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);

		
		Playlist playlist = user.findPlaylistWithVideoId(videoId);
		if (playlist != null) {
			if (!StringUtils.equals(playlist.getId(), playlistId.trim())) {
				ConflictException conflict = new ConflictException(
						"This video is already part of another playlist");
				conflict.addAttribute("otherPlaylist", playlist.getName());
				throw conflict;
			} else {
				// it's already in the playlist, do nothing
				jsonResponse.put("added", false);
				jsonResponse.put("message", "The playlist already contains this video.");
			}
		}

		playlist = getPlaylist(user, playlistId);

		List<Video> videos = playlist.getVideos();
		if (videos.size() >= maxPlaylistVideos) {
			throw new ConflictException("Cannot add more than "+ maxPlaylistVideos + " videos to a playlist.");
		}

		Video video = new Video();
		video.setId(videoId);

		boolean added = playlist.addVideo(video);
		if (added) {
			playlistManager.update(playlist);
			jsonResponse.put("added", true);
		} else {
			jsonResponse.put("added", false);
		}

		return jsonResponse;
	}

	@RequestMapping(value="/remove", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> remove(HttpServletRequest request,
			@RequestParam(required=true) String videoId,
			@RequestParam(required=true) String playlistId) throws ServiceException {
		User user = getUser(request);

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		Playlist playlist = user.findPlaylistById(playlistId);
		
		if (playlist != null) {
			boolean removed = playlist.removeVideo(videoId);

			if (removed) {
				playlistManager.update(playlist);
			}

			jsonResponse.put("removed", removed);
			return jsonResponse;
		}

		// didn't find the playlist or video
		jsonResponse.put("removed", false);

		return jsonResponse;
	}

	@RequestMapping(value="/delete", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> delete(HttpServletRequest request,
			@RequestParam(required=true) String playlistId) throws ServiceException {

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);
		Playlist playlist = user.findPlaylistById(playlistId);
		
		if (playlist != null) {
			boolean deleted = playlistManager.delete(playlist);
			jsonResponse.put("deleted", deleted);
			return jsonResponse;
		}

		
		jsonResponse.put("deleted", false);
		return jsonResponse;
	}

	@RequestMapping(value="/rename", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> rename(HttpServletRequest request,
			@RequestParam(required=true) String name,
			@RequestParam(required=true) String playlistId) throws ServiceException, ConflictException {

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);
		Playlist playlist = getPlaylist(user, playlistId);

		// if we don't find a playlist with the given name, then we can rename
		Playlist playlistByName = user.findPlaylistByName(name);
		if (playlistByName == null) {
			playlist.setName(name.trim());
			playlistManager.update(playlist);
		} else {
			// if it's a different playlist
			if (!StringUtils.equals(playlistByName.getId(), playlist.getId())) {
				throw new ConflictException("A playlist already exists with the name '" + name + "'");
			}
		}

		
		return jsonResponse;
	}

	@RequestMapping(value="/reorderVideos", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> reorderVideos(HttpServletRequest request,
			@RequestParam(required=true) String videoIds,
			@RequestParam(required=true) String playlistId) throws ServiceException {

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		User user = getUser(request);
		Playlist playlist = getPlaylist(user, playlistId);

		String[] ids = videoIds.split(",");
		playlist.orderVideos(ids);
		playlistManager.update(playlist);
		
		return jsonResponse;
	}

	@RequestMapping(value="/reorder", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> reorder(HttpServletRequest request,
			@RequestParam(required=true) String playlistIds) throws ServiceException {
		
		Map<String,Object> jsonResponse = json(HttpStatus.OK);
		
		User user = getUser(request);
		String[] ids = playlistIds.split(",");
		playlistManager.reorder(user.getUID(), ids);

		return jsonResponse;
	}

	private Playlist getPlaylist(User user, String playlistId) {
		Playlist playlist = user.findPlaylistById(playlistId);
		if (playlist == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested playlist was not found.");
		}
		return playlist;
	}
}
