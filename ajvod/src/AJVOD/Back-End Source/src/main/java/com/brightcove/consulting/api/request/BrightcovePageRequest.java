package com.brightcove.consulting.api.request;

import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class BrightcovePageRequest {
    
    private int page;
    private int size;
    private Sort sort;
    
    
    /**
     * 
     * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page} will return the first
     * page. The sort order is defaulted to the natural sort order for a given request.
     * 
     * @param page the page of results requested
     * @param size how many videos to return per this page
     * 
     */
    public BrightcovePageRequest( int page, int size ) {
        this( page, size, new Sort( Direction.NATURAL, SortProperty.CREATION_DATE ) );
    }

    /**
     * 
     * Creates a new {@link BrightcovePageRequest} with sort parameters applied.
     * Pages are zero indexed, thus providing 0 for {@code page} will return the first
     * page.
     * 
     * @param page the page of results requested
     * @param size how many videos to return per this page
     * @param sort the type of sort to apply to the page
     * 
     */
    public BrightcovePageRequest( int page, int size, Sort sort ) {

        if ( 0 > page ) {
            throw new IllegalArgumentException( "Page index must not be less than zero!" );
        }

        if ( 0 >= size ) {
            throw new IllegalArgumentException( "Size must not be less than or equal to zero!" );
        }

        this.page = page;
        this.size = size;
        this.sort = sort;
    
    }

    
    public int getPageSize( ) {
        return size;
    }

    
    public int getPageNumber( ) {
        return page;
    }

    
    public Sort getSort( ) {
        return sort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + page;
        result = prime * result + size;
        result = prime * result + ((sort == null) ? 0 : sort.hashCode());
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
        BrightcovePageRequest other = (BrightcovePageRequest) obj;
        if (page != other.page)
            return false;
        if (size != other.size)
            return false;
        if (sort == null) {
            if (other.sort != null)
                return false;
        } else if (!sort.equals(other.sort))
            return false;
        return true;
    }
    
    
}
