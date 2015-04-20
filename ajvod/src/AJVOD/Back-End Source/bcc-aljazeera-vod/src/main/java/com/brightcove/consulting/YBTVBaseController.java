package com.brightcove.consulting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.NavRepository;
import com.brightcove.consulting.alj.models.Query;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.models.Show;
import com.brightcove.consulting.alj.models.Subscription;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.alj.utils.SearchFieldsBuilder;
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
import com.brightcove.consulting.services.BrightcoveReadApiService;
import com.brightcove.consulting.util.ObjectToJson;


/**
 * Abstract Controller for common functionality that all 
 * YBTV Controllers can use.
 */
public abstract class YBTVBaseController {
	
    private static final Logger logger = LoggerFactory.getLogger(YBTVBaseController.class);
    @Autowired
    protected String readToken;	 

    @Autowired
    protected int pageSize;	

    @Autowired
    protected int pageSizeLargeCarousel;	

    @Autowired
    protected int pageSizeSmallCarousel;	

    @Autowired
    protected String webResourceRoot;	

    @Autowired
    protected String webRoot;	
    
    @Autowired
    protected String publisherId;
    
    @Autowired
    protected String playerId;

    @Autowired
    protected String featuredPlaylistId;
    
    @Autowired
    protected String channelCustomField;	 

    @Autowired
    protected String videotypeCustomField;	 

    @Autowired
    protected String showGenreCustomField;	 

    @Autowired
    protected String showCustomField;	 

    @Autowired
    protected String topicCustomField;	 

    @Autowired
    protected String regionCustomField;	 
    
    @Autowired
    protected String eventCustomField;

    @Autowired
    protected BrightcoveReadApiService brightcoveReadApiService;

    protected NavRepository navRepository;	

    @Autowired
    protected UserManager userManager;

    @Autowired
    public void setNavRepository(@Value("#{navRepository}") NavRepository pNavRepository) {
        this.navRepository = pNavRepository;
    }
    
    protected static final String DEFAULT_CHANNEL_KEY = "eng";

    protected BrightcoveReadApiService readApiService() {
        return brightcoveReadApiService;
    }

    
    /**
     * Retrieves a required channel and will throw an http not found exception if
     * it is not found.  Only use this when you need to assume that a given request
     * for a channel must exist.
     * 
     * @param channelId
     * @return
     * @throws HttpClientErrorException
     * 
     * TODO: consider making this redirect instead.
     */
    protected Channel getChannel(String channelId) {
        Channel channel = navRepository.getChannel(channelId);
        if (channel == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested channel was not found.");
        }
        return channel;
    }
    /**
     * Retrieve the requested channel by it's id, or use the default if channelId is empty.
     * 
     * @param channelId The requested channel id.
     * @param defaultId The id of the channel to default to.
     * @return
     * @throws HttpClientErrorException 404 if requested channel is not found.
     */
    protected Channel getChannel(String channelId, String defaultId) {
        if (StringUtils.isEmpty(channelId)) {
            return getChannel(defaultId);
        }
        return getChannel(channelId);
    }

   /**
    * @param channel
    * @param showId
    * @return Show
    * @throws HttpClientErrorException
    */
    protected Show getShow(Channel channel, String showId) {
        Show show = channel.getShow(showId);
        if (show == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested show was not found.");
        }
        return show;
    }
    protected Show getShow(Channel channel, String showId, String defaultId) {
        if (StringUtils.isEmpty(showId)) {
            return getShow(channel, defaultId);
        }
        return getShow(channel, showId);
    }

    protected User getUser(HttpServletRequest request) throws ServiceException {
        User user = (User) request.getAttribute(ModelConstants.USER);
        if (user != null) {
            return user;
        }
        // make one more attempt to get it
        String uid = (String) request.getSession().getAttribute(ModelConstants.SESSION_USER_ID);
        if (uid != null) {
            return userManager.get(uid);
        }
        return null;
    }

    /**
     * Tests if the given {@link WebRequest} is an AJAX request or not.
     *
     * @param request
     * @return true if the request is an AJAX request.
     */
    protected boolean isAjax(WebRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
	
	protected String jsonResponse(Object object, HttpServletResponse response) {
	    response.setContentType("application/json");
	    return new ObjectToJson(object).toJson();   
	}
	protected String jsonResponse(Object object, int depth, HttpServletResponse response) {
	    response.setContentType("application/json");
	    return new ObjectToJson(object).maxDepth(depth).toJson();   
	}

	public static String urlEncode(String value) throws UnsupportedEncodingException {
	    return URLEncoder.encode(value, "UTF-8");
	}

    protected SortProperty getSortProperty(String sortKey) {
        if (sortKey == null) {
            return SortProperty.MODIFIED_DATE;
        }
        try {
            return SortProperty.fromString(sortKey);
        } catch (IllegalArgumentException e) {
            return SortProperty.MODIFIED_DATE;
        }
    }

	/**
	 * Fetch a single video based on its BCOV video id
	 * 
	 * @param videoId
	 * @return
	 */
	protected BrightcoveVideo fetchSingleVideo(String videoId) 
	{
		BrightcoveResponse<BrightcoveVideo> videoCollection = readApiService().findVideoById(new BrightcoveVideoRequest(0, pageSize, new Sort(Direction.NATURAL, SortProperty.MODIFIED_DATE),  videoId));
		if(videoCollection == null || videoCollection.getTotalCount() < 1)
			return null;

		return videoCollection.getBrightcoveItemCollection().get(0);
	}
	
	/**
	 * Fetches a single playlist based on playlist id
	 * 
	 * @param playlistId
	 * @return
	 */
	protected BrightcoveResponse<BrightcoveVideo> fetchSinglePlaylist(String playlistId) 
	{
		BrightcoveVideoRequest bvr2 = new BrightcoveVideoRequest(0, pageSize, new Sort(Direction.NATURAL, SortProperty.NATURAL),  playlistId);

		return readApiService().findVideosByPlaylist(bvr2);
	}
	
	/**
	 * Fetch a set of videos based on a single custom field key/value pair.
	 * 
	 * @param cfName the custom field name
	 * @param cfValue the custom field value
	 * @param sort Sort object for the query
	 * @param pageNum page number for the query
	 * @return
	 */
	protected BrightcoveResponse<BrightcoveVideo> fetchVideosBySingleCustomField(String cfName, String cfValue, Sort sort, int pageNum)
	{	
		List<BrightcovePair> searchFields = new ArrayList<BrightcovePair>();
		searchFields.add(new CustomField(cfName, cfValue, BrightcoveLogic.ALL));
		
		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(pageNum, pageSize, sort, searchFields));
	}
	
	/**
	 * Fetch a set of videos based on a single custom field key/value pair.
	 * 
	 * @param cfName the custom field name
	 * @param cfValue the custom field value
	 * @param sort Sort object for the query
	 * @param pageNum page number for the query
	 * @return
	 */
	protected BrightcoveResponse<BrightcoveVideo> fetchVideosByChannel(String cfName, String cfValue, String channelId, Sort sort, int pageNum, int pageSize, boolean newsOnly)
	{		
	    SearchFieldsBuilder builder = new SearchFieldsBuilder();
	    Channel channel = navRepository.getChannel(channelId);
	    builder.add(channel, BrightcoveLogic.ALL);
	    if (newsOnly) {
	        builder.add("news");
	    }
		List<BrightcovePair> searchFields = builder.getFields();
		
		if(!StringUtils.isEmpty(cfName))
			searchFields.add(new CustomField(cfName, cfValue, BrightcoveLogic.ALL));
		
		return readApiService().findVideosBySearchFields(new SearchApiPageRequest(pageNum, pageSize, sort, searchFields));
	}

	protected SearchResult searchResult(HttpServletRequest request, BrightcoveResponse<BrightcoveVideo> response) {
	    SearchResult searchResult = new SearchResult(response);
	    Query query = searchResult.getQuery();

	    // if there's no query, then there's nothing to test against
	    if (query == null) {
	        return searchResult;
	    }

	    User user;
	    try {
            user = getUser(request);
        } catch (ServiceException e) {
            logger.error("Exception occurred while retrieving user.", e);
            return searchResult;
        }

	    // if there is a user, see if they're already subscribed to this particular search
	    if (user != null) {
	        Subscription subscription = user.getMatchingSubscription(query);
	        searchResult.setSubscription(subscription);
	    }
	    return searchResult;
	}
	
	protected SearchResult searchResult(HttpServletRequest request, BrightcoveResponse<BrightcoveVideo> response, int id) {
	    SearchResult searchResult = new SearchResult(response);
	    Query query = searchResult.getQuery();
	    
	    //add an id for the search result
	    searchResult.setId(id);

	    // if there's no query, then there's nothing to test against
	    if (query == null) {
	        return searchResult;
	    }

	    User user;
	    try {
            user = getUser(request);
        } catch (ServiceException e) {
            logger.error("Exception occurred while retrieving user.", e);
            return searchResult;
        }

	    // if there is a user, see if they're already subscribed to this particular search
	    if (user != null) {
	        Subscription subscription = user.getMatchingSubscription(query);
	        searchResult.setSubscription(subscription);
	    }
	    return searchResult;
	}

	protected List<BrightcoveVideoWrapper> wrap(List<BrightcoveVideo> videos) {
	    return BrightcoveVideoWrapper.convert(videos);
	}
	
	protected DateTimeZone getTimeZone(HttpServletRequest request) {
		return TimeZoneResolver.getTimeZone();
	}
}
