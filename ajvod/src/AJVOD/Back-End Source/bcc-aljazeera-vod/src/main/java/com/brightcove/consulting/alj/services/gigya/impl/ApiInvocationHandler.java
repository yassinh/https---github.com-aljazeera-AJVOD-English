package com.brightcove.consulting.alj.services.gigya.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.gigya.ApiMethod;
import com.brightcove.consulting.alj.services.gigya.GigyaService;
import com.brightcove.consulting.alj.services.gigya.Required;
import com.brightcove.consulting.alj.services.impl.GigyaServiceException;
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSLogger;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;

public class ApiInvocationHandler implements InvocationHandler {

    private Class<? extends ApiMethod> apiMethod;

    private GSRequest request;

    private Map<String,Object> params;

    private String apiMethodName;

    @Autowired
    protected String gigyaToken;

    @Autowired
    protected String gigyaSecret;


    public ApiInvocationHandler(Class<? extends ApiMethod> apiMethod) {
        this.apiMethod = apiMethod;
    }


//  response examples:
//    errorCode:400002
//    errorMessage:Missing required parameter
//    data:{"statusCode":400,
//          "errorMessage":"Missing required parameter",
//          "errorDetails":"Missing required parameter: uid",
//          "statusReason":"Bad Request",
//          "errorCode":400002,
//          "callId":"e55e7c2bfa46411eb2f6338d106e2a82"}
    
//    errorCode:403005
//    errorMessage:Unauthorized user
//    data:{"statusCode":403,"errorMessage":"Unauthorized user",
//          "errorDetails":"Unknown user",
//    		"statusReason":"Forbidden","errorCode":403005,"callId":"d92af472213e4f11a311e9f537523346"}
    
//	  errorCode:0
//	  errorMessage:null
//	  data:{"isVerified":false,"createdTimestamp":1370318743940,"UIDSignature":"3z81w8iLFifIgZ3qxfjrybYzgHQ=","lastUpdatedTimestamp":1370318743943,"data":{},"lastUpdated":"2013-06-04T04:05:43.943Z","isRegistered":true,"socialProviders":"twitter","callId":"b17f02e033b64de287c89763eef5e8ab","statusCode":200,"isActive":true,"oldestDataUpdatedTimestamp":1370318743894,"lastLogin":"2013-06-04T04:05:44Z","created":"2013-06-04T04:05:43.94Z","lastLoginTimestamp":1370318744000,"UID":"_guid_TXxBdoebAUE5c7GXstNcNw==","statusReason":"OK","signatureTimestamp":"1370318789","errorCode":0,"oldestDataUpdated":"2013-06-04T04:05:43.894Z","loginProvider":"twitter","profile":{"lastName":"Fanatic","photoURL":"http://a0.twimg.com/profile_images/1665102105/images.jpeg","nickname":"_jbfanatic_","profileURL":"http://twitter.com/_jbfanatic_","firstName":"Jonas","thumbnailURL":"http://a0.twimg.com/profile_images/1665102105/images_normal.jpeg"}}

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("submit")) {
            return submit(args);
        } else if (method.getName().startsWith("set")) {
            if (params == null) {
                params = new HashMap<String,Object>();
            }
            params.put(getParamName(method), args[0]);
        }
        return null;
    }

    private GSResponse submit(Object[] args) throws Throwable {
    	initializeRequest();
        validateParams();
        GSLogger logger = new GSLogger();
        if (params != null && !params.isEmpty()) {
            GSObject gsParams = createParams();
            request.setParams(gsParams);
        }
        GSResponse response = (GSResponse) MethodUtils.invokeMethod(request, "send", args);
        if (response.getErrorCode() > 0) {
        	throw new GigyaServiceException(response);
        }

        return response;
    }

    private GSObject createParams() throws Throwable {
        Set<String> keys = params.keySet();
        GSObject gsParams = new GSObject();

        for (String key : keys) {
            Object value = params.get(key);
            if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
                // nothing
                Object[] putArgs = new Object[] {key, value};
                MethodUtils.invokeMethod(gsParams, "put", putArgs);
            } else if (value.getClass().isArray()) {
                if (value instanceof GSArray) {
                    gsParams.put(key, (GSArray)value);
                } else {
                    // TODO: handle array conversion too
                    throw new NotImplementedException("Not handling array parameter conversion at this time.");
                }
            } else {
                if (value instanceof GSObject) {
                    gsParams.put(key, (GSObject)value);                    
                } else {
                	JSONObject obj = null;
                	if (value instanceof Map) { 
	                    obj = new JSONObject((Map)value);
	                } else if (value instanceof JSONObject) {
	                	obj = (JSONObject)value;
	                } else {
	                    obj = new JSONObject(value);
	                }
                	// TODO: don't double translate, but gs JSONObject isn't kosher
                	value = new GSObject(obj.toString());

                	gsParams.put(key, (GSObject)value);
                }
            }
        }
        return gsParams;
    }

    private void validateParams() {
        Method[] methods = apiMethod.getMethods();
        for (Method method : methods) {
            if (AnnotationUtils.findAnnotation(method, Required.class) != null) {
                String paramName = getParamName(method);
                if (params == null || !params.containsKey(paramName)) {
                    // TODO: consider making this a checked exception
                    // who's the audience? If it's a required param, does it filter down to the client or do we assume
                    // it's handled on the front end and this is essentially a bug (therefor fail hard and fast).
                    throw new IllegalStateException("Required parameter '"+ paramName + "' was not provided.");
                }
            }
        }
    }

    /**
     * Assumes a "set" method following camel casing conventions.
     * 
     * @param method
     * @return
     */
    private String getParamName(Method method) {
        return method.getName().substring(3,4).toLowerCase() + method.getName().substring(4); 
    }

    private void initializeRequest() {
        apiMethodName = DynamicService.getApiMethodName(apiMethod);
        request = new GSRequest(gigyaToken, gigyaSecret, apiMethodName, true);
    }


    public static void main(String[] args) throws JSONException, ServiceException {
//        GetAccountInfo info = new AccountsImpl().GetAccountInfo();
//        info.setUid("_guid_TXxBdoebAUE5c7GXstNcNw==");
//        System.out.println(info.submit());
//
//        GetUserInfo uinfo = new SocializeImpl().GetUserInfo();
//        uinfo.setUid("_guid_TXxBdoebAUE5c7GXstNcNw==");
//        System.out.println(uinfo.submit());
//
//        GetSchema getSchema = new AccountsImpl().GetSchema();
//        GSResponse resp = getSchema.submit();
//        System.out.println(resp);
//        System.out.println(new JSONObject(resp.getData()).toString(4));
//        
//        String schema = "{"+
//	        "   fields: {"+
//	        "       'bcc.language': {"+
//	        "           type: 'string',"+
//	        "           writeAccess: 'serverOnly',"+
//	        "            format:\"regex('^[a-z]{2}$')\""+
//	        "        },"+
//	        "        'bcc.locale': {"+
//	        "            type: 'string',"+
//	        "            writeAccess: 'serverOnly',"+
//	        "            format:\"regex('^[a-z]{2}_[a-z]{2}$')\""+
//	        "        },"+
//	        "        'bcc.subscriptions.name': {"+
//	        "            type: 'string',"+
//	        "            writeAccess: 'serverOnly'"+
//	        "        }"+
//	        "    },"+
//	        "    dynamicSchema: false"+
//	        "}";
//        schema = "{"+
//    	        "   fields: {"+
//    	        "       'language': null,"+
//    	        "        'locale': null,"+
//    	        "        'subscriptions': null"+
//    	        "    },"+
//    	        "    dynamicSchema: true"+
//    	        "}";
//        SetSchema setSchema = accounts.SetSchema();
//        setSchema.setDataSchema(schema);
//        System.out.println(setSchema.submit());

//    	GigyaUserManager userDao = new GigyaUserManager();
//    	User user = userDao.get("_guid_TXxBdoebAUE5c7GXstNcNw==");
//    	
//    	AccountsImpl accounts = new AccountsImpl();
//
//    	GetSchema getSchema = accounts.GetSchema();
//        System.out.println(getSchema.submit());

//        GetPolicies getPolicies = accounts.GetPolicies();
//        try {
//        GSResponse resp = getPolicies.submit();
//        System.out.println(resp);
//
//        User user = response.toObject(User.class);

//        GetAccountInfo info = accounts.GetAccountInfo();
//        info.setUid("_guid_TXxBdoebAUE5c7GXstNcNw==");
//        System.out.println(info.submit());
//
//      	SetAccountInfo setInfo = accounts.SetAccountInfo();
//      	setInfo.setUid("_guid_TXxBdoebAUE5c7GXstNcNw==");
//      	setInfo.setData("{language: null, subscriptions: null}");
//      	System.out.println(setInfo.submit());
////
//      	info = accounts.GetAccountInfo();
//      	info.setUid("_guid_TXxBdoebAUE5c7GXstNcNw==");
//      	info.setInclude("data");
//      	System.out.println(info.submit());
    }
}
