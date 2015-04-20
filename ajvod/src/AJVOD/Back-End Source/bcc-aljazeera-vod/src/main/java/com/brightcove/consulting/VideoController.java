package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.EPG;
import com.brightcove.consulting.alj.models.EPG.ShowJson;
import com.brightcove.consulting.alj.models.EPGFeed;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.services.EpgFeedManager;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;
import com.brightcove.consulting.util.WebUtils;

/**
 * Handles requests for the application video page.
 */
@Controller
public class VideoController extends YBTVBaseController {
	
	@Autowired
	private EpgFeedManager epgFeedManager;
	
	@Autowired
	LocaleResolver localeResolver;
	
	private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	/**
	 * Performs a simple query to get the desired video for display
	 * on the video page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
    @RequestMapping(value = "/channels/{cid}/videos/{vid}", method = RequestMethod.GET)
	public String video(Locale locale, Model model, 
	        HttpServletRequest request, HttpServletResponse response
			, @PathVariable("cid") String channelId
			, @PathVariable("vid") String videoId) {
		
		return videoHandler(locale, model, request, response, channelId, videoId);
	}
	

	/**
	 * Performs a simple query to get the desired video for display
	 * on the video page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
    @RequestMapping(value = "/channels/{cid}/videos/{vname}/{vid}", method = RequestMethod.GET)
	public String video(Locale locale, Model model, 
	        HttpServletRequest request, HttpServletResponse response
			, @PathVariable("cid") String channelId
			, @PathVariable("vname") String videoName
			, @PathVariable("vid") String videoId) {
		
		return videoHandler(locale, model, request, response, channelId, videoId);
	}

	/**
	 * Performs a simple query to get the desired video for display
	 * on the video page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/live", method = RequestMethod.GET)
	public String live(Locale locale, Model model, 
	        HttpServletRequest request, HttpServletResponse response
			, @PathVariable("cid") String channelId) {
		
		Channel currentChannel = getChannel(channelId, DEFAULT_CHANNEL_KEY);
				
		Sort sort = new Sort(Direction.NATURAL, SortProperty.MODIFIED_DATE);
		int carouselCount = 1;

		BrightcoveVideo currentVideo = fetchSingleVideo(currentChannel.getConfig("liveAssetID"));

		//retrieve epg shows
		EPGFeed feed = epgFeedManager.getFeed(channelId);
		
		//set the current locale
		String channelLang = currentChannel.getLanguage();
		if(StringUtils.isEmpty(channelLang)){
			locale = localeResolver.resolveLocale(request);
		}
		else{
			locale = new Locale(channelLang);
		}
			
		List<ShowJson> shows = null;
		if (feed != null) {
		    EPG epg = new EPG(feed);
		    epg.setLocale(locale);
		    DateTime startTime = DateTime.now().withZone(getTimeZone(request));
		    shows = epg.setStartTime(startTime).getLineup(3);
		}
		model.addAttribute("epgShows", shows);
		
		//retrieve videos for shows by channel
		BrightcoveResponse<BrightcoveVideo> episodeCollection = retrieveSmallCarouselVideos(currentVideo, currentChannel.getCustomFieldValue(), sort);  		

		//retrieve videos for news clips by channel
		BrightcoveResponse<BrightcoveVideo> clipCollection = retrieveLiveVideos(currentVideo, currentChannel.getCustomFieldValue(), sort);
		
		SearchResult episodeResult = searchResult(request, episodeCollection, carouselCount++);
		SearchResult clipsResult = searchResult(request, clipCollection, carouselCount++);
		
		//add channel objects to view model
		if (currentVideo != null) {
		    model.addAttribute("currentVideo", new BrightcoveVideoWrapper(currentVideo));
		}
		model.addAttribute("channels", navRepository.getChannels());
		model.addAttribute("currentChannel", currentChannel );
		model.addAttribute("navMap", navRepository.getChannels().get(channelId).getChildren());
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );
		
		model.addAttribute("playerId", currentChannel.getConfig("livePlayerId") );
                model.addAttribute("playerKey", currentChannel.getConfig("livePlayerKey") );
                model.addAttribute("livePlayerId", currentChannel.getConfig("livePlayerId") );
                model.addAttribute("livePlayerKey", currentChannel.getConfig("livePlayerKey") );
                model.addAttribute("viralPlayerId", currentChannel.getConfig("viralPlayerId") );
                model.addAttribute("viralPlayerKey", currentChannel.getConfig("viralPlayerKey") );

		model.addAttribute("customPlayer", currentChannel.getConfig("customPlayer") );
		model.addAttribute("pageName", "live" );

		model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );
                model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );

		
		model.addAttribute("largeCarouselVideos", episodeResult );
		model.addAttribute("smallCarouselVideos", clipsResult );

		// TODO:i18n
		// Handle the carousel headings differently based on the video type
		if ("programme".equals(currentVideo.getCustomField(videotypeCustomField)) 
				|| "programme clip".equals(currentVideo.getCustomField(videotypeCustomField))
				|| "promo".equals(currentVideo.getCustomField(videotypeCustomField))) {
			model.addAttribute("largeCarouselHeading", "Episodes" );
			model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(currentVideo.getCustomField(showCustomField)) );
			model.addAttribute("smallCarouselHeading", "Show Clips" );
			model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(currentVideo.getCustomField(showCustomField)) );
		}
		else {
			model.addAttribute("largeCarouselHeading", "News Clips" );
			model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/news");
			model.addAttribute("smallCarouselHeading", currentVideo.getCustomField(topicCustomField) );
			model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/topics/" + WebUtils.slugify(currentVideo.getCustomField(topicCustomField)) );			
		}
		if (currentVideo.getCustomField(topicCustomField) == null && currentVideo.getCustomField(showCustomField) == null) {
			model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey());
		}
		
		// Return the name of the view to be rendered
		return "live/index";
	}
	
	// Performs the common steps for both of the video instances
	private String videoHandler(Locale locale, Model model,
			HttpServletRequest request, HttpServletResponse response,
			String channelId, String videoId) {
		response.setCharacterEncoding("UTF-8");

		//Set the current channel
		Channel currentChannel = getChannel(channelId, DEFAULT_CHANNEL_KEY);
				
		//Set the sort logic
		Sort sort = new Sort(Direction.NATURAL, SortProperty.MODIFIED_DATE);
		int carouselCount = 1;

		//retrieve single video
		//TODO: Extra video fields for topics and related links
		BrightcoveVideo currentVideo = fetchSingleVideo(videoId);

		//retrieve videos for shows by channel
		BrightcoveResponse<BrightcoveVideo> episodeCollection = retrieveLargeCarouselVideos(currentVideo, currentChannel.getCustomFieldValue(), sort);  		

		//retrieve videos for news clips by channel
		BrightcoveResponse<BrightcoveVideo> clipCollection = retrieveSmallCarouselVideos(currentVideo, currentChannel.getCustomFieldValue(), sort);

		SearchResult relatedResult = null;
		if (currentVideo != null) {
            relatedResult = searchResult(request, findRelatedVideos(currentVideo), carouselCount++);
		}
		
		SearchResult episodeResult = searchResult(request, episodeCollection, carouselCount++);
		SearchResult clipsResult = searchResult(request, clipCollection, carouselCount++);
		
		//add channel objects to view model
		if (currentVideo != null) {
		    model.addAttribute("currentVideo", new BrightcoveVideoWrapper(currentVideo));
		}
		model.addAttribute("channels", navRepository.getChannels());
		model.addAttribute("currentChannel", currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );
		
                model.addAttribute("playerId", currentChannel.getConfig("playerId") );
                model.addAttribute("playerKey", currentChannel.getConfig("playerKey") );
                model.addAttribute("livePlayerId", currentChannel.getConfig("livePlayerId") );
                model.addAttribute("livePlayerKey", currentChannel.getConfig("livePlayerKey") );
                model.addAttribute("viralPlayerId", currentChannel.getConfig("viralPlayerId") );
                model.addAttribute("viralPlayerKey", currentChannel.getConfig("viralPlayerKey") );

		model.addAttribute("customPlayer", currentChannel.getConfig("customPlayer") );
		model.addAttribute("publisherId", publisherId );
		
		model.addAttribute("pageName", "detail" );
		
		model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );
                model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );
		
		model.addAttribute("largeCarouselVideos", episodeResult);
		model.addAttribute("smallCarouselVideos", clipsResult);
		model.addAttribute("recommendedVideos", relatedResult);

		// TODO:i18n
		// Handle the carousel headings differently based on the video type
		if ("programme".equals(currentVideo.getCustomField(videotypeCustomField)) 
				|| "programme clip".equals(currentVideo.getCustomField(videotypeCustomField))
				|| "promo".equals(currentVideo.getCustomField(videotypeCustomField))) {
			model.addAttribute("largeCarouselHeading", "Episodes" );
			model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(currentVideo.getCustomField(showCustomField)) );
			model.addAttribute("smallCarouselHeading", "Show Clips" );
			model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows/" + WebUtils.slugify(currentVideo.getCustomField(showCustomField)) );
		}
		else {
			model.addAttribute("largeCarouselHeading", "News Clips" );
			model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/news");
			model.addAttribute("smallCarouselHeading", currentVideo.getCustomField(topicCustomField) );
			model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/topics/" + WebUtils.slugify(currentVideo.getCustomField(topicCustomField)) );			
		}

        // used to create subscription title
        model.addAttribute("titlePrefix", currentVideo.getShowText());

		// Return the name of the view to be rendered
		return "video/index";
	}
	
	private BrightcoveResponse<BrightcoveVideo> findRelatedVideos(BrightcoveVideo video) {
	    Sort sort = new Sort(Direction.DESC, SortProperty.PUBLISH_DATE);
	    BrightcoveVideoRequest request = new BrightcoveVideoRequest(0, pageSizeSmallCarousel, sort, video.getId() + "");
	    return readApiService().findRelatedVideos(request);
	}

	@SuppressWarnings("unchecked")
    private BrightcoveResponse<BrightcoveVideo> retrieveLargeCarouselVideos(BrightcoveVideo currentVideo, String channelCFValue, Sort sort) 
	{
		if(currentVideo == null)
			return new BrightcoveExceptionResponse<BrightcoveVideo>();
	
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

	@SuppressWarnings("unchecked")
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
		    // TODO: this was being displayed as 'shows' but was just a duplicate query for news clips on the news video page.
		    //  removing this for now as a workaround but will need to review whether or not there needs to be some kind of videos here.
            return new VideoResponse(0,0,0,ListUtils.EMPTY_LIST);
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
	
	@SuppressWarnings("unchecked")
	private BrightcoveResponse<BrightcoveVideo> retrieveLiveVideos(BrightcoveVideo currentVideo, String channelCFValue, Sort sort) 
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
		    // TODO: this was being displayed as 'shows' but was just a duplicate query for news clips on the news video page.
		    //  removing this for now as a workaround but will need to review whether or not there needs to be some kind of videos here.
            return new VideoResponse(0,0,0,ListUtils.EMPTY_LIST);
		}
		
		//if this is a show video
		searchFields.add(new CustomField(videotypeCustomField, "programme clip", BrightcoveLogic.ALL));
		searchFields.add(new CustomField(channelCustomField, channelCFValue, BrightcoveLogic.ALL));
		
		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeSmallCarousel, sort, searchFields));
	}


	
}
