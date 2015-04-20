package com.brightcove.consulting.api.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class BrightcovePageRequestTest {

    @Test
    public void testBrightcoveSimplePageRequest( ) {
        
        BrightcovePageRequest request = new BrightcovePageRequest( 0, 10 );
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSort( ).getDirection( ), Direction.NATURAL );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.MODIFIED_DATE );
    
    }
    
    @Test
    public void testBrightcovePageRequest( ) {
        
        BrightcovePageRequest request = new BrightcovePageRequest( 0, 10, new Sort( Direction.DESC, SortProperty.CREATION_DATE ) );
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSort( ).getDirection( ), Direction.DESC );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.CREATION_DATE );
    
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPageNumber( ) {
        new BrightcovePageRequest(-1, 10);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSizeNumber( ) {
        new BrightcovePageRequest(0, 0);
    }
    
    
}
