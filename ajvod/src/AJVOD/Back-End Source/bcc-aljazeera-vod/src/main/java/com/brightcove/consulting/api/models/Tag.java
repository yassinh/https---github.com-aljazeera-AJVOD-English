package com.brightcove.consulting.api.models;

import org.apache.commons.lang.Validate;

public class Tag implements BrightcovePair {
    
    private final String BRIGHTCOVE_SEARCH_TAG_KEY = "tag";
    private String tagValue;
    private BrightcoveLogic brightcoveLogic;
    
    
    public Tag( String tagValue, BrightcoveLogic brightcoveLogic ) {    
        
        this.tagValue = tagValue;
        this.brightcoveLogic = brightcoveLogic;
        this.validateConstructor( );
    
    }

    
    public BrightcoveLogic getBrightcoveLogic() {
        return brightcoveLogic;
    }
    
    
    private void validateConstructor( ){
        
        Validate.notNull( tagValue, "Tag Value can't be null" );
        Validate.notNull( brightcoveLogic, "Brightcove logic--ALL,ANY,NOT can't be null" );

    }

    
    public String getBrightcoveParameter() {
        return BRIGHTCOVE_SEARCH_TAG_KEY + ":" + tagValue;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + BRIGHTCOVE_SEARCH_TAG_KEY.hashCode();
        result = prime * result
                + ((brightcoveLogic == null) ? 0 : brightcoveLogic.hashCode());
        result = prime * result
                + ((tagValue == null) ? 0 : tagValue.hashCode());
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
        Tag other = (Tag) obj;
        if (BRIGHTCOVE_SEARCH_TAG_KEY == null) {
            if (other.BRIGHTCOVE_SEARCH_TAG_KEY != null)
                return false;
        } else if (!BRIGHTCOVE_SEARCH_TAG_KEY
                .equals(other.BRIGHTCOVE_SEARCH_TAG_KEY))
            return false;
        if (brightcoveLogic != other.brightcoveLogic)
            return false;
        if (tagValue == null) {
            if (other.tagValue != null)
                return false;
        } else if (!tagValue.equals(other.tagValue))
            return false;
        return true;
    }

    
}
