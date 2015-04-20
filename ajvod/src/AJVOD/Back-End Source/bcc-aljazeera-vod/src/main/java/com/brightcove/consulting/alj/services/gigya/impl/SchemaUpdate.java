package com.brightcove.consulting.alj.services.gigya.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.brightcove.consulting.alj.services.ServiceException;
import com.brightcove.consulting.alj.services.gigya.Accounts;
import com.brightcove.consulting.alj.services.gigya.Accounts.SetSchema;
import com.gigya.socialize.GSResponse;

/**
 * 
 * @author ssayles
 */
public class SchemaUpdate {

	@Autowired
	private String gigyaUserDataSchema;

	@Autowired
	private Accounts gigyaAccountsApi;

	public SchemaUpdate() {

	}

	public void execute() throws ServiceException {
		SetSchema setSchemaRequest = gigyaAccountsApi.SetSchema();
		setSchemaRequest.setDataSchema(gigyaUserDataSchema);
		GSResponse response = setSchemaRequest.submit();
		System.out.println(response);
	}

	public static void main(String[] args) {
		SchemaUpdate schemaUpdate = new SchemaUpdate();

		
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
