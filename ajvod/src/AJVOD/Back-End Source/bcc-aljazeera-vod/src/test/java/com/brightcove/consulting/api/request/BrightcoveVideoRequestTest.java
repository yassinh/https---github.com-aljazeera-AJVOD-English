package com.brightcove.consulting.api.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class BrightcoveVideoRequestTest {
    
    @Test
    public void testBrightcoveVideoRequest( ) {
        
        BrightcoveVideoRequest request = new BrightcoveVideoRequest( 0, 10, new Sort( Direction.DESC, SortProperty.CREATION_DATE ), "123");
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSort( ).getDirection( ), Direction.DESC );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.CREATION_DATE );
        assertEquals( request.getPlaylistId( ), "123" );
        assertEquals( request.getVideoId( ), "123" );
    
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPageNumber( ) {
        new BrightcoveVideoRequest(-1, 10, new Sort( Direction.DESC, SortProperty.CREATION_DATE ), "123");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidId( ) {
        new BrightcoveVideoRequest(0, 10, new Sort( Direction.DESC, SortProperty.CREATION_DATE ), null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSizeNumber( ) {
        new BrightcoveVideoRequest(0, 0, new Sort( Direction.DESC, SortProperty.CREATION_DATE ), "123");
    }

}
