package com.brightcove.consulting.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.brightcove.commons.catalog.objects.CustomField;
import com.brightcove.consulting.api.models.BrightcoveVideo;

public class BcVideoTest {

	
	@Test
	public void createBaseYbtvVideo( ){
		
		Date now = new Date( );
		BrightcoveVideo video = new BrightcoveVideo ( 1000L, "referenceId", "This is the short description", "This is the long description", now, 
				"http://www.brightcove.com", "Brightcove Link", "http://brightcoveImage", 83403L, 100L, "Name of the Video", null );
		
		assertEquals("Did not return the correct video reference id in getter", "referenceId",video.getReferenceId( ));
		assertEquals("Did not return the correct video short description in getter", "This is the short description",video.getShortDescription( ));
		assertEquals("Did not return the correct video long description in getter", "This is the long description",video.getLongDescription( ));
		assertEquals("Did not return the correct video displaydate in getter", now,video.getDate());
		assertEquals("Did not return the correct video linkurl in getter", "http://www.brightcove.com",video.getLinkUrl());
		assertEquals("Did not return the correct video linkText in getter", "Brightcove Link",video.getLinkText( ));
		assertEquals("Did not return the correct video imageUrl in getter", "http://brightcoveImage",video.getImageUrl( ));
		assertEquals("Did not return the correct video videoLength in getter", (Long)83403L,video.getVideoLength( ));
		assertEquals("Did not return the correct video views in getter", (Long)100L,video.getViews( ));
		assertEquals("Did not return the correct video name in getter", "Name of the Video",video.getName( ));
		
	}
	
 
}
