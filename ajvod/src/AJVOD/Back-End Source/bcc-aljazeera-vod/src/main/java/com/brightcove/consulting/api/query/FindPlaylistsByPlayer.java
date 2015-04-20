package com.brightcove.consulting.api.query;

import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Playlists;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcovePlaylist;
import com.brightcove.consulting.api.request.PlaylistPageRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;

public class FindPlaylistsByPlayer extends PlaylistApi<BrightcovePlaylist, PlaylistPageRequest> {

	/**
     *
	 * Default constructor requires the readToken
     * 
     * @param readToken the readToken for a Brightcove account
     * 
     */
    public FindPlaylistsByPlayer(String readToken) {
        
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
    public FindPlaylistsByPlayer(String readToken, Logger apiLogger) {
        
        super(readToken, apiLogger);
    
    }


    /**
     *
     * Retrieves a subset of videos from the Brightcove account taking into account the 
     * # of videos requested and the page of videos requested. After making a Brightcove
     * request, the videos are formatted to return an object of type BrightcoveResponse.
     * 
     * @param playlistPageRequest the request from the user for specific videos.
     *
     */
    @Override
    public BrightcoveResponse<BrightcovePlaylist> executeQuery( PlaylistPageRequest playlistPageRequest,
			int size, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder) {

		ReadApi readApi = this.getReadApi( );
		String playerId = playlistPageRequest.getPlayerId();
		BrightcoveResponse<BrightcovePlaylist> response;
		
		try {
			Playlists playlists = readApi.FindPlaylistsForPlayerId(getReadToken( ), playerId, size, page, convertVideoFields( ), convertCustomFields( ), convertPlaylistFields());
			
			response = convertToBrightcoveResponse ( playlists, page );            
		} catch ( BrightcoveException e ) {            
			response = new BrightcoveExceptionResponse<BrightcovePlaylist>( );
		}
		
		return response;
	}



}
