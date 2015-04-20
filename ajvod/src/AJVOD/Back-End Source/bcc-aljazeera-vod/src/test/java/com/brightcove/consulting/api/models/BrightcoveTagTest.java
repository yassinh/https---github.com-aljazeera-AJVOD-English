package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BrightcoveTagTest {

    @Test
    public void testBasicTagField( ) {
        Tag brightcoveTag = new Tag("tagName", BrightcoveLogic.ANY);
        assertEquals("The Brightcove search tag doesn't match",brightcoveTag.getBrightcoveParameter(),"tag:tagName");
        assertEquals("The tag name does not match", brightcoveTag.getBrightcoveLogic(),BrightcoveLogic.ANY);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullTagValue( ) {
        new Tag(null, BrightcoveLogic.ALL);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullTagLogic( ) {
        new Tag("tagName", null);
    }
}
