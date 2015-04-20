package com.brightcove.consulting.alj.api;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.EPG;
import com.brightcove.consulting.alj.models.EPG.Schedule;
import com.brightcove.consulting.alj.models.EPGFeed;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.NavRepository;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.EpgFeedManager;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.config.parser.GetChildrenRule;
import com.google.gson.Gson;

/**
 * Handles requests for the application Channel page.
 */
@Controller
public class EpgController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(EpgController.class);

	@Autowired
	private NavRepository navRepository;

	@Autowired
	private UserManager userManager;

	@Autowired
	private EpgFeedManager epgFeedManager;

	@NoAuthorization
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/api/epg", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map epg(HttpServletRequest request,
		   @RequestParam(value="cid", required=true) String channelId,
		   @RequestParam(value="offset", required=false, defaultValue="0") Integer offset) throws JsonParseException, JsonMappingException, IOException, ServiceException {

		EPGFeed feed = epgFeedManager.getFeed(channelId);
		Channel channel = navRepository.getChannel(channelId);

		DateTimeZone timeZone = getTimeZone(request);

		Schedule schedule = null;
		Locale locale = new Locale(channel.getLanguage());
		
		if (feed == null) {
			schedule = new EPG(null).setLocale(locale).getSchedule();
		} else {
			EPG epg = new EPG(feed);
	
			DateTime startTime = DateTime.now().withZone(timeZone).toLocalDate().toDateTimeAtStartOfDay(timeZone);
			epg.setNumberOfDays(7);
			epg.setNumberOfHours(24);
			epg.setStartTime(startTime);
			epg.setLocale(locale);
			epg.load();
	
			schedule = epg.getSchedule();
			// for now, the front end doesn't want the actual start time.
			List<Map<String, String>> times = schedule.getTimes();
			times.remove(0);
		}

		JSONObject json = new JSONObject(schedule);
		Gson gson = new Gson();
		Map jsonData = gson.fromJson(json.toString(), Map.class);
		jsonData.put("status", 200);
		return jsonData;
	}

	public static void main(String[] args) throws JSONException {
		EPG epg = new EPG(null);
		Schedule schedule = epg.getSchedule();
		System.out.println(new JSONObject(schedule).toString(4));
	}
}