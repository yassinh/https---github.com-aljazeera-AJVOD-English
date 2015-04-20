package com.brightcove.consulting;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Performs a simple query to get the first page of videos for display
	 * on the home page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model, HttpServletResponse response) {
		
		response.setCharacterEncoding("UTF-8");

		// Create a page request
		BrightcovePageRequest bpr = new BrightcovePageRequest(0, pageSize);
		// Execute the query to get a response
		BrightcoveResponse<BrightcoveVideo> videoCollection = readApiService().findAllVideos(bpr);
	 	
	 	//retrieve featuredPlaylist
		BrightcoveResponse<BrightcoveVideo> featuredVideos = fetchSinglePlaylist(featuredPlaylistId);
		List<BrightcoveVideoWrapper> featuredVideosList = wrap(featuredVideos.getBrightcoveItemCollection());

		//add controller objects to view model
		model.addAttribute("videos", wrap(videoCollection.getBrightcoveItemCollection()));

		// get channels from NavRepository
		model.addAttribute("channels", navRepository.getChannels() );
		model.addAttribute("featuredVideolist", featuredVideosList);
		model.addAttribute("currentVideo", featuredVideosList.get(0) );
		model.addAttribute("channelName", "Al Jazeera Video On Demand" );
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );
		
		model.addAttribute("publisherId", publisherId );

                // TODO: These are being hardcoded here, but we're removing the player on the homepage in Epic 9, so this is a one-epic holdover
                model.addAttribute("playerId", "2695936593001" );
                model.addAttribute("playerKey", "AQ~~,AAAA1Dbg5Jk~,tLPWFvsjaQA_yxZ9itQgk6U4WgjrsCwO" );
		
		model.addAttribute("pageName", "home" );
		model.addAttribute("currentChannel", null );
		model.addAttribute("googleAcct", null );
		model.addAttribute("metaDescription", null );

		// Return the name of the view to be rendered
		return "home/index";
	}

	/**
	 * Performs multiple queries to get the desired videos for display
	 * on the Channel page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/about", method = RequestMethod.GET)
	public String about(Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) {
		
		logger.debug("home controller (about handler). The client locale is ", locale);
		
		Channel currentChannel = getChannel(DEFAULT_CHANNEL_KEY); 

		//add channel objects to view model
		model.addAttribute("channels", navRepository.getChannels());
		//model.addAttribute(ModelConstants.CURRENT_CHANNEL, currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );

		model.addAttribute("pageName", "about" );
		model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("metaDescription", currentChannel.getName() );
		model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );

		// Return the name of the view to be rendered
		return "about/index";
	}
	
}
