package com.brightcove.consulting.api.query;

import java.util.logging.Logger;

import org.springframework.cache.annotation.Cacheable;

import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;

public class FindVideoById extends VideoApi<BrightcoveVideo, BrightcoveVideoRequest> {
	
	/**
     * 
     * Default constructor requires the readToken
     * 
     * @param readToken the readToken for a Brightcove account
     * 
     */
    public FindVideoById(String readToken) {
        
        super(readToken);
    
    }

    
    /**
     * 
     * Constructor which enables logging for a specific api
     * request.
     * 
     * @param readToken the readToken for a Brightcove account
     * @param apiLogger the logger for this request
     *
     */
    public FindVideoById(String readToken, Logger apiLogger) {
        
        super(readToken, apiLogger);
    
    }


    /**
    *
    * Retrieves a subset of videos from the Brightcove account taking into account the 
    * # of videos requested and the page of videos requested. After making a Brightcove
    * request, the videos are formatted to return an object of type BrightcoveResponse.
    * 
    * @param brightcoveVideoRequest the request from the user for specific videos.
    *
    */
   @Override
   public BrightcoveResponse<BrightcoveVideo> executeQuery( BrightcoveVideoRequest brightcoveVideoRequest,
			int size, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder) {

		ReadApi readApi = this.getReadApi( );
		long videoId = Long.parseLong(brightcoveVideoRequest.getVideoId());
		BrightcoveResponse<BrightcoveVideo> response;
		
		try {
			Video video = readApi.FindVideoById(getReadToken( ), videoId, convertVideoFields( ), convertCustomFields( ));
			
			response = convertToBrightcoveResponse ( video, page );            
		} catch ( BrightcoveException e ) {            
			response = new BrightcoveExceptionResponse<BrightcoveVideo>( );
		}
		
		return response;
	}


}
