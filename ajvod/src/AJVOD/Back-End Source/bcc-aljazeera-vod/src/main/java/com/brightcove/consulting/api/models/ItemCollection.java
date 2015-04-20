package com.brightcove.consulting.api.models;


/**
 * 
 * The item collection is the generic iterable type that is returned
 * from a Brightcove api request -- generally a video or a playlist
 * 
 * @author woladehin
 *
 */
public interface ItemCollection {

    /**
     * 
     * Returns the unique id related to a Brightcove DTO from the api
     *  
     * @return the unique id of this entity within a specific Brightcove account
     *
     */
    public long getId( );
   
    
}
