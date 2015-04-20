package com.brightcove.consulting.api.query;

import com.brightcove.consulting.api.models.ItemCollection;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

/**
 * 
 * Interface that returns a video or playlist response object from Brightcove. 
 * The class that implements MediaApi must extend the generics for {@link ItemCollection} 
 * for the return type and also the {@link BrightcovePageRequest} for the type of request from
 * Brightcove. 
 * <p>
 * The BrightcoveResponse must be able to take the given type of request and return a 
 * response back to the user. The response can either be a valid response from Brightcove
 * or an exception response specifying that something was wrong with the request.
 *
 * @see         ItemCollection, BrightcovePageRequest
 *
 */
public interface MediaApi< T extends ItemCollection, S extends BrightcovePageRequest > {
    
    public BrightcoveResponse<T> query( S brightcovePageRequest );

}
