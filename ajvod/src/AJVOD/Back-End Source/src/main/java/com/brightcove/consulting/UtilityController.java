package com.brightcove.consulting;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.api.models.BrightcovePlaylist;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.query.FindAllVideos;
import com.brightcove.consulting.api.query.FindPlaylistsByPlayer;
import com.brightcove.consulting.api.query.FindVideoById;
import com.brightcove.consulting.api.query.FindVideosByPlaylist;
import com.brightcove.consulting.api.query.PlaylistApi;
import com.brightcove.consulting.api.query.VideoApi;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.request.PlaylistPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

/**
 * Handles requests for the application test page.
 */
@Controller
public class UtilityController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(UtilityController.class);
	
	/**
	 * Performs a simple query to get the first page of videos for display
	 * on the test page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/testFunc/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {

		// Create a page request
		BrightcovePageRequest bpr = new BrightcovePageRequest(0, pageSize);
		// Create the appropriate MediaApi
		VideoApi<BrightcoveVideo, BrightcovePageRequest> fav = new FindAllVideos(readToken);
		// Execute the query to get a response
		BrightcoveResponse<BrightcoveVideo> bresp = fav.query(bpr);
		// Pass the returned items to the view
		model.addAttribute("videos", bresp.getBrightcoveItemCollection() );
		
		// Pass the channel list to the view
	 	model.addAttribute("channels", navRepository.getChannels() );

		//retrieve playlists by player id
		String playerId = "11892216001";
		PlaylistPageRequest ppr = new PlaylistPageRequest(0, 10, playerId);
		PlaylistApi<BrightcovePlaylist, PlaylistPageRequest> fpp = new FindPlaylistsByPlayer(readToken);
		BrightcoveResponse<BrightcovePlaylist> presp = fpp.query(ppr);
		model.addAttribute("playlists", presp.getBrightcoveItemCollection() );
		
		//retrieve single video
		String videoId = "8371442001";
		BrightcoveVideoRequest bvr = new BrightcoveVideoRequest(0, pageSize, new Sort(Direction.NATURAL, SortProperty.CREATION_DATE),  videoId);
		VideoApi<BrightcoveVideo, BrightcoveVideoRequest> fv = new FindVideoById(readToken);
		BrightcoveResponse<BrightcoveVideo> bresp3 = fv.query(bvr);
		model.addAttribute("video", bresp3.getBrightcoveItemCollection().get(0) );		
		
		//retrieve videos by playlist id
		String playlistId = "8324665001";
		BrightcoveVideoRequest bvr2 = new BrightcoveVideoRequest(0, pageSize, new Sort(Direction.NATURAL, SortProperty.CREATION_DATE),  playlistId);
		VideoApi<BrightcoveVideo, BrightcoveVideoRequest> fvp = new FindVideosByPlaylist(readToken);
		BrightcoveResponse<BrightcoveVideo> bresp2 = fvp.query(bvr2);
		model.addAttribute("playlistVideos", bresp2.getBrightcoveItemCollection() );
		
		// Return the name of the view to be rendered
		return "home";
	}
		
	@RequestMapping(value = "/fake-ajax/{dataType}", method = RequestMethod.GET)
	public @ResponseBody String ajax(Model model, HttpServletResponse response, @PathVariable("dataType") String dataType) {
		String strJsonResponse = "";

		int dataTypeEnum = 0;
		if (dataType.equals("allshows")) {
			dataTypeEnum = 1;
		}
		else if (dataType.equals("search")) {
			dataTypeEnum = 2;
		} 
		
		switch (dataTypeEnum) {
		case 1 :
			strJsonResponse = "[ { \"thumbnail\": \"path/to/image.jpg\", \"title\": \"title 1\", \"description\": \"test description\", \"url\": \"/path/to/show/\" }, { \"thumbnail\": \"path/to/image.jpg\", \"title\": \"title 2\", \"description\": \"test description\", \"url\": \"/path/to/show/\" }, { \"thumbnail\": \"path/to/image.jpg\", \"title\": \"title 3\", \"description\": \"test description\", \"url\": \"/path/to/show/\" } ]";
			break;

		case 2 :
			strJsonResponse = "{ \"results\": [ { \"title\": \"Video title 1\", \"show\": \"show name\", \"duration\": 156000, \"date\": 1362577054338, \"description\": \"Video short description\", \"thumbnail\": \"http://path/to/image.jpg\" }, { \"title\": \"Video title 2\", \"show\": \"show name\", \"duration\": 156000, \"date\": 1362577054338, \"description\": \"Video short description\", \"thumbnail\": \"http://path/to/image.jpg\" } ], \"categories\": [ { \"title\": \"Related topic or show\", \"url\": \"/path/to/topic/or/show\" }, { \"title\": \"Related topic or show\", \"url\": \"/path/to/topic/or/show\" } ] }";			
			break;
		
		default:
			strJsonResponse = "[ { } ]";			
			
		}
		
		return jsonResponse(strJsonResponse, response);
	}
	
}
