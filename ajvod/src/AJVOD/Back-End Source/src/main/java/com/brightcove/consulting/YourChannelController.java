package com.brightcove.consulting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.models.Playlist;
import com.brightcove.consulting.alj.models.PlaylistWrapper;
import com.brightcove.consulting.alj.models.Query;
import com.brightcove.consulting.alj.models.Subscription;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.models.Video;
import com.brightcove.consulting.alj.services.PlaylistManager;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcoveVideosRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

@Controller
public class YourChannelController extends YBTVBaseController implements ApplicationContextAware {

	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private int maxPlaylistVideos;
	
    private static final Logger logger = LoggerFactory.getLogger(YourChannelController.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/your-channel", method = RequestMethod.GET)
    public String show(Locale locale, Model model, HttpServletRequest request, HttpServletResponse response) throws ServiceException, IOException {
        
        User user = getUser(request);

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        
        //retrieve videos for unfinished videos
        List<BrightcoveVideoWrapper> unfinishedVideos = null;

        // populate the Subscription objects, including related videos
        if (user != null) {

        	List<Map> userPositions = user.videoPositions();
        	
        	BrightcoveResponse<BrightcoveVideo> unfinishedVideoResponse = retrieveUnfinishedVideos(userPositions);
        	if (unfinishedVideoResponse != null) {
        		unfinishedVideos = BrightcoveVideoWrapper.convert(unfinishedVideoResponse.getBrightcoveItemCollection());
        	}
        	
            Map[] subscriptionData = user.subscriptions();

            if (subscriptionData != null && subscriptionData.length > 0) {
                for (Map data : subscriptionData) {
                    Subscription subscription = null;
                    try {
                        subscription = new Subscription(data);
                    } catch (JSONException e) {
                        logger.error("Unable to parse subscription for user "
                                + user.getUID() + " subscription: " + data, e);
                        continue;
                    } catch (RuntimeException e) {
                        logger.error("Exception occurred while creating Subscription object: " + e.getMessage(), e);
                        continue;
                    }

                    BrightcoveResponse<BrightcoveVideo> videos = getVideos(subscription);
                    subscription.setVideos(BrightcoveVideoWrapper.convert(videos.getBrightcoveItemCollection()));
                    subscriptions.add(subscription);
                }
            }
                  	
        	List<Playlist> userPlaylists = playlistManager.getPlaylists(user.getUID());
        	List<PlaylistWrapper> playlistCollection = new ArrayList<PlaylistWrapper>();	
        	
        	for (Playlist playlist : userPlaylists) {

        		BrightcoveResponse<BrightcoveVideo> playlistVideoResponse = retrievePlaylistVideos(playlist.getVideos());
        		PlaylistWrapper pCollection = new PlaylistWrapper();
        		pCollection.setPlaylist(playlist);

        		if (playlistVideoResponse != null) {
        			pCollection.setVideos(BrightcoveVideoWrapper.convert(playlistVideoResponse.getBrightcoveItemCollection()));
    			}

        		
        		playlistCollection.add(pCollection);
        	}
        	
        	model.addAttribute("userPlaylists", playlistCollection);
            
            model.addAttribute("userName", user.getProfile().get("firstName"));
        }
        else{
        	response.sendRedirect("/");
        }

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("unfinishedVideos", unfinishedVideos);
        
        // common attributes
        // TODO: move this to a more centralized location
        model.addAttribute("page", "yourchannel");
        model.addAttribute("pageName", "yourchannel");
        model.addAttribute("channels", navRepository.getChannels() );
//?        model.addAttribute("channelName", "Al Jazeera Video On Demand" );
        model.addAttribute("webResourceRoot", webResourceRoot );
        model.addAttribute("webRoot", webRoot );
        model.addAttribute("playerId", playerId );
        model.addAttribute("publisherId", publisherId );

        return "yourchannel/index";
    }

    private BrightcoveResponse<BrightcoveVideo> retrievePlaylistVideos(List<Video> videos) {
    	if(videos == null || videos.size() < 1)
    		return null;
    	
    	Set<String> videoIds = new HashSet<String>();
    	int videoCount = 1;
    	for(Video video : videos)
    	{
    		String videoId = (String) video.getId();
    		if(StringUtils.isEmpty(videoId))
    			continue;
    		
    		videoIds.add(videoId);
    		
    		if(++videoCount > maxPlaylistVideos)
    			break;
    	}
    	
    	BrightcoveVideosRequest bcRequest = new BrightcoveVideosRequest(0, 10, new Sort(Direction.DESC, SortProperty.NATURAL), videoIds);
    	BrightcoveResponse<BrightcoveVideo> videoList = brightcoveReadApiService.findVideosByIds(bcRequest);
    	
		return videoList;
	}

    @SuppressWarnings("rawtypes")
	private BrightcoveResponse<BrightcoveVideo> retrieveUnfinishedVideos(List<Map> userPositions) {
    	if(userPositions == null || userPositions.isEmpty())
    	    return null;
    	
    	Set<String> videoIds = new HashSet<String>();
    	int videoCount = 1;
    	for(Map position : userPositions)
    	{
    		String videoId = (String) position.get("id");
    		if(StringUtils.isEmpty(videoId))
    			continue;
    		
    		videoIds.add(videoId);
    		
    		if(++videoCount > maxPlaylistVideos)
    			break;
    	}
    	
    	BrightcoveVideosRequest bcRequest = new BrightcoveVideosRequest(0, 10, new Sort(Direction.DESC, SortProperty.NATURAL), videoIds);
    	BrightcoveResponse<BrightcoveVideo> videos = brightcoveReadApiService.findVideosByIds(bcRequest);
    	
		return videos;
	}

	private BrightcoveResponse<BrightcoveVideo> getVideos(Subscription subscription) {
        Query query = subscription.getQuery();
        SearchApiPageRequest pageRequest = query.toPageRequest(0, pageSize);
        return brightcoveReadApiService.findVideosBySearchFields(pageRequest);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
    }
    
}
