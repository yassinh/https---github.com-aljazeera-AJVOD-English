package com.brightcove.consulting.api.models;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.brightcove.consulting.api.query.VideoField;

/**
 * 
 * Brightcove api calls can return two sets of high level metadata. The
 * first set are the standard Brightcove fields and the second set are the
 * custom fields. Since each api call requires a combination of those fields,
 * the Brightcove api fields object encapsulates the specific use cases that
 * can be expected when created a Brightcove api call and informing the api
 * which fields should be returned and when.
 * 
 * @author woladehin
 *
 */

public class BrightcoveApiFields {

    private EnumSet<VideoField> videoFields;
    private Set<String> customFields;
    
    
   /**
    * 
    * Default constructor will return no custom fields
    * and return all standard fields
    *
    */
    public BrightcoveApiFields( ) {
        
        this.customFields = null;
        this.videoFields = EnumSet.allOf( VideoField.class );
    
    }
    
    
    /**
     * 
     * Constructor that specifies which custom fields to
     * return and that all standard brightcove fields should
     * be returned. 
     * 
     * <p>
     * If the customFields is null we invalidate this constructor.
     * 
     * @param customFields the custom fields to return for an api call
     * 
     */
    public BrightcoveApiFields( Set<String> customFields ) {
    
        Validate.notNull(customFields, "Custom fields can't be null in the non-default constructor");

        this.customFields = customFields;
        this.videoFields = EnumSet.allOf( VideoField.class );
    
    }
    
    
    /**
     * 
     * Constructor that specifies which standard fields to
     * return and that no custom fields should
     * be returned. 
     * 
     * <p>
     * If the videoFields is null we invalidate this constructor.
     * 
     * @param videoFields the video fields to return for an api call
     * 
     */
    public BrightcoveApiFields( EnumSet<VideoField> videoFields ) {
    
        Validate.notNull(videoFields, "Video fields can't be null in the non-default constructor");

        this.customFields = null;
        this.videoFields = videoFields;
    
    }
    
    
    /**
     * 
     * Constructor that specifies which standard and custom fields to
     * return from an api call
     *  
     * <p>
     * If either the videoFields or the custom fields are null we invalidate this constructor.
     * 
     * @param videoFields the video fields to return for an api call
     * @param customFields the custom fields to return for an api call
     * 
     */
    public BrightcoveApiFields( Set<String> customFields, EnumSet<VideoField> videoFields ) {
    
        Validate.notNull(customFields, "Custom fields can't be null in the non-default constructor");
        Validate.notNull(videoFields, "Video fields can't be null in the non-default constructor");

        this.customFields = customFields;
        this.videoFields = videoFields;
    
    }
    
    
    /****************************** Getters ******************************/

    public EnumSet< VideoField > getVideoFields( ) {
        return videoFields;
    }


    public Set<String> getCustomFields( ) {
        return customFields;
    }
    
    
}
