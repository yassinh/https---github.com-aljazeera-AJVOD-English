package com.brightcove.consulting.alj.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.models.BrightcoveVideoWrapper;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.utils.SearchFieldsBuilder;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

@Controller
@RequestMapping("/api/events")
public class EventsController extends BaseController {

	@NoAuthorization
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value="/timeline")
    public @ResponseBody Map<String,Object> related(HttpServletRequest request,
            @RequestParam(value="videoId", required=true) String videoId) throws ServiceException {

        Map<String, Object> json = json(HttpStatus.OK);

        BrightcoveResponse<BrightcoveVideo> videoResponse = 
                readApiService().findVideoById(
                        new BrightcoveVideoRequest(0, 1, new Sort(Direction.DESC, SortProperty.CREATION_DATE), videoId));

        List<BrightcoveVideo> videos = videoResponse.getBrightcoveItemCollection();
        json.put("videos", new ArrayList());
        if (videos.isEmpty()) {
            return json;
        }


        BrightcoveVideo video = videos.get(0);
        String eventName = video.getCustomField("eventname");
        // if not eventName then nothing to get 
        if (StringUtils.isEmpty(eventName)) {
            return json;
        }


        // find all vidoes that are not the hero with the same event name
        SearchFieldsBuilder builder = new SearchFieldsBuilder();
        builder.add("eventname", eventName, BrightcoveLogic.ALL);
        if (!StringUtils.isEmpty(video.getCustomField("channel"))) {
            builder.add("channel", video.getCustomField("channel"), BrightcoveLogic.ALL);
        }
        SearchApiPageRequest searchReq = new SearchApiPageRequest(0, 100, builder.getFields());
        BrightcoveResponse<BrightcoveVideo> response = readApiService().findVideosBySearchFields(searchReq);

        // transform into json sorted by publishDate for the response
        List<BrightcoveVideo> timelineVideos = response.getBrightcoveItemCollection();
        Collection videosJson = CollectionUtils.collect(timelineVideos, new VideoTransformer());

        TreeBag sortedVideos = new TreeBag(new TimelineComparator());
        sortedVideos.addAll(videosJson);
        
        if(timelineVideos != null && timelineVideos.size() > 0){
        	Object[] videoObjects = sortedVideos.toArray();
        	HashMap startVideo = (HashMap)videoObjects[0];
        	HashMap endVideo = (HashMap)videoObjects[videoObjects.length - 1];

	        json.put("startDate", startVideo.get("displayDate"));
	        json.put("endDate", endVideo.get("displayDate"));
        }
        json.put("eventName", eventName);
        json.put("videos", sortedVideos.toArray());

        return json;
    }

    private static class VideoTransformer implements Transformer {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Object transform(Object input) {
            BrightcoveVideoWrapper video = new BrightcoveVideoWrapper((BrightcoveVideo)input);
            Map json = new HashMap();
            json.put("id", video.getId());
            json.put("name", video.getName());
            json.put("thumbnailUrl", video.getThumbnailUrl());
            json.put("showText", video.getShowText());
            json.put("description", video.getShortDescription());
            json.put("duration", video.getVideoLength());
            json.put("publishDate", video.getDate());
            json.put("displayDate", video.getDisplayDate());
            json.put("showUrl", video.getShowLink());
            return json;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class TimelineComparator implements Comparator<Map> {
        @Override
        public int compare(Map o1, Map o2) {
            Date o1Date = (Date) o1.get("publishDate");
            Date o2Date = (Date) o2.get("publishDate");
            if (o1Date == null) {
                if (o2Date == null) {
                    return 0;
                }
                // other date is not null, assume we come before
                return -1;
            }

            return o1Date.compareTo(o2Date);
        }
        
    }

    public static void main(String[] args) {
        Date date = new Date();
        Date date2 = DateUtils.addYears(date, 1);
        Date date3 = DateUtils.addYears(date2, 1);
        Set<Date> dates = new TreeSet<Date>();
        dates.add(date);
        dates.add(date2);
        dates.add(date3);
        System.out.println(dates);
    }
}
