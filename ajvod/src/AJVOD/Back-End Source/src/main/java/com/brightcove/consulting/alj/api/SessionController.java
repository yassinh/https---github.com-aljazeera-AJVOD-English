package com.brightcove.consulting.alj.api;

import static com.brightcove.consulting.util.LogUtils.debug;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.api.errors.InvalidSignatureException;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;

/**
 * Handles requests for the application Channel page.
 */
@Controller
public class SessionController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

	@Autowired
	private UserManager userManager;

	@NoAuthorization
	@RequestMapping(value = "/api/login", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> login(HttpServletRequest request,
	        @RequestParam(required=true, value="UID") String uid,
	        @RequestParam(required=true, value="UIDSignature") String uidSig,
	        @RequestParam(required=true, value="signatureTimestamp") String sigTs) throws JSONException, InvalidSignatureException, ServiceException  {

		if (userManager.validateSignature(uid, uidSig, sigTs)) {

			try {
				debug(logger, "signature validated, requesting user account data");
				User user = userManager.get(uid);
				HttpSession session = request.getSession();
				request.setAttribute(ModelConstants.USER, user);
				session.setAttribute(ModelConstants.SESSION_USER_ID, uid);
	
				// set any user preferences
				Map prefs = user.preferences();
				if (!StringUtils.isEmpty((String) prefs.get("lang"))) {
					session.setAttribute(ModelConstants.SESSION_PREFERRED_LOCALE, prefs.get("lang"));
				}
				if (!StringUtils.isEmpty((String) prefs.get("timezone"))) {
					session.setAttribute(ModelConstants.SESSION_PREFERRED_TIMEZONE, prefs.get("timezone"));
				}
				if (!StringUtils.isEmpty((String) prefs.get("country"))) {
					session.setAttribute(ModelConstants.SESSION_PREFERRED_COUNTRY, prefs.get("country"));
				}

				return json(HttpStatus.OK);
			} catch (ServiceException e) {
				debug(logger, "Exception while getting user account data: " + e);
				// let the exception resolver handle this
				// TODO: inspect the service exception and do something better
				throw e;
			}
		} else {
			debug(logger, "invalid user signature");
			throw new InvalidSignatureException();
		}
	}

	/**
	 * Log out of gigya, removing all stored session data related to the end user 
	 * @throws ServiceException 
	 */
	@RequestMapping(value = "/api/logout", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> logout(HttpServletRequest request) throws ServiceException {

	    HttpSession session = request.getSession();
	    String uid = (String) session.getAttribute(ModelConstants.SESSION_USER_ID);

		//log out of gigya and remove session object
		if (uid != null) {
		    session.invalidate();
			userManager.logout(uid);
		}

		return json(HttpStatus.OK);
	}
}