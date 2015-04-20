package com.brightcove.consulting.api.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class PlaylistPageRequestTest {
	
	@Test
    public void testPlaylistSimplePageRequest( ) {
        
		PlaylistPageRequest request = new PlaylistPageRequest( 0, 10 );
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSort( ).getDirection( ), Direction.NATURAL );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.CREATION_DATE );
    
    }
	
	@Test
    public void testPlaylistPageRequest( ) {
        
        PlaylistPageRequest request = new PlaylistPageRequest( 0, 10, "123");
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSort( ).getDirection( ), Direction.NATURAL );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.CREATION_DATE );
        assertEquals( request.getPlayerId( ), "123" ); 
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPageNumber( ) {
        new PlaylistPageRequest(-1, 10, "123");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPlayerId( ) {
        new PlaylistPageRequest(0, 10, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSizeNumber( ) {
        new PlaylistPageRequest(0, 0, "123");
    }

}
