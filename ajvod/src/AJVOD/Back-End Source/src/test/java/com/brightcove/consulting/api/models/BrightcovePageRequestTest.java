package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcovePageRequest;

/**
 * Unit test for {@link BrightcovePageRequest}.
 * 
 * @author woladehin
 */
public class BrightcovePageRequestTest {

    
    @Test
    public void testBrightcovePageRequest( ) {
        
        Sort basicSort = new Sort( Direction.ASC, SortProperty.CREATION_DATE );
        BrightcovePageRequest pageRequest = new BrightcovePageRequest (1 , 10, basicSort );
        assertEquals( "The page request should be 1 ", pageRequest.getPageNumber( ) , 1 );
        assertEquals( "The page size should be 10", pageRequest.getPageSize( ) , 10 );
        assertEquals( "The sort order is ascendingd",pageRequest.getSort().getDirection() , Direction.ASC  );
        assertEquals( "The sort property is creation date", pageRequest.getSort().getSortProperty(), SortProperty.CREATION_DATE  );
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPreventsNegativePage() {

        new BrightcovePageRequest(-1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventsNegativeSize() {

        new BrightcovePageRequest(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventsZeroSize() {

        new BrightcovePageRequest(0, 0);
    }
}
