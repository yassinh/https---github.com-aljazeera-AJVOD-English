package com.brightcove.consulting.alj.services;

import com.brightcove.consulting.alj.models.User;

public interface UserManager {

	User get(String uid) throws ServiceException;

	void save(User user) throws ServiceException;
	
	void logout(String uid) throws ServiceException;

	boolean validateSignature(String uid, String uidSignature, String timestampSignature);
}
