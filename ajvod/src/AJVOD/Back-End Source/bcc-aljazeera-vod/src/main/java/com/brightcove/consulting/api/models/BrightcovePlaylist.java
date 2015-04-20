package com.brightcove.consulting.api.models;

import java.util.List;

/**
 * 
 * The BrightcovePlaylist object encapsulates basic playlist functionality. This object is responsible for
 * encapsulating the common use cases for playlist entities. When a project
 * requires mapping or formatting playlists different,  the mapping and formatting
 * of the playlist response should take place within this DTO. All Brightcove playlists should implement
 * a ItemCollection, {@link ItemCollection} so that they can be returned as part of a Brightcove api
 * response. They should also contain a list of videos related to the play
 * 
 * @author woladehin
 *
 */
public class BrightcovePlaylist implements ItemCollection {

    private Long id;
    private String referenceId;
    private String name;
    private String shortDescription;
    private List<Long> videoIds;
    private List<BrightcoveVideo> videos;
    private String thumbnailUrl;
    private List<String> filterTags;
    private PlaylistType playlistType;
    private long accountId;
    
    
    /**
     * 
     * Default constructor
     *
     */
    public BrightcovePlaylist( ) {
        
    }
    
    
    /**
     * 
     * 
     * @param id the id of the playlist unique to Brightcove
     * @param referenceId the Brightcove reference id that is manually created by the user 
     * @param name the name of the playlist
     * @param shortDescription the description of the playlist
     * @param videoIds the set of unique Brightcove ids that encompasses the videos in a playlist
     * @param videos the actual video objects for a given playlist
     * @param thumbnailUrl the thumbnail image attached to the playlist
     * @param filterTags the set of tags that a playlist has been filtered by if it's a smart playlist
     * @param playlistType the type of playlist it is -- manual or automatic
     * @param accountId the account id of this playlist from Brightcove
     * 
     */
    public BrightcovePlaylist( Long id, String referenceId, String name, String shortDescription, 
                               List<Long> videoIds, List<BrightcoveVideo> videos, String thumbnailUrl,
                               List<String> filterTags, PlaylistType playlistType, long accountId ) {
        
        this.id = id;
        this.referenceId = referenceId;
        this.name = name;
        this.shortDescription = shortDescription;
        this.videoIds = videoIds;
        this.videos = videos;
        this.thumbnailUrl = thumbnailUrl;
        this.filterTags = filterTags;
        this.playlistType = playlistType;
        this.accountId = accountId;    
    
    }
    
    
    /****************************** Getters ******************************/

    public long getId( ) {
        return id;
    }

    
    public String getReferenceId( ) {
        return referenceId;
    }

    
    public String getName( ) {
        return name;
    }

    
    public String getShortDescription( ) {
        return shortDescription;
    }

    
    public List<Long> getVideoIds( ) {
        return videoIds;
    }

    
    public List<BrightcoveVideo> getVideos( ) {
        return videos;
    }

    
    public String getThumbnailUrl( ) {
        return thumbnailUrl;
    }

    
    public List<String> getFilterTags( ) {
        return filterTags;
    }

    
    public PlaylistType getPlaylistType( ) {
        return playlistType;
    }

    
    public long getAccountId( ) {
        return accountId;
    }

    
}
