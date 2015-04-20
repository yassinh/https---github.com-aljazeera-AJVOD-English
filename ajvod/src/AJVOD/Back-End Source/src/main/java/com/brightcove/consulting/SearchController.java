package com.brightcove.consulting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.json.RelatedNavJson;
import com.brightcove.consulting.alj.json.VideoJson;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.Pagination;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.alj.utils.RelatedNavSearchPredicate;
import com.brightcove.consulting.alj.utils.SearchFieldsBuilder;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

/**
 * Handles requests for the application Search page.
 * 
 * @author jmyatt
 * 
 */
@Controller
public class SearchController extends YBTVBaseController {

    private static final Logger logger = LoggerFactory
            .getLogger(SearchController.class);

    /**
     * Performs a search query to power the search results view.
     * 
     * @param locale
     * @param model
     * @param pRequest
     * @return
     */
    @RequestMapping(value = "/searchresults", method = RequestMethod.GET)
    public String search(Locale locale, Model model, HttpServletRequest pRequest, HttpServletResponse pResponse
    		, @RequestParam(value = "keyword", required = true) final String searchTerm
    		, @RequestParam(value = "cid", required = false) final String channelId
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey) {
    	

    	pResponse.setCharacterEncoding("UTF-8");

        // prepare the paging and sorting values
        page--;
        SortProperty sortProperty = getSortProperty(sortKey);
        Sort sort = new Sort(Direction.DESC, sortProperty);

        //Set the channel
        String placeholderChannelId = channelId != null && channelId.length() > 0 ? channelId : DEFAULT_CHANNEL_KEY; 
        Channel viewChannel = getChannelForLocale(locale, placeholderChannelId);
      	Channel channel = getChannel(placeholderChannelId);
      	Channel[] relatedChannels = getRelatedChannels(channel);
      	Channel firstRelatedChannel = relatedChannels.length > 0 ? relatedChannels[0] : channel;
      	Channel secondRelatedChannel = relatedChannels.length > 1 ? relatedChannels[1] : channel;
      	
      	String videoType = "all";

        // Execute the query requested query
        BrightcoveResponse<BrightcoveVideo> videoCollection = retrieveSearchedVideos(
                searchTerm, channelId, page, pageSize, sort, videoType);

        // Retrieve the search results for the 1st extra channel
        // TODO: Set related channel ids
        BrightcoveResponse<BrightcoveVideo> firstRelatedCollection = retrieveSearchedVideos(
                searchTerm, firstRelatedChannel.getCustomFieldValue(), page, 3, sort, null);

        // Retrieve the search results for the 2nd extra channel
        BrightcoveResponse<BrightcoveVideo> secondRelatedCollection = retrieveSearchedVideos(
                searchTerm, secondRelatedChannel.getCustomFieldValue(), page, 3, sort, null);
        
        //Set the paging
  		Pagination pagination = new Pagination(page + 1, (int)Math.ceil((double) videoCollection.getTotalCount()/pageSize), null);

        // Pass the returned items to the view
  		SearchResult sr = searchResult(pRequest, videoCollection);
  		model.addAttribute("searchResult", sr);
  		
  		model.addAttribute("firstRelatedVideos", wrap(firstRelatedCollection.getBrightcoveItemCollection()) );
  		model.addAttribute("secondRelatedVideos", wrap(secondRelatedCollection.getBrightcoveItemCollection()) );

        model.addAttribute("currentChannel", getChannel(placeholderChannelId));  
        model.addAttribute("channelId", channelId);
        model.addAttribute("channels", navRepository.getChannels());
        model.addAttribute("currentChannel", viewChannel );
        model.addAttribute("navMap", channel.getChildren());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("metaDescription", searchTerm );
        model.addAttribute("pagination", pagination);
        model.addAttribute("firstRelatedChannel", firstRelatedChannel);
        model.addAttribute("secondRelatedChannel", secondRelatedChannel);
        model.addAttribute("webResourceRoot", webResourceRoot);
        model.addAttribute("webRoot", webRoot );
        
        model.addAttribute("playerId", channel.getConfig("playerId") );
        model.addAttribute("playerKey", channel.getConfig("playerKey") );
        model.addAttribute("customPlayer", channel.getConfig("customPlayer") );
        model.addAttribute("publisherId", publisherId );

	model.addAttribute("hideEvents", channel.getConfig("hideEvents") );
        model.addAttribute("hideLive", channel.getConfig("hideLive") );
		
        model.addAttribute("pageName", "search");
        model.addAttribute("totalCount", videoCollection.getTotalCount());
        model.addAttribute("videoStartIndex", (page * pageSize) + 1);
        model.addAttribute("videoEndIndex", Math.min((page + 1) * pageSize, videoCollection.getTotalCount()));
        
        model.addAttribute("googleAcct", channel.getConfig("googleAccount") );
		model.addAttribute("twitterAcct", channel.getConfig("twitterAccountName") );
		model.addAttribute("twitterID", channel.getConfig("twitterWidgetID") );


        // Return the name of the view to be rendered
        return "search/index";
    }

    /**
     * Returns search results for the search overlay, currently used for
     * search-ahead results as the user is typing.
     * 
     * @param locale
     * @param model
     * @param searchTerm
     * @param channelId
     * @return
     * @throws UnsupportedEncodingException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/searchresults-ajax", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    Map searchAhead(
            HttpServletResponse response,
            Locale locale,
            Model model,
            @RequestParam(value = "keyword", required = true) final String searchTerm,
            @RequestParam(value = "cid", required = false) String channelId) throws UnsupportedEncodingException {

    	response.setCharacterEncoding("UTF-8");

        Map returnVal = new HashMap();
        returnVal.put("resultsUrl", null);
        returnVal.put("videos", new Object[] {});
        returnVal.put("categories", new Object[] {});

        Channel channel = null;
        if (!StringUtils.isEmpty(channelId)) {
            channel = navRepository.getChannel(channelId);
            /*if (channel == null) {
                return returnVal;
            }*/
        }

        //String channelCFValue = channelId == null ? null : navRepository.getChannel(channelId).getCustomFieldValue();
        
        BrightcoveResponse<BrightcoveVideo> videos = retrieveSearchedVideos(searchTerm, channelId, 0, 2, new Sort(Direction.DESC, SortProperty.CREATION_DATE), null);

        Collection<VideoJson> videosJson = VideoJson.convert(
                videos.getBrightcoveItemCollection(), channel);

        returnVal.put("videos", videosJson);

        // the assumption here is that getTotalCount() always returns at least 1
        // even if there are no results, check that first before using totalCount
        List<BrightcoveVideo> videoCollection = videos.getBrightcoveItemCollection();
        int totalCount = (int) videos.getTotalCount();
        if (totalCount == 1) {
            if (videoCollection.isEmpty()) {
                totalCount = 0;
            }
        }
        returnVal.put("totalVideos", totalCount);

        // we're either going to search content for one channel or everything
        Collection<Channel> channels = null;
        if (channel != null) {
            channels = new ArrayList<Channel>();
            channels.add(channel);
        } else {
            channels = navRepository.getChannels().values();
        }

        Predicate relatedSearch = new RelatedNavSearchPredicate(searchTerm);
        // TODO: this should at least use some kind of weighted searching so
        // more
        // relevant results are returned instead of just the first 5 we find.
        // TODO: cache this
        Set<NavigationItem> results = (Set<NavigationItem>) NavUtils
                .breadthSearch(channels, relatedSearch, 5);
        returnVal.put("categories", RelatedNavJson.convert(results));
        returnVal.put("resultsUrl", webRoot + "/searchresults?keyword=" + URLEncoder.encode(searchTerm, "UTF-8"));

        return returnVal;
    }

    /**
     * Performs an ajax search query to power the search results view.
     * 
     * @param locale
     * @param modelc
     * @param pRequest
     * @return
     */
    @RequestMapping(value = "/searchresults-ajax", method = RequestMethod.GET)
    public String ajax(Locale locale, Model model, HttpServletRequest pRequest, HttpServletResponse pResponse
    		, @RequestParam(value = "st", required = true) final String searchTerm
    		, @RequestParam(value = "cid", required = false) final String channelId
    		, @RequestParam(value = "vidtype", required = false) final String videoType
    		, @RequestParam(value = "page", defaultValue = "1") int page
    		, @RequestParam(value = "sort", defaultValue = "creation_date") String sortKey) {

    	pResponse.setCharacterEncoding("UTF-8");

        // prepare the paging and sorting values
        page--;
        SortProperty sortProperty = getSortProperty(sortKey);
        Sort sort = new Sort(Direction.DESC, sortProperty);

        // requested channel
        Channel channel = StringUtils.isEmpty(channelId) ? null : getChannel(channelId);

        // required channel for the view
        String placeholderChannelId = channelId != null && channelId.length() > 0 ? channelId : DEFAULT_CHANNEL_KEY; 
        Channel viewChannel = channel == null ? getChannelForLocale(locale, placeholderChannelId) : channel;

        // Execute the search query 
        BrightcoveResponse<BrightcoveVideo> videoCollection = 
        		retrieveSearchedVideos(searchTerm, channelId, page, pageSize, sort, videoType);
        
        SearchResult searchResult = searchResult(pRequest, videoCollection);

        //Set the paging
  		Pagination pagination = new Pagination(page + 1, (int)Math.ceil((double) videoCollection.getTotalCount()/pageSize), null);

        // Pass the returned items to the view

        model.addAttribute("channelId", channelId);
        model.addAttribute(ModelConstants.CURRENT_CHANNEL, viewChannel);
        model.addAttribute("pagination", pagination);
        model.addAttribute("totalPages", pagination.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("searchResult", searchResult);
        model.addAttribute("totalCount", videoCollection.getTotalCount());
        model.addAttribute("videoStartIndex", Math.min((page * pageSize) + 1, videoCollection.getTotalCount()));
        model.addAttribute("videoEndIndex", Math.min((page + 1) * pageSize, videoCollection.getTotalCount()));
        model.addAttribute("webResourceRoot", webResourceRoot);

        // Return the name of the view to be rendered
        return "search/searchGrid";
    }

    private BrightcoveResponse<BrightcoveVideo> retrieveSearchedVideos(
            String searchTerm, String channelId, int page, int pageSize,
            Sort sort, String videoType) {
    	
        Channel channel = navRepository.getChannel(channelId);

        SearchFieldsBuilder fieldsBuilder = new SearchFieldsBuilder();
        fieldsBuilder.add(videoType, BrightcoveLogic.ANY).add(channel, BrightcoveLogic.ALL);

        List<BrightcovePair> searchFields = fieldsBuilder.getFields();

        SearchTerm term = new SearchTerm(searchTerm, BrightcoveLogic.ALL);

        if (searchFields.isEmpty()) {
            return readApiService().findVideosBySearchFields(
                    new SearchApiPageRequest(page, pageSize, sort, term));
        }

        return readApiService().findVideosBySearchFields(
                new SearchApiPageRequest(
                        page, pageSize, sort, term, searchFields));

    }

    private Channel[] getRelatedChannels(Channel channel) {
        if (channel == null) {
            return new Channel[0];
        }
        Set<Channel> relatedChannels = channel.getRelatedChannels();

        // the number of channels we're looking for
        int numChannels = 2;

        // if it's less than what we need, then find some more
        if (relatedChannels.size() < numChannels) {
            // the set may be unmodifiable
            relatedChannels = new HashSet<Channel>(relatedChannels);

            // loop over all channels until we find what we need
            Collection<Channel> channels = navRepository.getChannels().values();
            for (Channel _channel : channels) {
                if (_channel != channel && !relatedChannels.contains(_channel)) {
                    relatedChannels.add(_channel);
                }
                if (relatedChannels.size() == numChannels) {
                    // we found the amount we need, now stop
                    break;
                }
            }
        }
        return relatedChannels.toArray(new Channel[]{});
    }

    /**
     * 
     * @param locale
     * @param pRequest
     * @param channelId
     * @return
     */
    private Channel getChannelForLocale(Locale locale, String channelId) {
        Channel channel = null;
        if (!StringUtils.isEmpty(channelId)) {
            channel = getChannel(channelId);
        }

        if (channel == null) {
            String language = locale.getLanguage();
            // find the first channel with language of the current locale
            Collection<Channel> channels = navRepository.getChannels().values();
            for (Channel _channel : channels) {
				if (!StringUtils.isEmpty(channel.getLanguage()) && StringUtils.equalsIgnoreCase(language, channel.getLanguage())) {
					return channel;
				}
			}
            
            channel = navRepository.getChannel(DEFAULT_CHANNEL_KEY);
        }

        return channel;
    }
}
