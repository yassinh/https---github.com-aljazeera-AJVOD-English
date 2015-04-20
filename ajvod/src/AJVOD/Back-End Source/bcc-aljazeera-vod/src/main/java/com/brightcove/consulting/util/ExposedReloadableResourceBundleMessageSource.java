package com.brightcove.consulting.util;

import java.util.Properties;
import java.util.Set;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class ExposedReloadableResourceBundleMessageSource extends
		ReloadableResourceBundleMessageSource {

	private String baseName;

	public Set<Object> getKeys() {
		PropertiesHolder ph = getProperties(baseName);
		Properties properties = ph.getProperties();
		return properties.keySet();
	}

	@SuppressWarnings("unchecked")
	public void setBasename(String basename) {
		this.baseName = basename;
		super.setBasename(basename);
    }
}
