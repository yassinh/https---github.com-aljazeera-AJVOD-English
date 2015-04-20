package com.brightcove.consulting;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavRepository;
import com.brightcove.consulting.config.GigyaConfig;

public class GigyaConfigResolver {

	@Autowired
	NavRepository navRepository;
	
    public GigyaConfig resolveConfig(Locale locale) {
        String language = locale.getLanguage();
        GigyaConfig config = new GigyaConfig();
        Collection<Channel> channels = navRepository.getChannels().values();
        for (Channel channel : channels) {
			if (StringUtils.equals(channel.getLanguage(), language)) {
				config.setChannel(channel.getConfig("gigyaChannel"));
				config.setLanguage(language);
				config.setToken(channel.getConfig("gigyaToken"));
			}
		}
        return config;
    }
}
