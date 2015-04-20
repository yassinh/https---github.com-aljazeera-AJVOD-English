package com.brightcove.consulting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.NavigationTypeEnum;
import com.brightcove.consulting.alj.models.SearchResult;
import com.brightcove.consulting.alj.models.Topic;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.response.BrightcoveResponse;

@Controller
public class TopicController extends YBTVBaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(TopicController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/channels/{cid}/topics", method = RequestMethod.GET)
    public String topics(final Locale locale, Model model, 
    		HttpServletRequest request, HttpServletResponse response,
    		@PathVariable("cid") String channelId) {
    	
		response.setCharacterEncoding("UTF-8");

        Channel channel = getChannel(channelId);
        int carouselCount = 1;

        Collection<Topic> topics = channel.getTopics();
        // sort topics by their name
        topics = (Collection<Topic>) NavUtils.sortByName(topics);
        
        //TODO: Topic object may need query for jsp
        
        NavigationItem navItem = channel.getChildren(NavigationTypeEnum.AllPages.toString()).get("topics");
		if (navItem == null) {
            // TODO-I18n
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "The requested "+ NavigationTypeEnum.AllPages.toString().toLowerCase() +" was not found.");
		}
        
        //Add videos to each topic for display
        Map<Topic, SearchResult> topicVideoCollection = new HashMap<Topic, SearchResult>();
        for (Topic topic : topics) {
        	String topicCFvalue = topic.getCustomFieldValue();
        	
        	BrightcoveResponse<BrightcoveVideo> topicVideos = fetchVideosByChannel(topicCustomField, topicCFvalue, channelId, 
        			new Sort(Direction.DESC, SortProperty.CREATION_DATE), 0, 4, false);
        	topicVideoCollection.put(topic, searchResult(request, topicVideos, carouselCount++));
        }
        
        model.addAttribute("currentChannel", channel);
        model.addAttribute("topicResults", topicVideoCollection);
        
		model.addAttribute("channels", navRepository.getChannels() );
		model.addAttribute("navMap", channel.getChildren());
		model.addAttribute("googleAcct", channel.getConfig("googleAccount") );
		model.addAttribute("currentNavItem", navItem);
		model.addAttribute("metaDescription", navItem.getName());

		model.addAttribute("webResourceRoot", webResourceRoot );
		model.addAttribute("webRoot", webRoot );
		
		model.addAttribute("publisherId", publisherId );
                model.addAttribute("playerId", channel.getConfig("playerId") );
                model.addAttribute("playerKey", channel.getConfig("playerKey") );	
                model.addAttribute("customPlayer", channel.getConfig("customPlayer") );
		model.addAttribute("hideEvents", channel.getConfig("hideEvents") );
                model.addAttribute("hideLive", channel.getConfig("hideLive") );
	
		model.addAttribute("pageName", "topics" );

		// used to construct subscription title
		model.addAttribute("titlePrefixTerm", "topics");

        return "topics/index";
    }

}
