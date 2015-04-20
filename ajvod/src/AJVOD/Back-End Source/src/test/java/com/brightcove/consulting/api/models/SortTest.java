package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;

public class SortTest {
    
    @Test
    public void testSortCreation( ) {
        Sort basicSort = new Sort( Direction.ASC, SortProperty.CREATION_DATE );
        assertEquals( basicSort.getDirection( ), Direction.ASC );
        assertEquals( basicSort.getSortProperty( ), SortProperty.CREATION_DATE );
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSortPropertyValue( ) {
        new Sort( Direction.ASC, null );
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSortDirectionLogic( ) {
        new Sort( null, SortProperty.CREATION_DATE );
    }
    
    @Test
    public void testSortPropertyEnumeration( ) throws Exception {
        assertEquals( SortProperty.DISPLAY_NAME, SortProperty.fromString("display_name") );
        assertEquals( SortProperty.REFERENCE_ID, SortProperty.fromString("reference_id") );
        assertEquals( SortProperty.PLAYS_TOTAL, SortProperty.fromString("plays_total") );
        assertEquals( SortProperty.PLAYS_TRAILING_WEEK, SortProperty.fromString("plays_trailing_week") );
        assertEquals( SortProperty.START_DATE, SortProperty.fromString("start_date") );
        assertEquals( SortProperty.PUBLISH_DATE, SortProperty.fromString("publish_date") );
        assertEquals( SortProperty.CREATION_DATE, SortProperty.fromString("creation_date") );
        assertEquals( SortProperty.MODIFIED_DATE, SortProperty.fromString("modified_date") );
        assertEquals( SortProperty.NATURAL, SortProperty.fromString("natural") );

    }

    @Test( expected = IllegalArgumentException.class )
    public void testInvalidSortPropertyEnum( ) throws Exception {
        SortProperty.fromString("invalid");
    }
    
    @Test
    public void testDirectionEnumeration( ) throws Exception {
        assertEquals( Direction.ASC, Direction.fromString("asc") );
        assertEquals( Direction.DESC, Direction.fromString("desc") );
        assertEquals( Direction.NATURAL, Direction.fromString("natural") );

    }

    @Test( expected = IllegalArgumentException.class )
    public void testInvalidDirectionEnum( ) throws Exception {
        Direction.fromString("invalid");
    }
}
