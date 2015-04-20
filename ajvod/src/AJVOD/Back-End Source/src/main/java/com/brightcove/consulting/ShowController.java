package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.models.Show;
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
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;
import com.brightcove.consulting.util.WebUtils;

/**
 * Handles requests for the application Show page.
 */
@Controller
public class ShowController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowController.class);

	/**
	 * Performs multiple queries to get the desired videos for display
	 * on the Channel page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/shows/{sid}", method = RequestMethod.GET)
	public String show(Locale locale, Model model, HttpServletRequest request, HttpServletResponse response 
			,@PathVariable("cid") String channelId
			,@PathVariable("sid") String showId) {

		logger.debug("show controller. The client locale is ", locale);

		// get the requested channel and show
        Channel currentChannel = getChannel(channelId, DEFAULT_CHANNEL_KEY);
        // TODO: why are we defaulting to show 4?
		Show show = getShow(currentChannel, showId, "listening-post");

		// params for search
		String showCFValue = show.getCustomFieldValue();
		Sort sort = new Sort(Direction.NATURAL, SortProperty.CREATION_DATE);
		int carouselCount = 1;

		//retrieve videos for show episodes
		BrightcoveResponse<BrightcoveVideo> episodeCollection = retrieveEpisodesByShow(currentChannel, showCFValue, sort);  		

		//retrieve videos for show clips
		BrightcoveResponse<BrightcoveVideo> showClipCollection = retrieveShowClipsByShow(currentChannel, showCFValue, sort);
		
		//retrieve the promotional video
		BrightcoveVideo promotionalVideo = retrievePromoVideoByShow(showCFValue);
		if(promotionalVideo == null){
			promotionalVideo = episodeCollection.getBrightcoveItemCollection().get(0);
		}
		
		SearchResult episodesResult = searchResult(request, episodeCollection, carouselCount++);
		SearchResult clipsResult = searchResult(request, showClipCollection, carouselCount++);


		model.addAttribute("channels", navRepository.getChannels());
		model.addAttribute("currentChannel", currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());

		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );

		

		if (promotionalVideo != null) {
		    model.addAttribute("currentVideo", new BrightcoveVideoWrapper(promotionalVideo));
			model.addAttribute("playerId", currentChannel.getConfig("playerId") );
			model.addAttribute("playerKey", currentChannel.getConfig("playerKey") );

			BrightcoveVideoWrapper nvideo = new BrightcoveVideoWrapper(promotionalVideo);


			List<String> myTags = nvideo.getTags();

			//throw new EmptyStackException();
			for (int i=0;i < myTags.size();i++)
			{
			   String geo = new String("nogeoblock"); 
			   String tag = new String(myTags.get(i));

			   if(tag.equals(geo))
			   {
					 String pk = new String("AQ~~,AAAA1Dbg5Jk~,tLPWFvsjaQAAoN8ed2LFQvmrAPLvRbks");
					 String pid = new String("3908650057001");

					 model.addAttribute("playerKey", pk );
				  model.addAttribute("playerId", pid );
				 
			   }
			}
		}
		


		// if there's a promotional video and we don't have any episodes, populate the view for
		// a video page for the promo
		// TODO: forward or redirect the request to the video page instead of dup'ing code here
		if(promotionalVideo != null && episodeCollection.getBrightcoveItemCollection().isEmpty())
		{
			//retrieve videos for shows by channel
			episodeCollection = retrieveLargeCarouselVideos(promotionalVideo, currentChannel.getCustomFieldValue(), sort);  		
			//retrieve videos for news clips by channel
			showClipCollection = retrieveSmallCarouselVideos(promotionalVideo, currentChannel.getCustomFieldValue(), sort);

			episodesResult = searchResult(request, episodeCollection, carouselCount++);
			clipsResult = searchResult(request, showClipCollection, carouselCount++);
			
			//add channel objects to view model
			//model.addAttribute("playerId", currentChannel.getConfig("playerId") );
              //  	model.addAttribute("playerKey", currentChannel.getConfig("playerKey") );
			model.addAttribute("customPlayer", currentChannel.getConfig("customPlayer") );
			model.addAttribute("pageName", "detail" );
			
			model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
			model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
			model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );
			
        	        model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
	                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );

			model.addAttribute("largeCarouselVideos", episodesResult);
			model.addAttribute("smallCarouselVideos", clipsResult);

			// TODO:i18n
			// Handle the carousel headings differently based on the video type
			if ("programme".equals(promotionalVideo.getCustomField(videotypeCustomField)) 
					|| "programme clip".equals(promotionalVideo.getCustomField(videotypeCustomField))
					|| "promo".equals(promotionalVideo.getCustomField(videotypeCustomField))) {
				model.addAttribute("largeCarouselHeading", "Episodes" );
				model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(promotionalVideo.getCustomField(showCustomField)) );
				model.addAttribute("smallCarouselHeading", "Show Clips" );
				model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(promotionalVideo.getCustomField(showCustomField)) );
			}
			else {
				model.addAttribute("largeCarouselHeading", "News Clips" );
				model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/news");
				model.addAttribute("smallCarouselHeading", promotionalVideo.getCustomField(topicCustomField) );
				model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/topics/" + WebUtils.slugify(promotionalVideo.getCustomField(topicCustomField)) );			
			}
			
			// Return the name of the view to be rendered
			return "video/index";
		}

		//add controller objects to view model
		model.addAttribute("largeCarouselHeading", "Episodes" );
		model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + show.getKey() );
		model.addAttribute("smallCarouselHeading", "Show Clips" );
		model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + show.getKey() );
		
		model.addAttribute("largeCarouselVideos", episodesResult );
		model.addAttribute("smallCarouselVideos", clipsResult );

		model.addAttribute("currentNavItem", show);
		model.addAttribute("metaDescription", show.getName() );

		model.addAttribute("pageName", "show" );
		model.addAttribute("playerId", currentChannel.getConfig("playerId") );
                model.addAttribute("playerKey", currentChannel.getConfig("playerKey") );
		model.addAttribute("customPlayer", currentChannel.getConfig("customPlayer") );
		model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );
		// for constructing subscription title
		model.addAttribute("titlePrefix", show.getName());

		// Return the name of the view to be rendered
		return "show/index";
	}
	
	private BrightcoveVideo retrievePromoVideoByShow(String showCFValue) 
	{
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(showCustomField, showCFValue, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "promo", BrightcoveLogic.ALL));
		
		BrightcoveResponse<BrightcoveVideo> videoCollection = readApiService().
		        findVideosBySearchFields(new SearchApiPageRequest(0, 1, searchFields));
		if(videoCollection == null || videoCollection.getTotalCount() < 1)
			return null;

		return videoCollection.getBrightcoveItemCollection().get(0);
	}

	private BrightcoveResponse<BrightcoveVideo> retrieveEpisodesByShow(Channel channel, String showCFValue, Sort sort)
	{
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(channelCustomField, channel.getKey(), BrightcoveLogic.ALL));
		searchFields.add(new CustomField(showCustomField, showCFValue, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "programme", BrightcoveLogic.ALL));
		// explicitly exclude this other video type so that we don't get false positive results
		searchFields.add(new CustomField(videotypeCustomField, "programme clip", BrightcoveLogic.NOT));

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeLargeCarousel, sort, searchFields));
	}
	
	private BrightcoveResponse<BrightcoveVideo> retrieveShowClipsByShow(Channel channel, String showCFValue, Sort sort)
	{
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(channelCustomField, channel.getKey(), BrightcoveLogic.ALL));
		searchFields.add(new CustomField(showCustomField, showCFValue, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "programme clip", BrightcoveLogic.ALL));

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeSmallCarousel, sort, searchFields));
	}

	private BrightcoveResponse<BrightcoveVideo> retrieveLargeCarouselVideos(BrightcoveVideo currentVideo, String channelCFValue, Sort sort) 
	{
		if(currentVideo == null)
			return new BrightcoveExceptionResponse<BrightcoveVideo>();
	
		//Initialize the api(s)
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		
		//retrieve the video type
		String videoType = currentVideo.getCustomField(videotypeCustomField);
		
		//if this is a news video, then retrieve all the news clips otherwise return the episodes
		if(videoType != null && (videoType.equals("package") || videoType.equals("bulletin") || videoType.equals("rushes") ))
		{		
			searchFields.add(new CustomField(videotypeCustomField, "package", BrightcoveLogic.ANY));
			searchFields.add(new CustomField(videotypeCustomField, "bulletin", BrightcoveLogic.ANY));
		}
		
		//if this is a show video
		else
		{
			//retrieve the show id from the video
			String showId = currentVideo.getCustomField(showCustomField);

			if (showId == null) {
			    return new VideoResponse(0,0,0,ListUtils.EMPTY_LIST);
			}
			searchFields.add(new CustomField(showCustomField, showId, BrightcoveLogic.ALL));
			searchFields.add(new CustomField(videotypeCustomField, "programme", BrightcoveLogic.ALL));
			searchFields.add(new CustomField(videotypeCustomField, "programme clip", BrightcoveLogic.NOT));
		}

		searchFields.add(new CustomField(channelCustomField, channelCFValue, BrightcoveLogic.ALL));	

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeLargeCarousel, sort, searchFields));
	}

	private BrightcoveResponse<BrightcoveVideo> retrieveSmallCarouselVideos(BrightcoveVideo currentVideo, String channelCFValue, Sort sort) 
	{
		if(currentVideo == null)
			return new BrightcoveExceptionResponse<BrightcoveVideo>();
		
		//Initialize the api(s)
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		
		//retrieve the video type
		String videoType = currentVideo.getCustomField(videotypeCustomField);
		
		//if this is a news video, then retrieve all the channel-related news clips otherwise return the show clips
		if(videoType != null && (videoType.equals("package") || videoType.equals("bulletin") || videoType.equals("rushes") ))
		{		
			searchFields.add(new CustomField(videotypeCustomField, "package", BrightcoveLogic.ANY));
			searchFields.add(new CustomField(videotypeCustomField, "bulletin", BrightcoveLogic.ANY));
		}
		
		//if this is a show video
		else
		{
			//retrieve the show id from the video
			String showId = currentVideo.getCustomField(showCustomField);
            if (showId == null) {
                return new VideoResponse(0,0,0,ListUtils.EMPTY_LIST);
            }
			searchFields.add(new CustomField(showCustomField, showId, BrightcoveLogic.ALL));
			searchFields.add(new CustomField(videotypeCustomField, "programme clip", BrightcoveLogic.ALL));
		}
		
		searchFields.add(new CustomField(channelCustomField, channelCFValue, BrightcoveLogic.ALL));

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeSmallCarousel, sort, searchFields));
	}


}
