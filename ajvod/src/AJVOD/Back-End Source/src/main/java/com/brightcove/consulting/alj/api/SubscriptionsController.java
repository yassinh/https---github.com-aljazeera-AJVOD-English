package com.brightcove.consulting.alj.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.api.errors.ConflictException;
import com.brightcove.consulting.alj.api.errors.MissingJsonPropertyException;
import com.brightcove.consulting.alj.models.Query;
import com.brightcove.consulting.alj.models.Subscription;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.alj.utils.EncryptionHandler;


@Controller
@RequestMapping("/api/subscriptions")
public class SubscriptionsController extends BaseController {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsController.class);

	private static int MAX_SUBSCRIPTIOSN = 10;

	@Autowired
	UserManager userManager;

	@Autowired
	EncryptionHandler encryptionHandler;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/get", method = RequestMethod.GET)
	public @ResponseBody Map<String,Object> get(HttpServletRequest request) throws ServiceException {

		User user = getUser(request);

		Map<String,Object> jsonResponse = json(HttpStatus.OK);

		Map[] subscriptions = user.subscriptions();
		jsonResponse.put("subscriptions", subscriptions);

		return jsonResponse;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value="/add", method = RequestMethod.POST)
	public @ResponseBody Map add(HttpServletRequest request, HttpServletResponse response, @RequestBody Map data) throws ServiceException, ConflictException, JSONException {
	    User user = getUser(request);
	    Map[] subscriptions = user.subscriptions();
	    if (subscriptions.length + 1 > MAX_SUBSCRIPTIOSN) {
	        throw new ConflictException(
	                "Cannot add more than " + MAX_SUBSCRIPTIOSN + " subscriptions.",
	                "Remove a subscription before adding another.");
	        
	    }
	    String query = (String) data.get("query");

	    if (!StringUtils.isEmpty(query)) {
	    	// if the useEncryption setting is true, then assume the query
	    	// we're being given is encrypted
	    	if (encryptionHandler.isUseEncryption()) {
		    	query = encryptionHandler.decrypt(query);
		    	data.put("query", query);
	    	}
	    }

	    // if a matching subscription exists, just return an ok with the id
	    Subscription subscription = user.getMatchingSubscription(new Query(query));
	    if (subscription != null) {
	        String id = subscription.getId();
	        Map<String, Object> json = json(HttpStatus.OK);
	        json.put("id", id);
	        return json;
	    }

	    String id = user.addSubscription(data);
	    if (id == null) {
	        // then it wasn't added because it's a dup
	        // but this shouldn't happen
	        return json(HttpStatus.OK);
	    }

	    // else, we added a new subscription to the user data
	    userManager.save(user);

		Map<String, Object> json = json(HttpStatus.CREATED);
		json.put("id", id);
		return json;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value="/order", method = RequestMethod.POST)
	public @ResponseBody Map order(HttpServletRequest request, @RequestBody Map data)  throws ServiceException, MissingJsonPropertyException  {
		User user = getUser(request);
		Object object = data.get("ids");
		if (object == null || !(object instanceof List)) {
		    throw new MissingJsonPropertyException("ids:[]");
		}
		List list = (List)object;
		String[] ids = (String[]) list.toArray(new String[]{});
		boolean reordered = user.orderSubscriptions(ids);
		if (reordered) {
		    userManager.save(user);
		}
	    
        return json(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/remove", method = RequestMethod.POST)
	public @ResponseBody Map remove(HttpServletRequest request, @RequestParam(required=true) String id) throws ServiceException, MissingJsonPropertyException  {
		User user = getUser(request);

	    boolean removed = user.removeSubscription(id);
	    if (removed) {
	        userManager.save(user);
	    }

	    Map<String, Object> response = json(HttpStatus.OK);
	    response.put("removed", removed);

	    return response;
	}
}
