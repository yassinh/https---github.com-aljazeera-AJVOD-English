package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;
import com.brightcove.consulting.api.query.VideoField;

public class BrightcoveApiFieldsTest {

    @Test
    public void testBrightcoveApiFields( ) {
        
        BrightcoveApiFields brightcoveApi = new BrightcoveApiFields( );
        assertEquals( null, brightcoveApi.getCustomFields( ) );
        assertEquals( EnumSet.allOf(VideoField.class) , brightcoveApi.getVideoFields( ) );
        
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullBrightcoveApiField( ) {
        new BrightcoveApiFields( ( Set<String> ) null );
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void testNullBrightcoveVideoField( ) {
        new BrightcoveApiFields( ( EnumSet<VideoField> ) null );
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void testAllNullParameters( ) {
        new BrightcoveApiFields( ( Set<String> ) null, ( EnumSet<VideoField> ) null );
    }
    
    
}
