package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.brightcove.consulting.alj.json.VideoJson;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.NavigationTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.query.FindVideosBySearchFields;
import com.brightcove.consulting.api.query.VideoApi;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

/**
 * Handles requests for the application all shows page.
 */
@Controller
public class AllShowsController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(AllShowsController.class);

	/**
	 * Retrieve navigation map to get the desired videos for display
	 * on the All Shows page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/shows", method = RequestMethod.GET)
	public String allshows(Locale locale, Model model, HttpServletResponse response, 
			@PathVariable("cid") String channelId) {
		
//		response.setCharacterEncoding("UTF-8");
		
		//Set the current channel
		Channel currentChannel = getChannel(channelId);
		
		NavigationItem navItem = currentChannel.getChildren(NavigationTypeEnum.AllPages.toString()).get("shows");
		if (navItem == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested "+ NavigationTypeEnum.AllPages.toString().toLowerCase() +" was not found.");
		}
		
		//retrieve all the shows from the navigation map based on channel
		Map<String, NavigationItem> showGenres = currentChannel.getChildren().get("ShowGenre");
		
		//Pass the show list to the view
	 	model.addAttribute("showGenres", showGenres );
	 	model.addAttribute("channels", navRepository.getChannels() );
		model.addAttribute("currentChannel", currentChannel );
		model.addAttribute("metaDescription", currentChannel.getName() );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("currentNavItem", navItem);
		
		model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );
		
		model.addAttribute("webRoot", webRoot );
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("pageName", "allshows" );

		model.addAttribute("publisherId", publisherId );
                model.addAttribute("playerId", currentChannel.getConfig("playerId") );
                model.addAttribute("playerKey", currentChannel.getConfig("playerKey") );
                model.addAttribute("customPlayer", currentChannel.getConfig("customPlayer") );
		model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );

		// Return the name of the view to be rendered
		return "allshows/index";
	}
	
	/**
	 * Perform multiple AJAX queries to get the desired video episodes for display
	 * on the All Shows page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/channels/{cid}/episodes/{sid}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody VideoJsonList ajax(HttpServletResponse response, Model model, 
	        @PathVariable("cid") String channelId, @PathVariable("sid") String showId) {
	
		response.setCharacterEncoding("UTF-8");

		//TODO: default to featured id
		if(showId == null || showId.length() < 1)
		{
			showId = "show4";
		}

		// validate channel is correct
		getChannel(channelId);

		//Set the sort criteria
		Sort sort = new Sort(Direction.NATURAL, SortProperty.CREATION_DATE);
		
		//retrieve videos for shows by channel
		BrightcoveResponse<BrightcoveVideo> episodeCollection = retrieveEpisodesByShow(showId, sort); 

		// wraps videos in json objects for rendering
		Channel channel = navRepository.getChannel(channelId);
		Collection videosJson = VideoJson.convert(episodeCollection.getBrightcoveItemCollection(), channel);
		
		return new VideoJsonList(videosJson);
	}
	
	
 	private BrightcoveResponse<BrightcoveVideo> retrieveEpisodesByShow(String showId, Sort sort)
	{
		//Initialize the api(s)
		VideoApi<BrightcoveVideo, SearchApiPageRequest> videoApi = new FindVideosBySearchFields(readToken);
				
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(showCustomField, showId, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "programme", BrightcoveLogic.ALL));

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, 3, sort, searchFields));
	}

 	public static class VideoJsonList extends ArrayList<VideoJson> {

		public VideoJsonList() {
			super();
		}

		public VideoJsonList(Collection<? extends VideoJson> arg0) {
			super(arg0);
		}
 		
 	}
}
