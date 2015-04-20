package com.brightcove.consulting.alj.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.UserManager;
import com.brightcove.consulting.alj.services.gigya.Accounts;
import com.brightcove.consulting.alj.services.gigya.Accounts.GetAccountInfo;
import com.brightcove.consulting.alj.services.gigya.Accounts.Logout;
import com.brightcove.consulting.alj.services.gigya.Accounts.SetAccountInfo;
import com.brightcove.consulting.alj.services.gigya.JSObject;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.SigUtils;

@Component("userManager")
public class GigyaUserManager implements UserManager {

	private static Logger logger = LoggerFactory.getLogger(GigyaUserManager.class);

	@Autowired
	private Accounts gigyaAccountsApi;

	@Autowired
	private String gigyaSecret;

//  errorCode:403005
//  errorMessage:Unauthorized user
//  data:{"statusCode":403,"errorMessage":"Unauthorized user",
//        "errorDetails":"Unknown user",
//  		"statusReason":"Forbidden","errorCode":403005,"callId":"d92af472213e4f11a311e9f537523346"}
  
	@Autowired
	private CacheManager cacheManager;

	@Override
	public User get(String uid) throws ServiceException {
	    User user = getUserFromCache(uid);

	    if (user == null) {
    		GetAccountInfo getAccountInfoReq = gigyaAccountsApi.GetAccountInfo();
    		getAccountInfoReq.setUid(uid);
    		GSResponse response = getAccountInfoReq.submit();
    		GSObject data = response.getData();
    
    		try {
    			JSObject jsObject = new JSObject(data);
    			user = jsObject.toObject(User.class);
    		} catch (Exception e) {
    			logger.error("Exception occurred while attempting to convert account info json to user object.", e);
    			throw new RuntimeException(e);
    		}
    		cacheManager.getCache("users").put(user.getUID().hashCode(), user);
	    }

	    User clone = user.clone();
	    return clone;
	}

	/**
	 * For now this just updates the user custom data.
	 *
	 * TODO: handle more than just the custom data. 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	@CacheEvict(value="users", key="#user.getUID().hashCode()")
	public void save(User user) throws ServiceException {
		SetAccountInfo setAccountInfoReq = gigyaAccountsApi.SetAccountInfo();
		setAccountInfoReq.setUid(user.getUID());
		// TODO: we're getting extra data in user accounts for some reason, doing
		// this to make sure we just have the vod data.  Eventually this should 
		// be removed or replaced with logic to validate the schema data
		Map safeData = new HashMap();
		safeData.put("vod", user.vod());
		setAccountInfoReq.setData(safeData);
		GSResponse response = setAccountInfoReq.submit();
	}

	public void logout(String uid) throws ServiceException
	{
		Logout logout = gigyaAccountsApi.Logout();
		logout.setUid(uid);
		logout.submit();
	}

	/**
	 * Gigya specific implementation that will validate a user signature.  This
	 * assumes that the given data is the json obtained after a successful user
	 * login and will contain values for the keys UID, signatureTimestamp, and
	 * UIDSignature.
	 * 
	 * This should be called before assuming the current user that is attempting
	 * to establish a session is valid. If not, a user may obtain another a
	 * session for another user by trying different UIDs.
	 * 
	 * @see com.brightcove.consulting.alj.services.UserManager#validateSignature(java.util.Map)
	 * @param Gigya login response data.
	 *
	 * @return <code>true</code> if the user signature is valid.
	 */
	@Override

	public boolean validateSignature(String uid, String uidSignature, String timestampSignature) {
		if (uid == null || uid.trim().length() < 1) {
			return false;
		}

		try {
			return SigUtils.validateUserSignature(uid, timestampSignature, gigyaSecret, uidSignature);
		} catch (Exception e) {
			logger.error("Exception occurred while attempting to validate signature for " + uid + ": " + e.getMessage());
			return false;
		}
	}

	private User getUserFromCache(String uid) {

        Cache cache = cacheManager.getCache("users");
        ValueWrapper valueWrapper = cache.get(uid.hashCode());
        if (valueWrapper != null) {
            User user = (User) valueWrapper.get();
            return user;
        }
        return null;
	}
}
