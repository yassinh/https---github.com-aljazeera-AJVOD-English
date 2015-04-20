package com.brightcove.consulting.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Provides support for working with commented properties.
 *
 * @author ssayles
 */
public class CommentedProperties {

	/**
	 * Loads a Properties object with properties have a preceding comment
	 * line that matches the given pattern.
	 * 
	 * @param resourceName
	 * @param regex
	 * @return
	 * @throws IOException
	 */
	public static Properties loadFromResource(String resourceName, String regex) throws IOException {
		URL resource = Thread.currentThread()
				.getContextClassLoader().getResource(resourceName);

		Properties properties = new Properties();
		FileReader fr = new FileReader(resource.getPath());
		BufferedReader br = new BufferedReader(fr);
		try {
	
			String line = br.readLine();
			boolean addLine = false;
			while (line != null && !StringUtils.isEmpty(line)) {
				line = line.trim();
				if (line.startsWith("#")) {
					if (line.matches(regex)) {
						addLine = true;
					}
				} else if (addLine) {
					String[] split = line.split("=");
					if (split.length > 1) {
						properties.put(split[0].trim(), split[1].trim());
					}
					addLine = false;
				}
				line = br.readLine();
			}
		} finally {
			try {br.close();}catch(Exception e){}
			try {fr.close();}catch(Exception e){}
		}
		return properties;
	}
	
}
