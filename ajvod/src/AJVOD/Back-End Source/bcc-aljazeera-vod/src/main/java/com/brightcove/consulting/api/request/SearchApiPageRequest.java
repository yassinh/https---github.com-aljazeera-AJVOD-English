package com.brightcove.consulting.api.request;

import java.util.ArrayList;
import java.util.List;

import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.models.Sort;

public class SearchApiPageRequest extends BrightcovePageRequest {

    private SearchTerm searchTerm;
    private List<BrightcovePair> brightcovePairs;
    
    
    public SearchApiPageRequest( int page, int size, SearchTerm searchTerm ) {
        
        super( page, size );
        
        if (searchTerm == null ) {
            throw new IllegalArgumentException( "The search term can not be undefined!" );
        }
        
        this.searchTerm = searchTerm;
    }
    
    
    public SearchApiPageRequest( int page, int size, List<BrightcovePair> brightcovePairs ) {
        
        super( page, size );
        
        if (brightcovePairs == null || brightcovePairs.size() < 1) {
            throw new IllegalArgumentException( "The search keys can not be undefined!" );
        }
        
        this.brightcovePairs = brightcovePairs;
        
    }
    
    
    public SearchApiPageRequest( int page, int size, SearchTerm searchTerm, List<BrightcovePair> brightcovePairs ) {
        
        super( page, size );
        
        if (searchTerm == null && (brightcovePairs == null || brightcovePairs.size() < 1)) {
            throw new IllegalArgumentException( "The search keys can not be undefined!" );
        }
        
        this.searchTerm = searchTerm;
        this.brightcovePairs = brightcovePairs;
        
    }
    

    public SearchApiPageRequest( int page, int size, Sort sort, SearchTerm searchTerm ) {
        super( page, size, sort );
        
        if (searchTerm == null ) {
            throw new IllegalArgumentException( "The search term can not be undefined!" );
        }
        
        this.searchTerm = searchTerm;
    }

    
    public SearchApiPageRequest( int page, int size, Sort sort, SearchTerm searchTerm, List<BrightcovePair> brightcovePairs ) {
        super( page, size, sort );
        
        if (searchTerm == null && (brightcovePairs == null || brightcovePairs.size() < 1)) {
            throw new IllegalArgumentException( "The search keys can not be undefined!" );
        }
        
        this.searchTerm = searchTerm;
        this.brightcovePairs = brightcovePairs;
    }
    
    
    public SearchApiPageRequest( int page, int size, Sort sort, List<BrightcovePair> brightcovePairs ) {
        super( page, size, sort );
        
        if (brightcovePairs == null || brightcovePairs.size() < 1) {
            throw new IllegalArgumentException( "The search keys can not be undefined!" );
        }
        
        this.brightcovePairs = brightcovePairs;
    }

    
    public SearchTerm getSearch( ) {
        return this.searchTerm;
    }
    
    
    public List<BrightcovePair> getBrightcovePairs( ) {
        return this.brightcovePairs;
    }

    public List<BrightcovePair> getBrightcovePairs(BrightcoveLogic logic) {
        if(this.brightcovePairs == null || this.brightcovePairs.size() < 1)
        	return null;
    	
    	List<BrightcovePair> bcPairs = new ArrayList<BrightcovePair>();
        for(BrightcovePair pair : this.brightcovePairs)
        {
        	if(pair.getBrightcoveLogic().equals(logic)) {
        		bcPairs.add(pair);
        	}
        }
    	return bcPairs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((brightcovePairs == null) ? 0 : brightcovePairs.hashCode());
        result = prime * result
                + ((searchTerm == null) ? 0 : searchTerm.hashCode());
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
        SearchApiPageRequest other = (SearchApiPageRequest) obj;
        if (brightcovePairs == null) {
            if (other.brightcovePairs != null)
                return false;
        } else if (!brightcovePairs.equals(other.brightcovePairs))
            return false;
        if (searchTerm == null) {
            if (other.searchTerm != null)
                return false;
        } else if (!searchTerm.equals(other.searchTerm))
            return false;
        return true;
    }

    
}
