package com.brightcove.consulting.alj.services.gigya;

import org.apache.commons.beanutils.ConversionException;

import com.gigya.socialize.GSObject;
import com.google.gson.Gson;

/**
 * Convenience wrapper for GSObject so we can easily convert to a target
 * object type.
 * @author ssayles
 *
 */
public class JSObject {

	private GSObject gsObject;

	public JSObject(GSObject object) {
		gsObject = object;
	}

	public <T extends Object>T toObject(Class<T> type) throws ConversionException {
		Gson gson = new Gson();
		T fromJson = gson.fromJson(gsObject.toJsonString(), type);
		return fromJson;
	}
}
