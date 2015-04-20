package com.brightcove.consulting.api.request;

import com.brightcove.consulting.api.models.Sort;

public class BrightcoveVideoRequest extends BrightcovePageRequest {

	private String id;
	
    public BrightcoveVideoRequest( int page, int size, Sort sort, String id ) {
        super(page, size, sort);
        
        if (id == null || id.length() < 1 ) {
            throw new IllegalArgumentException( "The id must be populated!" );
        }
        
        this.id = id;
    }
    
    public String getPlaylistId(){
    	return id;
    }
    
    public String getVideoId(){
    	return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        BrightcoveVideoRequest other = (BrightcoveVideoRequest) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
