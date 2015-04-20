package com.brightcove.consulting.api.models;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * 
 * The search term represents the standard format that YBTV uses for
 * searching against a video. For every search request, the search term
 * parameter determines
 * 
 * <ul>
 * <li> What is the search parameter itself</li>
 * <li> What is the filtering logic needed by Brightcove -- ALL, ANY, NONE</li>
 * <li> What is the individual search terms within the keyword</li>
 * </ul>
 * 
 * @author woladehin
 *
 */
public class SearchTerm {

    public static final BrightcoveLogic DEFAULT_BRIGHTCOVE_SEARCH_LOGIC = BrightcoveLogic.ALL;
    private BrightcoveLogic brightcoveLogic;
    private String search;
    
    
    /**
     * 
     * Constructor that takes a search term.
     *
     * @param search the search term for the query
     *
     */
    public SearchTerm( String search ) {
        
        this.search = search;
        this.brightcoveLogic = DEFAULT_BRIGHTCOVE_SEARCH_LOGIC;
        this.validateConstructor( );

    }
    
    /**
     * 
     * Constructor that changes the brightcove logic type to apply a different
     * set of search criteria.  
     * 
     * @param search the search term for the query
     * @param brightcoveLogic how should the logic apply for the search term --ANY, ALL, NOT
     *
     */
    public SearchTerm( String search, BrightcoveLogic brightcoveLogic ) {
        
        this.search = search;
        this.brightcoveLogic = brightcoveLogic;
        this.validateConstructor( );

    }

    
    /****************************** Getters ******************************/

    
    public BrightcoveLogic getBrightcoveLogic( ) {
        return brightcoveLogic;    
    }
    
    
    public String getSearchKey( ) {
        return search;
    }

    
    public List<String> getSearchParameters( ) {
        return Arrays.asList( search.split(" ") );
    }
    
    
    private void validateConstructor( ) {
        
        Validate.notNull( search, "search key can't be null" );
        Validate.notNull( brightcoveLogic, "boolean logic for search term can't be null" );
    
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((brightcoveLogic == null) ? 0 : brightcoveLogic.hashCode());
        result = prime * result + ((search == null) ? 0 : search.hashCode());
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
        SearchTerm other = (SearchTerm) obj;
        if (brightcoveLogic != other.brightcoveLogic)
            return false;
        if (search == null) {
            if (other.search != null)
                return false;
        } else if (!search.equals(other.search))
            return false;
        return true;
    }


}
