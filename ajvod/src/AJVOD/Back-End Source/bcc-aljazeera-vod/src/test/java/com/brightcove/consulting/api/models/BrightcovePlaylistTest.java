package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import com.brightcove.consulting.api.models.BrightcoveVideo;

public class BrightcovePlaylistTest {

    @Test
    public void testBasicPlaylist( ){
        
        List<Long> videoIds = new ArrayList<Long>( Arrays.asList( 1000000L, 50900019L, 60606060L ) );  
        List<String> filterTags =  new ArrayList<String>( Arrays.asList( "category", "subcategory", "external" ) );
        List<BrightcoveVideo> videos = new ArrayList<BrightcoveVideo>( );
        videos.add( new BrightcoveVideo( ) );
        
        
        BrightcovePlaylist playlist = new BrightcovePlaylist( 10101010L ,  "video reference id is awesome", "This has got to be the name of my video", "short description", 
                videoIds, videos, "http://mythumbnail.com",
                filterTags, PlaylistType.ALPHABETICAL, 102039201L );
        
        assertEquals( "Brightcove Id's should match", playlist.getId(), 10101010L );
        assertEquals( "Brightcove Reference Id's should match", playlist.getReferenceId( ), "video reference id is awesome" );
        assertEquals( "Brightcove short Description should match", playlist.getShortDescription(), "short description" );
        assertEquals( "Brightcove name should match", playlist.getName(), "This has got to be the name of my video" );
        assertEquals( "Brightcove sort should be alphabetical", playlist.getPlaylistType(), PlaylistType.ALPHABETICAL);        
        assertEquals( "Brightcove thumbnail url should match", playlist.getThumbnailUrl(), "http://mythumbnail.com");
        assertEquals( "Brightcove account id should match", playlist.getAccountId(), 102039201L);

        assertNotNull( "Brightcove tags should not be null", playlist.getFilterTags( ) );
        assertNotNull( "Brightcove video Ids should not be null", playlist.getVideoIds( ) );
        assertNotNull( "Brightcove videos should not be null", playlist.getVideos( ) );

    }
}
