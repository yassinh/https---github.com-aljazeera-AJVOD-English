package com.brightcove.consulting.api.query;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Playlist;
import com.brightcove.commons.catalog.objects.Playlists;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.enumerations.PlaylistFieldEnum;
import com.brightcove.commons.catalog.objects.enumerations.PlaylistTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoFieldEnum;
import com.brightcove.consulting.api.models.BrightcoveApiFields;
import com.brightcove.consulting.api.models.BrightcovePlaylist;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.ItemCollection;
import com.brightcove.consulting.api.models.PlaylistType;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.request.PlaylistPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.PlaylistResponse;
import com.brightcove.mediaapi.wrapper.ReadApi;

/**
 * 
 * Implementation of the interface {@link MediaApi} for Brightcove playlist requests. 
 * This abstract class when extended will support returning a set of playlists as a {@BrightcovePlaylist}
 * based on a playlist request {@link PlaylistPageRequest}. 
 *
 * @see         PlaylistPageRequest, MediaApi
 *
 */
public abstract class PlaylistApi<T extends ItemCollection, S extends BrightcovePageRequest> implements MediaApi< T,S > {

    EnumSet< PlaylistField > playlistFields =EnumSet.noneOf( PlaylistField.class );
    private BrightcoveApiFields brightcoveApiFields;
    private String readToken;
    private Sort sort;
    private static String UTF_ENCODING = "UTF-8";
    private ReadApi readApi;

    /**
     * 
     * Default constructor
     * 
     * @param readToken read token for a specific Brightcove account
     * 
     */
    public PlaylistApi( String readToken ) {
        
        this.readToken = readToken;
        this.sort = new Sort(Direction.NATURAL,SortProperty.MODIFIED_DATE);  
        this.brightcoveApiFields = new BrightcoveApiFields( );
        this.readApi = new ReadApi(UTF_ENCODING);

    }

    
    /**
     * 
     * Constructor that enables logging for the Brightcove account linked
     * to the specific readToken
     * 
     * @param readToken read token for a specific Brightcove account 
     * @param apiLogger the logger for this request
     * 
     */
    public PlaylistApi( String readToken, Logger apiLogger ) {
        
        this.readToken = readToken;
        this.sort = new Sort(Direction.NATURAL,SortProperty.MODIFIED_DATE);  
        this.brightcoveApiFields = new BrightcoveApiFields( );
        this.readApi = new ReadApi(apiLogger, UTF_ENCODING);

    }
    
    /**
     * 
     * Constructor that enables specific sort criteria for a Brightcove request linked
     * to a given account via the read token
     * 
     * @param readToken read token for a specific Brightcove account
     * @param sort the sort criteria applied for this request
     * 
     */
    public PlaylistApi( String readToken, Sort sort ) {
        
        this.readToken = readToken;
        this.sort = sort;
        this.brightcoveApiFields = new BrightcoveApiFields( );
        this.readApi = new ReadApi(UTF_ENCODING);
    
    }

    /**
     *
     * Constructor that enables sorting and logging for a specific Brightcove account
     * using the account's read token
     * 
     * @param readToken read token for a specific Brightcove account
     * @param sort the sort criteria applied for this request
     * @param apiLogger the logger for this request
     *
     */
    public PlaylistApi( String readToken, Sort sort, Logger apiLogger ) {
        
        this.readToken = readToken;
        this.sort = sort;
        this.brightcoveApiFields = new BrightcoveApiFields( );
        this.readApi = new ReadApi( apiLogger, UTF_ENCODING );

    }
 
    
    /**
     * 
     * Constructor that enables retrieving specific Brightcove fields from an 
     * api requests for a given account's read token. 
     * 
     * @param readToken read token for a specific Brightcove account
     * @param brightcoveApiFields the api fields on the video response
     * 
     */
    public PlaylistApi( String readToken, BrightcoveApiFields brightcoveApiFields ) {
    
        this.readToken = readToken;
        this.brightcoveApiFields = brightcoveApiFields;
        this.sort = new Sort( Direction.NATURAL, SortProperty.MODIFIED_DATE ); 
        this.readApi = new ReadApi( UTF_ENCODING );

    }
    
    
    /**
     * 
     * Constructor that enables retrieving specific Brightcove fields from an api request along with
     * logging of the requests for a given account's read token.
     * 
     * @param readToken read token for a specific Brightcove account
     * @param brightcoveApiFields the api fields on the video response
     * @param apiLogger the logger for this request
     * 
     */
    public PlaylistApi( String readToken, BrightcoveApiFields brightcoveApiFields, Logger apiLogger ) {
        
        this.readToken = readToken;
        this.brightcoveApiFields = brightcoveApiFields;
        this.sort = new Sort( Direction.NATURAL, SortProperty.MODIFIED_DATE ); 
        this.readApi = new ReadApi( apiLogger, UTF_ENCODING );

    }
    
    
    /**
     * 
     * Constructor that enables retrieving specific Brightcove fields from an api request
     * along with specifying the sort order that should be applied to the videos. The
     * videos queried against will be against the given account's read token.
     * 
     * @param readToken read token for a specific Brightcove account
     * @param brightcoveApiFields the api fields on the video response
     * @param sort the sort criteria for the request
     *
     */
    public PlaylistApi( String readToken , BrightcoveApiFields brightcoveApiFields , 
                     Sort sort ) {
    
        this.readToken = readToken;
        this.brightcoveApiFields = brightcoveApiFields;
        this.sort = sort;
        this.readApi = new ReadApi(UTF_ENCODING);

    }
    
    
    /**
     *
     * Constructor that enables retrieving specific Brihtcove fields from an api request along with
     * specifying the sort order and the ability to log the request. The api call is made against the
     * account linked to the specific read token.
     * 
     * @param readToken read token for a specific Brightcove account
     * @param brightcoveApiFields the api fields on the video response
     * @param sort the sort criteria for the request
     * @param apiLogger the logger for this request
     *
     */
    public PlaylistApi( String readToken , BrightcoveApiFields brightcoveApiFields , 
            Sort sort, Logger apiLogger ) {

        this.readToken = readToken;
        this.brightcoveApiFields = brightcoveApiFields;
        this.sort = sort;
        this.readApi = new ReadApi(apiLogger, UTF_ENCODING);
        
    }
    
    public BrightcoveResponse<BrightcovePlaylist> query( PlaylistPageRequest playlistPageRequest ) {        
        int size = playlistPageRequest.getPageSize( );
        int page = playlistPageRequest.getPageNumber( );
        
        SortByTypeEnum sortBy = convertSortByTypeEnum( playlistPageRequest.getSort( ) );
        SortOrderTypeEnum sortOrder = convertSortOrderTypeEnum( playlistPageRequest.getSort( ) );

        return executeQuery(playlistPageRequest, size, page, sortBy, sortOrder);
        
    }

    protected abstract BrightcoveResponse<BrightcovePlaylist> executeQuery( PlaylistPageRequest brightcovePageRequest, int size,
    								int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder );
    
    protected BrightcoveResponse<BrightcovePlaylist> convertToBrightcoveResponse( Playlists playlists, int page ) {
        
        List<BrightcovePlaylist> brightcoveItemCollection = new ArrayList< BrightcovePlaylist > ( );
        
        for (Playlist playlist : playlists) {

        	brightcoveItemCollection.add( new BrightcovePlaylist(playlist.getId( ), playlist.getReferenceId( ), playlist.getName(), 
                                          playlist.getShortDescription( ), playlist.getVideoIds(), 
                                          convertToBrightcoveVideos(playlist.getVideos()), playlist.getThumbnailUrl(), playlist.getFilterTags(), 
                                          convertPlaylistTypeEnum(playlist.getPlaylistType()), playlist.getAccountId() ) );
        }
                
        return new PlaylistResponse( playlists.getTotalCount( ), page, brightcoveItemCollection.size( ), 
                                  brightcoveItemCollection );
       
    }
    
    private PlaylistType convertPlaylistTypeEnum(PlaylistTypeEnum playlistTypeEnum){
    	switch(playlistTypeEnum){
    		case ALPHABETICAL:
    			return PlaylistType.ALPHABETICAL;
    		
    		case NEWEST_TO_OLDEST:
    			return PlaylistType.NEWEST_TO_OLDEST;
    		
    		case OLDEST_TO_NEWEST:
    			return PlaylistType.OLDEST_TO_NEWEST;
    			
    		case PLAYS_TOTAL:
    			return PlaylistType.PLAYS_TOTAL;
    			
    		case PLAYS_TRAILING_WEEK:
    			return PlaylistType.PLAYS_TRAILING_WEEK;
    			
    		default:
    			return PlaylistType.EXPLICIT;
    	}

    }
    
    private List<BrightcoveVideo> convertToBrightcoveVideos(List<Video> videoList){
    	if(videoList == null)
    		return new ArrayList< BrightcoveVideo > ( );
    	
    	List<BrightcoveVideo> brightcoveItemCollection = new ArrayList< BrightcoveVideo > ( );
        
        for (Video video : videoList) {
        	// Total plays can be null
        	Integer plays = video.getPlaysTotal();
        	int playsSafe = plays != null ? plays.intValue() : 0;

        	brightcoveItemCollection.add( new BrightcoveVideo(video.getId( ), video.getReferenceId( ), 
                                          video.getShortDescription( ), video.getLongDescription( ), 
                                          video.getCreationDate( ), video.getLinkUrl( ), video.getLinkText( ), 
                                          video.getVideoStillUrl( ), video.getLength( ), playsSafe,
                                          video.getName( ), video.getCustomFields() ) );
        }
        
        return brightcoveItemCollection;
    }
    
    /**
     * 
     * Helper method that abstracts the VideoApi from the default wrapper. 
     * 
     * @return a string of custom fields used in an api request to Brightcove.
     *
     */
     protected Set<String> convertCustomFields( ) {
    
        return this.getBrightcoveApiFields( ).getCustomFields( );
    
    }
  
    /**
     * 
     * Helper method that converts the internal pages video fields to the Brightcove default
     * wrapper video fields used for api requests
     * 
     * @return the enumset required by the MAPI wrapper to handle Brightcove videos
     *
     */
    protected EnumSet<VideoFieldEnum> convertVideoFields( ) {
        
        EnumSet<VideoField> yourBrandVideoFields = this.getBrightcoveApiFields( ).getVideoFields( );
        EnumSet<VideoFieldEnum> brightcoveApiVideoFields = EnumSet.noneOf( VideoFieldEnum.class );
        
        for( VideoField videoField : yourBrandVideoFields ) {
            
            VideoFieldEnum brightcoveApiVideoFieldEnum = convertToVideoFieldEnum( videoField );
            brightcoveApiVideoFields.add( brightcoveApiVideoFieldEnum );
         
        }
        
        return brightcoveApiVideoFields;
        
    }
    
    /**
     * 
     * Helper method that converts the internal pages playlist fields to the Brightcove default
     * wrapper playlist fields used for api requests
     * 
     * @return the enumset required by the MAPI wrapper to handle Brightcove videos
     *
     */
    protected EnumSet<PlaylistFieldEnum> convertPlaylistFields( ) {
        
        EnumSet<PlaylistField> yourBrandVideoFields = playlistFields;
        EnumSet<PlaylistFieldEnum> brightcoveApiVideoFields = EnumSet.noneOf( PlaylistFieldEnum.class );
        
        for( PlaylistField videoField : yourBrandVideoFields ) {
            
        	PlaylistFieldEnum brightcoveApiVideoFieldEnum = convertToPlaylistFieldEnum( videoField );
            brightcoveApiVideoFields.add( brightcoveApiVideoFieldEnum );
         
        }
        
        return brightcoveApiVideoFields;
        
    }
    
    /**
     * 
     * Converts the Pages/YBTV version of the video fields to the underlying data service 
     * video fields. This allows for encapsulating the 3rd party wrapper into one object in the
     * event a different data layer is required.
     * 
     * @param videoField the standard Pages/YBTV video field
     * 
     * @return the videofieldEnum linked to the MAPI wrapper
     * 
     */
    protected VideoFieldEnum convertToVideoFieldEnum( VideoField videoField ) {
        
        switch ( videoField ) {

            case ID:
                return VideoFieldEnum.ID;
            case NAME:
                return VideoFieldEnum.NAME;
            case SHORT_DESCRIPTION:
                return VideoFieldEnum.SHORTDESCRIPTION;
            case LONG_DESCRIPTION:
                return VideoFieldEnum.LONGDESCRIPTION;
            case CREATION_DATE:
                return VideoFieldEnum.CREATIONDATE;
            case PUBLISHED_DATE:
                return VideoFieldEnum.PUBLISHEDDATE;
            case LAST_MODIFIED_DATE:
                return VideoFieldEnum.LASTMODIFIEDDATE;
            case START_DATE:
                return VideoFieldEnum.STARTDATE;
            case END_DATE:
                return VideoFieldEnum.ENDDATE;
            case LINK_URL:
                return VideoFieldEnum.LINKURL;
            case LINK_TEXT:
                return VideoFieldEnum.LINKTEXT;
            case TAGS:
                return VideoFieldEnum.TAGS;
            case VIDEO_STILL_URL:
                return VideoFieldEnum.VIDEOSTILLURL;
            case THUMBNAIL_URL:
                return VideoFieldEnum.THUMBNAILURL;
            case REFERENCE_ID:
                return VideoFieldEnum.REFERENCEID;
            case LENGTH:
                return VideoFieldEnum.LENGTH;
            case ECONOMICS:
                return VideoFieldEnum.ECONOMICS;
            case ITEM_STATE:
                return VideoFieldEnum.ITEMSTATE;
            case PLAYS_TOTAL:
                return VideoFieldEnum.PLAYSTOTAL;
            case PLAYS_TRAILING_WEEK:
                return VideoFieldEnum.PLAYSTRAILINGWEEK;
            case VERSION:
                return VideoFieldEnum.VERSION;
            case CUE_POINTS:
                return VideoFieldEnum.CUEPOINTS;
            case SUBMISSION_INFO:
                return VideoFieldEnum.SUBMISSIONINFO;    
            case CUSTOM_FIELDS:
                return VideoFieldEnum.CUSTOMFIELDS;
            case RELEASE_DATE:
                return VideoFieldEnum.RELEASEDATE;
            case FLV_URL:
                return VideoFieldEnum.FLVURL;
            case IOS_RENDITIONS:
                return VideoFieldEnum.IOSRENDITIONS;
            case RENDITIONS:
                return VideoFieldEnum.RENDITIONS;
            case GEO_FILTERED:
                return VideoFieldEnum.GEOFILTERED;
            case GEO_FILTER_EXCLUDE:
                return VideoFieldEnum.GEOFILTEREXCLUDE;
            case EXCLUDE_LIST_COUNTRIES:
                return VideoFieldEnum.EXCLUDELISTEDCOUNTRIES;
            case GEO_FILTERED_COUNTRIES:
                return VideoFieldEnum.GEOFILTEREDCOUNTRIES;
            case ALLOWED_COUNTRIES:
                return VideoFieldEnum.ALLOWEDCOUNTRIES;
            case ACCOUNT_ID:
                return VideoFieldEnum.ACCOUNTID;
            case FLV_FULL_LENGTH:
                return VideoFieldEnum.FLVFULLLENGTH;
            case VIDEO_FULL_LENGTH:
                return VideoFieldEnum.VIDEOFULLLENGTH;
            default:
                return null;
        }
    }

    /**
     * 
     * Converts the Pages/YBTV version of the playlist fields to the underlying data service 
     * playlist fields. This allows for encapsulating the 3rd party wrapper into one object in the
     * event a different data layer is required.
     * 
     * @param playlistField the standard Pages/YBTV video field
     * 
     * @return the videofieldEnum linked to the MAPI wrapper
     * 
     */
    protected PlaylistFieldEnum convertToPlaylistFieldEnum( PlaylistField playlistField ) {
        
        switch ( playlistField ) {

            case ID:
                return PlaylistFieldEnum.ID;
            case NAME:
                return PlaylistFieldEnum.NAME;
            case SHORT_DESCRIPTION:
                return PlaylistFieldEnum.SHORTDESCRIPTION;
            case ACCOUNT_ID:
                return PlaylistFieldEnum.ACCOUNTID;
            case FILTER_TAGS:
                return PlaylistFieldEnum.FILTERTAGS;
            case PLAYLIST_TYPE:
                return PlaylistFieldEnum.PLAYLISTTYPE;
            case REFERENCE_ID:
                return PlaylistFieldEnum.REFERENCEID;
            case THUMBNAIL_URL:
                return PlaylistFieldEnum.THUMBNAILURL;
            case VIDEO_IDS:
                return PlaylistFieldEnum.VIDEOIDS;
            case VIDEOS:
                return PlaylistFieldEnum.VIDEOS;
            default:
                return null;
        }
    }

    
    /**
     * 
     * Converts the sort object into a Brightcove MAPI wrapper compliant sortOrderTypeEnum.
     * 
     * If we are unable to find the sort type we default to the SortOrderTypeEnum to be descending.
     * 
     * @param sort the sort property for a video api request
     * 
     * @return the sortOrderTypeEnum object linked to the MAPI wrapper
     *
     */
    protected SortOrderTypeEnum convertSortOrderTypeEnum( Sort sort ) {
     
        Direction sortDirection = sort.getDirection();
        
        switch (sortDirection) {
            case ASC:
                return SortOrderTypeEnum.ASC;
            case DESC:
                return SortOrderTypeEnum.DESC;
            case NATURAL:
                return SortOrderTypeEnum.DESC;
            default:
                return SortOrderTypeEnum.DESC;                
        }
        
    }

    
    /**
     * 
     * Converts the sort object into a Brightcove MAPI wrapper compliant sortByTypeEnum.
     * 
     * If we are unable to find the sort type we default to the SortByTypeEnum to be creation date.
     * 
     * @param sort the sort property for a video api request
     * 
     * @return the sortByTypeEnum object linked to the MAPI wrapper
     *
     */
    protected SortByTypeEnum convertSortByTypeEnum(Sort sort) {
        
        SortProperty sortProperty = sort.getSortProperty();
        switch (sortProperty) {
        
            case DISPLAY_NAME:
                return SortByTypeEnum.DISPLAY_NAME;
            case REFERENCE_ID:
                return SortByTypeEnum.REFERENCE_ID;
            case PLAYS_TOTAL:
                return SortByTypeEnum.PLAYS_TOTAL;
            case PLAYS_TRAILING_WEEK:
                return SortByTypeEnum.PLAYS_TRAILING_WEEK;
            case START_DATE:
                return SortByTypeEnum.START_DATE;
            case PUBLISH_DATE:
                return SortByTypeEnum.PUBLISH_DATE;
            case CREATION_DATE:
                return SortByTypeEnum.CREATION_DATE;
            case MODIFIED_DATE:
                return SortByTypeEnum.MODIFIED_DATE;
            case NATURAL:
                return SortByTypeEnum.CREATION_DATE;
            default:
                return SortByTypeEnum.CREATION_DATE;                
        }
    }
    

    /**
     * 
     * Getter for the api fields requested for a video
     *
     */
    protected BrightcoveApiFields getBrightcoveApiFields( ) {
    
        return brightcoveApiFields;
    
    }

    /**
    *
    * Getter for the read token
    *
    */
   protected String getReadToken( ) {
   
       return readToken;
   
   }
   
   /**
    * 
    * Getter for the sort property
    *
    */
   protected Sort getSort( ) {
   
       return sort;
   
   }
   
    /**
     * 
     * Getter for the read api
     * 
     */
    protected ReadApi getReadApi() {
    
        return readApi;
    
    }
}
