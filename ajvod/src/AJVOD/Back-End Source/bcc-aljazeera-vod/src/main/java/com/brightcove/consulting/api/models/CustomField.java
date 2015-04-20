package com.brightcove.consulting.api.models;

import org.apache.commons.lang.Validate;

/**
 * 
 * The custom field object implements the interface {@link BrightcovePair} to
 * support being used in media api calls. The Custom Field requires all three values
 * to be populated:
 * 
 * <ul>
 * <li> The custom field key, which is the property of the custom field within 
 * Brightcove</li>
 * <li> The custom field value, which is the value for that custom field that 
 * should be present on a video</li>
 * <li> The logic, which defines how this custom field shoudl be treated as a filter</li>
 * </ul>
 * 
 * @author woladehin
 *
 */
public class CustomField implements BrightcovePair {

    private String customFieldKey;
    private String customFieldValue;
    private BrightcoveLogic brightcoveLogic;
    
    
    /**
     * 
     * Constructore for a custom field that requires setting the correct key and value for a custom field
     * along with how to apply the custom field to search.
     * 
     * @param customFieldKey the name of the custom field property as it exists in Brightcove
     * @param customFieldValue the name of the custom field value which is normally a drop down/select value
     * or a free form field.
     * @param brightcoveLogic applies the Brightcove logic of AND,OR, and NOT to the api.
     * 
     */
    public CustomField( String customFieldKey, String customFieldValue, BrightcoveLogic brightcoveLogic ) {
        
        this.customFieldKey = customFieldKey;
        this.customFieldValue = customFieldValue;
        this.brightcoveLogic = brightcoveLogic;
        this.validateConstructor( );

    }

    
    /****************************** Getters ******************************/

    
    public BrightcoveLogic getBrightcoveLogic( ) {
       return brightcoveLogic;
    }

    
    public String getBrightcoveParameter( ) {
        return customFieldKey + ":" + customFieldValue;
    }
    
    
    private void validateConstructor( ){
        
        Validate.notNull(customFieldKey, "customFieldKey can't be null");
        Validate.notNull(customFieldValue, "customFieldValue can't be null");
        Validate.notNull(brightcoveLogic, "brightcoveLogic can't be null");
    
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((brightcoveLogic == null) ? 0 : brightcoveLogic.hashCode());
        result = prime * result
                + ((customFieldKey == null) ? 0 : customFieldKey.hashCode());
        result = prime
                * result
                + ((customFieldValue == null) ? 0 : customFieldValue.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomField other = (CustomField) obj;
        if (brightcoveLogic != other.brightcoveLogic)
            return false;
        if (customFieldKey == null) {
            if (other.customFieldKey != null)
                return false;
        } else if (!customFieldKey.equals(other.customFieldKey))
            return false;
        if (customFieldValue == null) {
            if (other.customFieldValue != null)
                return false;
        } else if (!customFieldValue.equals(other.customFieldValue))
            return false;
        return true;
    }

    
}
