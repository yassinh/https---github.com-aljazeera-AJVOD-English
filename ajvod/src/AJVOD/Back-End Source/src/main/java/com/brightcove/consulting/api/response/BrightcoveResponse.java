package com.brightcove.consulting.api.response;

import java.util.List;

import com.brightcove.consulting.api.models.ItemCollection;

public interface BrightcoveResponse<T extends ItemCollection> {
    
    /**
     * The total number of videos in the collection.
     * @return
     */
    public long getTotalCount( );
    
    /**
     * Which page of the results this ItemCollection represents.
     * @return
     */
    public int getPageNumber( );
    
    /**
     * How many items a page consists of.
     * @return
     */
    public int getPageSize( );
    
    /**
     * The actual items that this collection contains.
     * @return
     */
    public List<T> getBrightcoveItemCollection( );
    
}
