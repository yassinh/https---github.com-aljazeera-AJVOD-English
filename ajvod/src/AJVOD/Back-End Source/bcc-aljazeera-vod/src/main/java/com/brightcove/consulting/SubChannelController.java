package com.brightcove.consulting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.NavigationTypeEnum;
import com.brightcove.consulting.alj.models.Pagination;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.utils.SearchFieldsBuilder;
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

/**
 * Handles requests for the application sub channel pages.
 */
@Controller
public class SubChannelController extends YBTVBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(SubChannelController.class);

	/**
	 * Performs a simple query to get the desired videos for display
	 * on the Topic page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/topics/{tid}", method = RequestMethod.GET)
	public String topics(Locale locale, Model model, HttpServletRequest pRequest
			, HttpServletResponse response
			, @PathVariable("cid") String channelId, @PathVariable("tid") String topicId
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey
    		, @RequestParam(value = "newsOnly", defaultValue = "false") boolean newsOnly) {

	    // used to create subscription title
	    if (newsOnly) {
	        model.addAttribute("titlePrefixTerm", "newsClips");
	    } else {
	        model.addAttribute("titlePrefixTerm", "topics");
	    }

		response.setCharacterEncoding("UTF-8");
		return commonHandler(pRequest, model, channelId, topicId, NavigationTypeEnum.Topic, page, sortKey, newsOnly);		
	}
	
	/**
	 * Performs a simple query to get the desired videos for display
	 * on the Show Genre page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/genres/{genreId}", method = RequestMethod.GET)
	public String genre(Locale locale, Model model, HttpServletRequest pRequest
			, HttpServletResponse response
			, @PathVariable("cid") String channelId, @PathVariable("genreId") String genreId
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey
    		, @RequestParam(value = "newsOnly", defaultValue = "false") boolean newsOnly) {

		response.setCharacterEncoding("UTF-8");
		return commonHandler(pRequest, model, channelId, genreId, NavigationTypeEnum.ShowGenre, page, sortKey, newsOnly);
	}
	
	/**
	 * Performs a simple query to get the desired videos for display
	 * on the News Clip page.
	 * 
	 * @param locale the user's Locale, made available by the Spring framework
	 * @param model the model made available by the Spring framework, which
	 * will be used to pass data to the view  
	 */
	@RequestMapping(value = "/channels/{cid}/regions/{rId}", method = RequestMethod.GET)
	public String region(Locale locale, Model model, HttpServletRequest pRequest
			, HttpServletResponse response
			, @PathVariable("cid") String channelId, @PathVariable("rId") String regionId
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey
    		, @RequestParam(value = "newsOnly", defaultValue = "false") boolean newsOnly) {

		response.setCharacterEncoding("UTF-8");
		return commonHandler(pRequest, model, channelId, regionId, NavigationTypeEnum.Region, page, sortKey, newsOnly);
	}

    @RequestMapping(value = "/channels/{cid}/events", method = RequestMethod.GET)
    public String events(Locale locale, Model model, HttpServletRequest pRequest
            , HttpServletResponse response
            , @PathVariable("cid") String channelId
            , @RequestParam(value = "page", defaultValue = "1") int page
            , @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey) {


        return commonHandler(pRequest, model, channelId, "event", NavigationTypeEnum.Event, page, sortKey, false);
    }

	@RequestMapping(value = "/channels/{cid}/news", method = RequestMethod.GET)
	public String news(Locale locale, Model model, HttpServletRequest pRequest
			, HttpServletResponse response
			, @PathVariable("cid") String channelId
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey) {

		response.setCharacterEncoding("UTF-8");

        // prepare the paging and sorting values
        page--;
        // TODO: use the passed parameter
        SortProperty sortProperty = getSortProperty(sortKey);
        Sort sort = new Sort(Direction.DESC, sortProperty);

		String routeName = "news";
		
		VideoApi<BrightcoveVideo, SearchApiPageRequest> videoApi = new FindVideosBySearchFields(readToken);
		
		Channel currentChannel = getChannel(channelId);
		
		NavigationItem navItem = currentChannel.getChildren(NavigationTypeEnum.AllPages.toString()).get(routeName);
		if (navItem == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested "+ NavigationTypeEnum.AllPages.toString().toLowerCase() +" was not found.");
		}

		// fetch by itemId plus channel id
		BrightcoveResponse<BrightcoveVideo> videoCollection = null;

		videoCollection = fetchVideosByChannel("", "", channelId, sort, page, pageSize, true);
		
		//Set the list of channels
		Map<String, Channel> channels = navRepository.getChannels();
		
		//Set the paging
		Pagination pagination = new Pagination(page + 1, (int)Math.ceil((double) videoCollection.getTotalCount()/pageSize), null);
		
		SearchResult sr = searchResult(pRequest, videoCollection);

		//add controller objects to view model
		model.addAttribute("totalCount", videoCollection.getTotalCount() );
		//model.addAttribute("itemId", "all-news" );
		model.addAttribute("currentPage", page + 1 );
		model.addAttribute("currentSort", sort.getSortProperty().toString() );
		model.addAttribute("routeName", routeName );
		model.addAttribute("searchResult", sr );
		model.addAttribute("channels", channels );
		model.addAttribute(ModelConstants.CURRENT_CHANNEL, currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("currentNavItem", navItem);
		model.addAttribute("metaDescription", navItem.getDescription());
		
        model.addAttribute("videoStartIndex", (page * pageSize) + 1);
        model.addAttribute("videoEndIndex", Math.min((page + 1) * pageSize, videoCollection.getTotalCount()));
        
        model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );
                model.addAttribute("hideEvents", currentChannel.getConfig("hideEvents") );
                model.addAttribute("hideLive", currentChannel.getConfig("hideLive") );

		model.addAttribute("pagination", pagination );
		
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

		model.addAttribute("pageName", "topic" );

		// used to create subscription title
		model.addAttribute("titlePrefixTerm", "newsClips");

		// Return the name of the view to be rendered
		return "subchannel/index";

	}

	// Performs the common steps for each of these instances of the SubChannel template 
	private String commonHandler(HttpServletRequest request, Model model,
	            String channelId, String itemId, 
				NavigationTypeEnum navType, int page, String sortKey, boolean newsOnly) {
		
        // prepare the paging and sorting values
        page--;
        
        // TODO: use the passed parameter
        SortProperty sortProperty = getSortProperty(sortKey);
        Sort sort = new Sort(Direction.DESC, sortProperty);
		
		//TODO: handle the item id: either error or default to featured
		if(itemId == null || itemId.length() < 1) {
			itemId = "politics";
		}

		//retrieve videos for shows for this nav item
		String customFieldName = null;
		String routeName = null;
		String pageName = "topic";
		switch (navType) {
			case Topic:
				customFieldName = topicCustomField;
				routeName = "topics";
				break;
			case ShowGenre:
				customFieldName = showGenreCustomField;
				routeName = "genres";
				break;
			case Region:
				customFieldName = regionCustomField;
				routeName = "regions";
				break;
			case Event:
			    customFieldName = "eventHero";
			    routeName = "events";
			    pageName = "events";
			    break;
			default:
				customFieldName = topicCustomField;
				routeName = "topics";
				break;
		}

		NavigationItem navItem = getChannel(channelId).getChildren(navType.toString()).get(itemId);
		if (navItem == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested "+ navType.toString().toLowerCase() +" was not found.");
		}

		String customFieldValue = navItem.getCustomFieldValue();
			
		// fetch by itemId plus channel id
		BrightcoveResponse<BrightcoveVideo> videoCollection = null;
		
		if(navType == NavigationTypeEnum.Event)
			videoCollection = retrieveEventVideos(channelId, sort);
		else
			videoCollection = fetchVideosByChannel(customFieldName, customFieldValue, channelId, sort, page, pageSize, newsOnly);
		
		//Set the list of channels
		Map<String, Channel> channels = navRepository.getChannels();
		
		//Set the current channel
		Channel currentChannel = channels.get(channelId);
		
		//Set the paging
		Pagination pagination = new Pagination(page + 1, (int)Math.ceil((double) videoCollection.getTotalCount()/pageSize), null);

		//add controller objects to view model
		model.addAttribute("totalCount", videoCollection.getTotalCount() );
		model.addAttribute("itemId", itemId );
		model.addAttribute("currentPage", page );
		model.addAttribute("currentSort", sort.getSortProperty().toString() );
		model.addAttribute("routeName", routeName );
		model.addAttribute("searchResult", searchResult(request, videoCollection));
		model.addAttribute("channels", channels );
		model.addAttribute(ModelConstants.CURRENT_CHANNEL, currentChannel );
		model.addAttribute("navMap", currentChannel.getChildren());
		model.addAttribute("currentNavItem", navItem);
		model.addAttribute("metaDescription", navItem.getName());
        model.addAttribute("videoStartIndex", (page * pageSize) + 1);
        model.addAttribute("videoEndIndex", Math.min((page + 1) * pageSize, videoCollection.getTotalCount()));
        
        model.addAttribute("googleAcct", currentChannel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", currentChannel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", currentChannel.getConfig("twitterWidgetID") );

		model.addAttribute("pagination", pagination );
		
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

		model.addAttribute("pageName", pageName);

		// Return the name of the view to be rendered
		return "subchannel/index";
	}
	
	private BrightcoveResponse<BrightcoveVideo> retrieveEventVideos(String channelCFValue, Sort sort) 
	{
		//Initialize the api(s)
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();

		searchFields.add(new CustomField(eventCustomField, "true", BrightcoveLogic.ALL));
		searchFields.add(new CustomField(channelCustomField, channelCFValue, BrightcoveLogic.ALL));
		
		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(0, pageSize, sort, searchFields));
	}
	
}
