package com.brightcove.consulting.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveExceptionResponse;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;

public class FindVideosBySearchFields extends VideoApi< BrightcoveVideo, SearchApiPageRequest > {

    private List<String> noneBrightcoveFields;
    private List<String> anyBrightcoveFields;
    private List<String> allBrightcoveFields;
    

    /**
     * 
     * Default constructor requires the readToken
     * 
     * @param readToken the readToken for a Brightcove account
     * 
     */
    public FindVideosBySearchFields(String readToken) {
        
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
    public FindVideosBySearchFields( String readToken, Logger apiLogger ) {
        
        super(readToken, apiLogger);
    
    }


    @Override
    public BrightcoveResponse<BrightcoveVideo> executeQuery( SearchApiPageRequest searchApiPageRequest,
			int size, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder) {
        
        ReadApi readApi = this.getReadApi();
        BrightcoveResponse<BrightcoveVideo> response;
        
        // Search for all keywords
        createBrightcoveSearchFields( searchApiPageRequest );
     
        try {        
            Videos videoList = readApi.SearchVideos( getReadToken( ), this.allBrightcoveFields, this.anyBrightcoveFields, 
                                                     this.noneBrightcoveFields, false, sortBy , sortOrder , size, page, 
                                                     convertVideoFields( ), convertCustomFields( ) );
            
            response = convertToBrightcoveResponse ( videoList, page );            
        } catch ( BrightcoveException e ) {            
            response = new BrightcoveExceptionResponse<BrightcoveVideo>( );
        }        
        return response;
    }

    private void createBrightcoveSearchFields ( SearchApiPageRequest searchApiPageRequest ) {
        
        SearchTerm searchTerm = searchApiPageRequest.getSearch();
        List< BrightcovePair > pairs = searchApiPageRequest.getBrightcovePairs();
        
        //Define the any list
        this.anyBrightcoveFields = filterBooleanList( searchTerm, pairs, BrightcoveLogic.ANY );
        
        //Define the all list
        this.allBrightcoveFields = filterBooleanList( searchTerm, pairs, BrightcoveLogic.ALL );
        
        //Define the Not list
        this.noneBrightcoveFields = filterBooleanList( searchTerm, pairs, BrightcoveLogic.NOT );
    
    }


    private List<String> filterBooleanList(SearchTerm searchTerm, List<BrightcovePair> pairs, BrightcoveLogic booleanLogic) {
        
        List<String> filters = new ArrayList< String >( );
        
        filters = createFilterListFromBrightcovePairs( pairs, filters, booleanLogic );
        filters = createFilterListFromBrightcoveSearch( searchTerm, filters, booleanLogic );
        filters = createProperResponseToApis( filters );
        
        return filters;
    
    }

    
    private List<String> createFilterListFromBrightcovePairs( List<BrightcovePair> pairs, List<String> filters, BrightcoveLogic booleanLogic ) {
        if (pairs != null) {

	        for( BrightcovePair brightcovePair : pairs ) {
	            
	            if( brightcovePair.getBrightcoveLogic( ).equals( booleanLogic ) ) {
	                
	                filters.add( brightcovePair.getBrightcoveParameter( ) );
	                
	            }
	            
	        }
        }
        return filters;
        
    }
    
    
    private List<String> createFilterListFromBrightcoveSearch( SearchTerm searchTerm, List<String> filters, BrightcoveLogic booleanLogic ) {
        if (searchTerm != null) {
        
	        if( searchTerm.getBrightcoveLogic( ).equals( booleanLogic ) ) {
	            
	            if( filters != null ) {
	                
	                List< String > searchFilters = searchTerm.getSearchParameters( );
	                for(String searchFilter : searchFilters ) {
	                    
	                    filters.add( searchFilter );
	                
	                }
	                
	            } else {
	                
	                filters = searchTerm.getSearchParameters( );
	                
	            }
	            
	        }
        }        
        return filters;
        
    }
    
    
    private List<String> createProperResponseToApis( List<String> filters ) {
        
        if( filters.isEmpty( ) ){
         
            return null;
        
        } else {
          
            return filters;
        
        }
    }




}
