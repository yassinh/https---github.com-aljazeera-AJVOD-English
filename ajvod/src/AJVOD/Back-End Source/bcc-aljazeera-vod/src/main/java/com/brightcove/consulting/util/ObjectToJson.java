package com.brightcove.consulting.util;

import java.beans.Encoder;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.consulting.alj.json.VideoJson;
import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.Topic;
import com.brightcove.consulting.api.models.BrightcoveVideo;

/**
 * Converts a given object to json.  This accounts for recursion by only traversing
 * a specified depth of object references.  By default, null properties are not
 * included and max traversal depth is 4.
 *
 * Example usage:
 *   
 *   String json = new ObjectToJson(videos).toJson();
 *
 *   String json2 = new ObjectToJson(myObj).maxDepth(6)
 *                                         .includeNulls()
 *                                         .toJson();
 *
 * @author ssayles
 *  
 */
// TODO: add date formatting support
public class ObjectToJson {

    private final Object object;
    private int maxDepth = 4;
    private boolean includeNulls = false;
    private boolean excludeEmptyObjects;

    // set of known properties that should be ignored when reflectively
    // gathering values for json objects.
    private static List<String> ignoreProperties = new ArrayList<String>();
    static {
        // ignore all object property methods
        Method[] methods = Object.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("is")) {
                ignoreProperties.add(StringUtils.uncapitalize(name.substring(2)));
            } else if (name.startsWith("get")) {
                ignoreProperties.add(StringUtils.uncapitalize(name.substring(3)));
            }
        }
        // this one is for Enums
        ignoreProperties.add("declaringClass");
    }

    public ObjectToJson(Object object) {
        this.object = object;
    }

    public ObjectToJson maxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public ObjectToJson excludeEmptyObjects() {
        this.excludeEmptyObjects = true;
        return this;
    }

    public ObjectToJson includeNulls() {
        this.includeNulls = true;
        return this;
    }

    public String toJson() {
        return wrap(object, 0).toString();
    }


    @SuppressWarnings("rawtypes")
    private Object wrap(Object object, int depth) {
        try {
            if (object == null) {
                return JSONObject.NULL;
            }
            if (object instanceof JSONObject || object instanceof JSONArray  ||
                    JSONObject.NULL.equals(object)      || object instanceof JSONString ||
                    object instanceof Byte   || object instanceof Character  ||
                    object instanceof Short  || object instanceof Integer    ||
                    object instanceof Long   || object instanceof Boolean    ||
                    object instanceof Float  || object instanceof Double     ||
                    object instanceof String) {
                return object;
            }

            if (object instanceof Collection) {
                return convert((Collection)object, depth);
            }
            if (object.getClass().isArray()) {
                return convertArray(object, depth);
            }
            if (object instanceof Map) {
                return convert((Map)object, depth);
            }
            if (object.getClass().isEnum()) {
                return object.toString();
            }
            if (object instanceof Date) {
                return DateFormatUtils.ISO_DATE_FORMAT.format((Date)object);
            }

            return convert(object, depth);
        } catch(Exception exception) {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private JSONArray convert(Collection collection, int depth) {
        JSONArray array = new JSONArray();
        if (depth+1 <= maxDepth) {
            for (Object obj : collection) {
                Object jsonObj = wrap(obj, depth + 1);
                if (jsonObj != null) {
                    array.put(jsonObj);
                }
            }
        }
        return array;
    }

    private JSONArray convertArray(Object array, int depth) {
        JSONArray jsonArray = new JSONArray();
        if (array.getClass().isArray()) {
            if (depth+1 <= maxDepth) {
                int length = Array.getLength(array);
                for (int i = 0; i < length; i += 1) {
                    jsonArray.put(wrap(Array.get(array, i), depth+1));
                }
            }
        }
        return jsonArray;
    }

    @SuppressWarnings("rawtypes")
    private JSONObject convert(Object object, int depth) {
        BeanMap bean = new BeanMap(object);
        return convert((Map)bean, depth);
    }

    @SuppressWarnings("rawtypes")
    private JSONObject convert(Map map, int depth) {
        JSONObject jsonObj = new JSONObject();
        if (depth+1 <= maxDepth) {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                
                // skip traversal over attributes that are json arrays if the array
                // itself reaches maxDepth
                // i.e. maxDepth of 1 should give us
                // {name: 'foo'}
                // and not
                // {name: 'foo'
                //   shows: []}
                if (value != null) {
                    if (value.getClass().isArray() || value instanceof Collection) {
                        if (depth+2 >= maxDepth) {
                            continue;
                        }
                    }
                }

                if (ignoreProperties.indexOf(key) == -1) {
                	try {                		                	
	                    if (value == null && includeNulls) {
	                        jsonObj.put(key.toString(), JSONObject.NULL);
	                    } else if (value != null) {
	                        Object wrap = wrap(value, depth+1);
	                        if (!(excludeEmptyObjects && isEmpty(wrap))) {
	                        	Object tempValue = wrap(value, depth+1);
	                        	/*if(key.equals("title") ){
	                        		tempValue = (Object) StringEscapeUtils.escapeJavaScript((String) tempValue);
	                        		tempValue = ((String)tempValue).replace("\\", "\\\\");
	                        	}*/
	                            jsonObj.put(key.toString(), tempValue);
	                        }
	                    }
                	} catch (JSONException e) {						
						e.printStackTrace();
					}
                }
            }
        }
        return jsonObj;
    }
    
    private boolean isEmpty(Object object) {
        if (object instanceof JSONArray) {
            return ((JSONArray)object).length() == 0;
        }
        if (object instanceof JSONObject) {
            return ((JSONObject)object).length() == 0;
        }
        return false;
    }


}
