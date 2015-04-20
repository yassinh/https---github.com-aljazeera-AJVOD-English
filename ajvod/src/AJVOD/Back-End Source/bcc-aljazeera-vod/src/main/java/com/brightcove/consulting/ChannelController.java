package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;

/**
 * Handles requests for the application Channel page.
 */
@Controller
public class ChannelController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);
	private static final int MAIN_CAROUSEL_SIZE = 6;
	
	/**
	 * Performs multiple queries to get the desired videos for display
	 * on the Channel page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}", method = RequestMethod.GET)
	public String channel(Locale locale, Model model, 
			HttpServletResponse response, HttpServletRequest request,
			@PathVariable("cid") String channelId) {
		
		logger.debug("channel controller. The client locale is ", locale);

		Channel currentChannel = getChannel(channelId, DEFAULT_CHANNEL_KEY); 

		Sort sort = new Sort(Direction.DESC, SortProperty.MODIFIED_DATE);
		int carouselCount = 1;

		//Retrieve current channel custom field value
		String custFieldValue = currentChannel.getCustomFieldValue();
		
		//Set the featured playlist id
		String featuredPlaylistId = currentChannel.getFeaturedPlaylistId();


		//retrieve videos for shows by channel
		BrightcoveResponse<BrightcoveVideo> showsCollection = retrieveLargeCarouselVideos(custFieldValue, sort); 		

		//retrieve videos for news clips by channel
		BrightcoveResponse<BrightcoveVideo> newsClipsCollection = retrieveSmallCarouselVideos(custFieldValue, sort);
		
		//retrieve videos for main carousel
		BrightcoveResponse<BrightcoveVideo> featuredVideos = retrieveFeaturedVideos(featuredPlaylistId, sort, showsCollection, newsClipsCollection);

		SearchResult showsResult = searchResult(request, showsCollection, carouselCount++);
		SearchResult newsResult = searchResult(request, newsClipsCollection, carouselCount++);
		
		//add channel objects to view model
		model.addAttribute("largeCarouselHeading", "Shows" );
		model.addAttribute("largeCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/shows");
		model.addAttribute("smallCarouselHeading", "News Clips" );
		model.addAttribute("smallCarouselHeadingLink", "/channels/" + currentChannel.getKey() + "/news");
		
		model.addAttribute("channels", navRepository.getChannels());
		model.addAttribute(ModelConstants.CURRENT_CHANNEL, currentChannel );
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
		
                model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );
		model.addAttribute("pageName", "channel" );
		model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("metaDescription", currentChannel.getName() );

		model.addAttribute("largeCarouselVideos", showsResult);
		model.addAttribute("smallCarouselVideos", newsResult);
		model.addAttribute("featuredVideos", wrap(featuredVideos.getBrightcoveItemCollection()) );

		// Return the name of the view to be rendered
		return "channel/index";
	}
	
	private BrightcoveResponse<BrightcoveVideo> retrieveLargeCarouselVideos(String channel, Sort sort)
	{
	    
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(channelCustomField, channel, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "programme", BrightcoveLogic.ALL));
		SearchApiPageRequest searchRequest = new SearchApiPageRequest(0, pageSizeLargeCarousel, sort, searchFields);

		return readApiService().findVideosBySearchFields(searchRequest);
	}
	
	private BrightcoveResponse<BrightcoveVideo> retrieveSmallCarouselVideos(String channel, Sort sort)
	{
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(channelCustomField, channel, BrightcoveLogic.ALL));
		searchFields.add(new CustomField(videotypeCustomField, "package", BrightcoveLogic.ALL));

		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSizeSmallCarousel, sort, searchFields));
	}
	
	private BrightcoveResponse<BrightcoveVideo> retrieveFeaturedVideos(String featuredPlaylistId, Sort sort, BrightcoveResponse<BrightcoveVideo> showsCollection, BrightcoveResponse<BrightcoveVideo> newsClipsCollection) 
	{
		//retrieve featured playlist
		BrightcoveResponse<BrightcoveVideo> featuredVideos = null;
		
		if (featuredPlaylistId != null) 
			featuredVideos = readApiService().findVideosByPlaylist(new BrightcoveVideoRequest(0, pageSize, sort,  featuredPlaylistId));		
		
		if(featuredVideos != null && featuredVideos.getTotalCount() > 0)
			return featuredVideos;
		
		if(showsCollection.getTotalCount() < 1 && newsClipsCollection.getTotalCount() < 1)
			return featuredVideos;
		
		//create the sorted list if there isn't a featured playlist		
		return createFeaturedPlaylist(sort, showsCollection, newsClipsCollection); 
	}

	private BrightcoveResponse<BrightcoveVideo> createFeaturedPlaylist(Sort sort, BrightcoveResponse<BrightcoveVideo> showsCollection, BrightcoveResponse<BrightcoveVideo> newsClipsCollection) {
		List<BrightcoveVideo> featuredVideoCollection = new ArrayList<BrightcoveVideo>();
		
		//gather videos from the collection of shows
		List<BrightcoveVideo> tempCollection = showsCollection.getBrightcoveItemCollection();			
		for(int i = 0; i < tempCollection.size(); i++)
		{
			if(i > 2)
				break;
			
			featuredVideoCollection.add(tempCollection.get(i));
		}
		
		//gather videos from the collection of news clips
		tempCollection = newsClipsCollection.getBrightcoveItemCollection();			
		for(int i = 0; i < tempCollection.size(); i++)
		{
			if(featuredVideoCollection.size() >= MAIN_CAROUSEL_SIZE)
				break;
			
			featuredVideoCollection.add(tempCollection.get(i));
		}
		
		//sort the videos collected
		featuredVideoCollection = sortBrightcoveVideos(sort, featuredVideoCollection);
		
		return new VideoResponse((long)featuredVideoCollection.size(), 0, pageSize, featuredVideoCollection);
	}

	private List<BrightcoveVideo> sortBrightcoveVideos(Sort sort, List<BrightcoveVideo> videoCollection) 
	{
		if(videoCollection == null || videoCollection.size() < 1)
			return videoCollection;
		
		Comparator<BrightcoveVideo> comparator = null;

		switch(sort.getSortProperty())
		{
			case DISPLAY_NAME:
				comparator = new Comparator<BrightcoveVideo>() {
				    public int compare(BrightcoveVideo bcVideo1, BrightcoveVideo bcVideo2) {
				    	return bcVideo1.getName().compareTo(bcVideo2.getName());
				    }
				};
				
				Collections.sort(videoCollection, comparator);
				break;
			
			case PLAYS_TRAILING_WEEK:
				comparator = new Comparator<BrightcoveVideo>() {
				    public int compare(BrightcoveVideo bcVideo1, BrightcoveVideo bcVideo2) {
				    	return 0;
				    }
				};
				
				Collections.sort(videoCollection, comparator);
				break;
			
			case MODIFIED_DATE:
				comparator = new Comparator<BrightcoveVideo>() {
				    public int compare(BrightcoveVideo bcVideo1, BrightcoveVideo bcVideo2) {
				    	if(bcVideo2.getDate().before(bcVideo1.getDate()))
				    		return 0;
				    	return 1;
				    }
				};
				
				Collections.sort(videoCollection, comparator);
				break;

			default:
				comparator = new Comparator<BrightcoveVideo>() {
				    public int compare(BrightcoveVideo bcVideo1, BrightcoveVideo bcVideo2) {
				    	if(bcVideo2.getDate().before(bcVideo1.getDate()))
				    		return 0;
				    	return 1;
				    }
				};
				
				Collections.sort(videoCollection, comparator);
		}
		
		return videoCollection;
	}

}
