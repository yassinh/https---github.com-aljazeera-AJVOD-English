package com.brightcove.consulting.api.query;

import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;

/**
 * Implements the {@link MediaApi} interface for retrieving all videos
 * from a given Brightcove account. Use this api when needing to return
 * all videos from an account without the need to filter them. The All Videos
 * api allows for specifying different types of sorting parameters via the 
 * brightcove page request
 * 
 * @author woladehin
 *
 */
public class FindAllVideos extends VideoApi<BrightcoveVideo, BrightcovePageRequest> {

    /**
     * 
     * Default constructor requires the readToken
     * 
     * @param readToken the readToken for a Brightcove account
     * 
     */
    public FindAllVideos(String readToken) {
        
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
    public FindAllVideos(String readToken, Logger apiLogger) {
        
        super(readToken, apiLogger);
    
    }
    

    /**
     *
     * Retrieves a subset of videos from the Brightcove account taking into account the 
     * # of videos requested and the page of videos requested. After making a Brightcove
     * request, the videos are formatted to return an object of type BrightcoveResponse.
     * 
     * @param brightcovePageRequest the request from the user for specific videos.
     *
     */
    @Override
    public BrightcoveResponse<BrightcoveVideo> executeQuery( BrightcovePageRequest brightcovePageRequest,
			int size, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder) {

		ReadApi readApi = this.getReadApi( );
		BrightcoveResponse<BrightcoveVideo> response;
		
		try {
			Videos videoList = readApi.FindAllVideos( getReadToken( ), size, page, 
			                                sortBy , sortOrder , convertVideoFields( ), convertCustomFields( ) );
			
			response = convertToBrightcoveResponse ( videoList, page );            
		} catch ( BrightcoveException e ) {            
			response = new BrightcoveExceptionResponse<BrightcoveVideo>( );
		}
		
		return response;
	}

}
