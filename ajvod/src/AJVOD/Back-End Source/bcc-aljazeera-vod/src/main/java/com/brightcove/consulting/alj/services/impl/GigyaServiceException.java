package com.brightcove.consulting.alj.services.impl;

import java.io.InvalidClassException;

import org.json.JSONException;
import org.json.JSONObject;

import com.brightcove.consulting.alj.services.ServiceException;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;


/**
 * A ServiceException that is populated by a gigya response.
 *
 * @author ssayles
 */
public class GigyaServiceException extends ServiceException {


	private GSResponse response;

	public GigyaServiceException(GSResponse response) {
		super(response.getErrorMessage());

		this.response = response;

		errorCode = response.getErrorCode();
    	errorDetails = response.getErrorDetails();
    	errorMessage = response.getErrorMessage();

    //  errorCode:400002
    //  errorMessage:Missing required parameter
    //  data:{"statusCode":400,
//            "errorMessage":"Missing required parameter",
//            "errorDetails":"Missing required parameter: uid",
//            "statusReason":"Bad Request",
//            "errorCode":400002,
//            "callId":"e55e7c2bfa46411eb2f6338d106e2a82"}
    	GSObject data = response.getData();
    	if (data != null) {
    		try {
    			if (data.containsKey("statusCode")) {
    				statusCode = data.getInt("statusCode");
    			}
    			if (data.containsKey("statusReason")) {
    				statusReason = data.getString("statusReason");
    			}
			} catch (InvalidClassException e) {
				e.printStackTrace();
			} catch (GSKeyNotFoundException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
    	}
	}

	public GSResponse getResponse() {
		return response;
	}

	public String toString() {
		JSONObject json = new JSONObject();
		try {
			json.put("statusReason",statusReason);
			json.put("statusCode", statusCode);
			json.put("errorCode", errorCode);
			json.put("errorMessage", errorMessage);
			json.put("errorDetails", errorDetails);
			return json.toString(4);
		} catch (JSONException e) {
			return errorMessage;
		}
	}
}
