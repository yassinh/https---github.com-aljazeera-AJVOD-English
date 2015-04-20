package com.brightcove.consulting.alj.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;


@Controller
@RequestMapping("/api/positions")
public class VideoPositionController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(VideoPositionController.class);

	@Autowired
	UserManager userManager;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value="/get", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String,Object> get(HttpServletRequest request
			, @RequestParam(value="id",required=false) String videoId) throws ServiceException {


		User user = getUser(request);
		Map<String,Object> jsonResponse = json(HttpStatus.OK);
		
		// return all position data if no videoId was given
		if (StringUtils.isEmpty(videoId)) {
			List<Map> userPositions = user.videoPositions();
			jsonResponse.put("positions", userPositions);

			return jsonResponse;
		}
			
		String videoPosition = user.getVideoPosition(videoId);

		jsonResponse.put("id", videoId);
		jsonResponse.put("pos", videoPosition);

		return jsonResponse;
	}

	@SuppressWarnings("rawtypes")
    @RequestMapping(value="/set", method = RequestMethod.POST)
	public @ResponseBody Map add(HttpServletRequest request, 
	        @RequestParam(required=true) String id,
	        @RequestParam(required=true) String pos) throws ServiceException {
	    User user = getUser(request);
	    
	    if(user.setVideoPosition(id, pos)){
	    	userManager.save(user);
	    }

		return json(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/remove", method = RequestMethod.POST)
	public @ResponseBody Map remove(HttpServletRequest request, 
			@RequestParam(required=true) String id) throws ServiceException {
		User user = getUser(request);
		boolean removed = false;

		if (removed = user.removeVideoPosition(id)) {
			userManager.save(user);
		}
		
		Map<String, Object> json = json(HttpStatus.OK);
		json.put("removed", removed);

		return json;
	}
	
	
}
