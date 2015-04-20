package com.brightcove.consulting.api.models;

/**
 * 
 * For advanced api calls, Brightcove expects a maximum of two types 
 * of parameters. The Brightcove logic specifies if we are looking
 * for none of a value, that the value exists at all, or the value is required.
 * 
 * <p> Along with the boolean value, each Brightcove pair is required to give the
 * parameter value that is expected within the api. This is expected for handling
 * tags, custom fields, search, and playlists
 * 
 * @author woladehin
 *
 */
public interface BrightcovePair {

    /**
     * 
     * Determines how the parameters should be applied to the relevant parameters.
     * 
     * @return the Brightcove Logic entity that works in combination with the parameter
     *
     */
    public BrightcoveLogic getBrightcoveLogic( );
    
    /**
     * 
     * Formats the Brightcove parameter based on the request and the type of
     * data being requested. This should be implemented for any parameter that
     * Brightcove can take for filtering/searching across videos 
     * 
     * @return
     *
     */
    public String getBrightcoveParameter( );
    
}
