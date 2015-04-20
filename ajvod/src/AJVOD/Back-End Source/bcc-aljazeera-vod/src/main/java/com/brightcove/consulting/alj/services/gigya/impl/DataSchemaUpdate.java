package com.brightcove.consulting.alj.services.gigya.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.gigya.DataStore;
import com.brightcove.consulting.alj.services.gigya.DataStore.GetSchema;
import com.brightcove.consulting.alj.services.gigya.DataStore.SetSchema;
import com.gigya.socialize.GSResponse;

/**
 * Updates the Gigya DataStore schemas.
 *
 * @author ssayles
 */
public class DataSchemaUpdate {

	@Autowired
	private String gigyaDataSchema;

	@Autowired
	private DataStore gigyaDataStoreApi;

	public DataSchemaUpdate() {

	}

	public void execute() throws ServiceException, JSONException {
		JSONObject schemas = new JSONObject(gigyaDataSchema);

		String[] types = JSONObject.getNames(schemas);
		for (String type : types) {
			SetSchema setSchema = gigyaDataStoreApi.SetSchema();
			setSchema.setType(type.trim());
			JSONObject schema = (JSONObject) schemas.get(type);
			setSchema.setDataSchema(schema.get("dataSchema"));

			System.out.println("Setting schema for \"" + type + "\"");
			GSResponse response = setSchema.submit();
			System.out.println(response);
		}
//		
//		GetSchema getSchema = gigyaDataStoreApi.GetSchema();
//		getSchema.setType("playlist");
//		GSResponse response = getSchema.submit();
//		System.out.println(response);
	}

	public static void main(String[] args) {
		DataSchemaUpdate schemaUpdate = new DataSchemaUpdate();

		
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			ApplicationContext applicationContext = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml");
			
			applicationContext.getAutowireCapableBeanFactory().autowireBean(schemaUpdate);
			
			schemaUpdate.execute();
		} catch (Throwable t) {
			System.out.println(t);
			t.printStackTrace();
			System.exit(1);
			return;
		}

		System.exit(0);
	}

}
