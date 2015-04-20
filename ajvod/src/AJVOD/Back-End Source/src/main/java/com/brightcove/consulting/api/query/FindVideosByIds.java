package com.brightcove.consulting.api.query;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.collections.Transformer;

import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.request.BrightcoveVideosRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;

public class FindVideosByIds extends VideoApi<BrightcoveVideo, BrightcoveVideosRequest> {
	
	/**
     * 
     * Default constructor requires the readToken
     * 
     * @param readToken the readToken for a Brightcove account
     * 
     */
    public FindVideosByIds(String readToken) {
        
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
    public FindVideosByIds(String readToken, Logger apiLogger) {
        
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
   @SuppressWarnings("unchecked")
   @Override
   public BrightcoveResponse<BrightcoveVideo> executeQuery( BrightcoveVideosRequest brightcoveVideoRequest,
			int size, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder) {

		ReadApi readApi = this.getReadApi( );
		Set<String> videoIds = brightcoveVideoRequest.getVideoIds();
		Set<Long> longIds = (Set<Long>)SetUtils.transformedSet(videoIds, new Transformer() {
			public Object transform(Object input) {
				return Long.parseLong((String) input);
			}
		});
		BrightcoveResponse<BrightcoveVideo> response;
		
		try {
			Videos videos = readApi.FindVideosByIds(getReadToken( ), longIds, convertVideoFields( ), convertCustomFields( ));
			
			response = convertToBrightcoveResponse ( videos, page );            
		} catch ( BrightcoveException e ) {            
			response = new BrightcoveExceptionResponse<BrightcoveVideo>( );
		}
		
		return response;
	}


}
