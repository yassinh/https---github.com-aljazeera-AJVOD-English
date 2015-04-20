package com.brightcove.consulting.config;

import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.brightcove.consulting.alj.models.NavRepository;
import com.brightcove.consulting.alj.utils.NavUtils;
import com.brightcove.consulting.util.FileScanner;

@Configuration
public class YBTVConfig {

	@Bean 
	public String gigyaToken() {
		return "3_waYiasagzKS2xBRA-7HpxtSg_T-M2rGRuBiujuHfx_zUVJHDzHw6tm75YMq6PNyS";
	}

    @Bean
    public String gigyaSecret() {
    	return "eCUUN4geoeI9PMgpennEQ5korm4dFKaTnMA8Z8GWX8A=";
    }
    
	/**
	 * API Read Token for Brightcove Video Cloud
	 * @return
	 */
	@Bean
	public String readToken() {
		return "74MPHIZqHAvFLkcrOzb8fzz8Zyurm-DGUiQ-YGru1eZpW0OHsa84Hw..";
	}

	@Bean
	public String webResourceRoot() {
		return "/resources"; // if app is deployed at server root (ie. AWS), use "/resources" vs "/aljazeera-vod/resources"
	}

	@Bean
	public String webRoot() {
		return "";  // if app is deployed at server root (ie. AWS), use "" vs ""
	}
	
	@Bean
	public String publisherId() {
		return "911453775001";
	}
	
	@Bean
	public String playerId() {
		return "2253104569001";
	}
	
	@Bean
	public String webDomain() {
		return "http://aljazeera-vod.elasticbeanstalk.com"; // if app is deployed on server use "http://aljazeera-vod.elasticbeanstalk.com"
	}

	@Bean
	public int maxPlaylists() {
		return 10;
	}

	@Bean
	public int maxPlaylistVideos() {
		return 100;
	}

	@Bean
	public int pageSize() {
		return 12;
	}
	@Bean
	public int pageSizeLargeCarousel() {
		return 24;
	}
	@Bean
	public int pageSizeSmallCarousel() {
		return 12;
	}
	
	@Bean
	public String featuredPlaylistId() {
		return "2245629251001";
	}

	@Bean
	public String navConfigRootDir() throws IOException {
        return new ClassPathResource("/channels").getFile().getAbsolutePath();
	}

	@Bean
	public NavRepository navRepository() {
	    NavRepository repository = new NavRepository();
	    return repository;
	}

	@Bean
	public NavUtils navUtils() {
	    NavUtils navUtils = new NavUtils();
	    return navUtils;
	}

	@Bean
	public FileScanner channelConfigScanner() throws IOException {
	    FileScanner scanner = new FileScanner();
	    FileFilter filter = new NameFileFilter("channel-config.xml");        
        scanner.setFilter(filter);
        scanner.setRootPath(navConfigRootDir());

	    return scanner;
	}

		
	@Bean
	public String channelCustomField() {
		return "channel";
	}

	@Bean
	public String videotypeCustomField() {
		return "video_type";
	}

	@Bean
	public String showGenreCustomField() {
		return "programme_type";
	}

	@Bean
	public String showCustomField() {
	    return "programme";
	}
 
    @Bean
    public String regionCustomField() {
        return "region";
    }
    
    @Bean
    public String eventCustomField() {
        return "eventhero";
    }

    @Bean
	public String topicCustomField() {
		return "aj_category";
	}

    /**
     * Custom field values for 'news' that are for videotype field.  It's 
     * assumed that all videos that have video type custom field with one of
     * these values is news.
     */
    @Bean
    public String[] newsCustomFieldValues() {
        return new String[] {"package","bulletin","rushes"};
    }

    /**
     * Custom field values for 'shows' that are for videotype field.  It's 
     * assumed that all videos that have video type custom field with one of
     * these values is a Show.
     */
    @Bean
    public String[] showsCustomFieldValues() {
        return new String[] {"promo", "programme", "programme_type"};
    }
}
