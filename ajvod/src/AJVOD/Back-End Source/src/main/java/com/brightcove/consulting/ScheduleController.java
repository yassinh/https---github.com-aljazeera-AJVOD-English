package com.brightcove.consulting;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.brightcove.consulting.alj.models.Channel;


/**
 * Handles requests for the application video page.
 */
@Controller
public class ScheduleController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
	
	/**
	 * Test url for triggering the epg page
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
    @RequestMapping(value = "channels/{cid}/epg", method = RequestMethod.GET)
	public String video(Locale locale, Model model, 
			HttpServletRequest request, 
			HttpServletResponse response,
			@PathVariable("cid") String channelId) {
		
		response.setCharacterEncoding("UTF-8");

		//Set the current channel
		Channel currentChannel = getChannel(channelId, DEFAULT_CHANNEL_KEY);

		//add channel objects to view model
		model.addAttribute("channels", navRepository.getChannels());
		model.addAttribute("currentChannel", currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );
		
		model.addAttribute("playerId", playerId );
		model.addAttribute("publisherId", publisherId );
		
                model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );


		model.addAttribute("pageName", "epg" );

		// Return the name of the view to be rendered
		return "epg/index";
	}

}
