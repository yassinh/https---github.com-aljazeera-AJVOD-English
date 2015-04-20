package com.brightcove.consulting.api.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class SearchApiPageRequestTest {

	@Test
    public void testSearchApiPageRequest( ) {
        
		SearchApiPageRequest request = new SearchApiPageRequest( 0, 10, new SearchTerm(""));
        assertEquals( request.getPageSize( ), 10 );
        assertEquals( request.getPageNumber( ), 0 );
        assertEquals( request.getSearch().getSearchKey(), "" );
        assertEquals( request.getSort( ).getDirection( ), Direction.NATURAL );
        assertEquals( request.getSort( ).getSortProperty( ), SortProperty.CREATION_DATE );
    
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPageNumber( ) {
        new SearchApiPageRequest(-1, 10, new SearchTerm(""));
    }
    //TODO: Can't test invalid search term because the constructor is ambiguous
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSearchKeys( ) {
        new SearchApiPageRequest(0, 10, new Sort( Direction.DESC, SortProperty.CREATION_DATE ), null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSizeNumber( ) {
        new SearchApiPageRequest(0, 0, new SearchTerm(""));
    }
}
