package com.brightcove.consulting.alj.models;

import java.util.ArrayList;

import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.request.SearchApiPageRequest;

import static com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum.PLAYS_TOTAL;
import static com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum.START_DATE;
import static com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum.DESC;
import static org.junit.Assert.*;


public class PlaylistIndexTest {

	@Test
	public void testOnlyNewPlaylistsThatHaveAMatchingUidWillBeAdded() {
    	PlaylistIndex index = new PlaylistIndex();
    	index.setUid("1");

    	index.add(new Playlist("123", "1"));
    	index.add(new Playlist("124", "1"));
    	index.add(new Playlist("125", "2"));
    	index.add(new Playlist("126"));

    	assertEquals(2, index.getPlaylistIds().size());
    	assertFalse(index.getPlaylistIds().contains("126"));
    	assertFalse(index.getPlaylistIds().contains("125"));
	}

    @Test
    public void testOrderShouldBePreservedWhenAddingPlaylists() {
    	
    	PlaylistIndex index = new PlaylistIndex();
    	index.setUid("1");

    	index.add(new Playlist("123", "1"));
    	index.add(new Playlist("124", "1"));
    	index.add(new Playlist("125", "1"));

    	List<String> playlistIds = index.getPlaylistIds();
    	assertEquals(3, playlistIds.size());
    	assertEquals("123", playlistIds.get(0));
    	assertEquals("124", playlistIds.get(1));
    	assertEquals("125", playlistIds.get(2));
    }

    @Test
    public void testItShouldSynchronizeTheInternalIndexToAGivenListEvenIfItDoesNotEntirelyMatch() {
    	PlaylistIndex index = new PlaylistIndex();
    	index.setUid("1");

    	// our initial list
    	index.add(new Playlist("123", "1"));
    	index.add(new Playlist("124", "1"));
    	index.add(new Playlist("125", "1"));

    	// a list that does not quite match because things got out of sync for
    	// for some reason
    	List<Playlist> playlists = new ArrayList<Playlist>();
    	playlists.add(new Playlist("126", "1"));
    	playlists.add(new Playlist("124", "1"));
    	playlists.add(new Playlist("123", "1"));

    	List<Playlist> orderedList = index.orderByIndexAndSync(playlists);
    	assertEquals(3, index.getPlaylistIds().size());
    	assertEquals(3, orderedList.size());

    	assertEquals("123", index.getPlaylistIds().get(0));
    	assertEquals("124", index.getPlaylistIds().get(1));
    	assertEquals("126", index.getPlaylistIds().get(2));

    	assertEquals("123", orderedList.get(0).getId());
    	assertEquals("124", orderedList.get(1).getId());
    	assertEquals("126", orderedList.get(2).getId());
    }
}
