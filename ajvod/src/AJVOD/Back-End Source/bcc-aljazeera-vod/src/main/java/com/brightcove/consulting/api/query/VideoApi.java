package com.brightcove.consulting.api.query;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoFieldEnum;
import com.brightcove.consulting.api.models.BrightcoveApiFields;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.ItemCollection;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;
import com.brightcove.mediaapi.wrapper.ReadApi;

/**
 * 
 * The VideoApi abstract class is an abstract class that encapsulates common functionality for creating any subset of
 * Video api calls to Brightcove. The class has multiple constructors for handling different types of api requests from
 * including just the readToken to including more information such as the sort order or logging capabilities. 
 * 
 * VideoApi also encapsulates functionality to convert all of the api responses from a 3rd party library into a standard
 * "Pages" Video object {@link BrightcoveVideo }. This is done to promote decoupling of the application from underlying
 * 3rd party requirements and to create a more flexible abstract class to build upon for future api calls.
 * 
 * @author woladehin
 *
 * @param <T> The type of item collection to return, in this case a BrightcoveVideo
 * @param <S> The type of requests this application will take and parse
 * 
 * @see BrightcovePageRequest, ItemCollection
 * 
 */
public abstract class VideoApi<T extends ItemCollection, S extends BrightcovePageRequest > implements MediaApi<T,S> {
    
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
    public VideoApi( String readToken ) {
        
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
    public VideoApi( String readToken, Logger apiLogger ) {
        
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
    public VideoApi( String readToken, Sort sort ) {
        
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
    public VideoApi( String readToken, Sort sort, Logger apiLogger ) {
        
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
    public VideoApi( String readToken, BrightcoveApiFields brightcoveApiFields ) {
    
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
    public VideoApi( String readToken, BrightcoveApiFields brightcoveApiFields, Logger apiLogger ) {
        
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
    public VideoApi( String readToken , BrightcoveApiFields brightcoveApiFields , 
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
    public VideoApi( String readToken , BrightcoveApiFields brightcoveApiFields , 
            Sort sort, Logger apiLogger ) {

        this.readToken = readToken;
        this.brightcoveApiFields = brightcoveApiFields;
        this.sort = sort;
        this.readApi = new ReadApi(apiLogger, UTF_ENCODING);
        
    }
    
    
    /**
     * 
     * Every api subclass must implement a query to Brightcove. The query must be able to
     * take a request object {@link BrightcovePageRequest} and based on the constructor used
     * be able to create an api call out to Brightcove REST services. After retrieving the call
     * the query must return a response object, {@link BrighcoveResponse}.
     * 
     * @param S brightcove page Response
     * 
     * @return a Brightcove Response from the REST api service
     * 
     */
    public BrightcoveResponse<T> query( S brightcovePageRequest ) {        
        int size = brightcovePageRequest.getPageSize( );
        int page = brightcovePageRequest.getPageNumber( );
        
        SortByTypeEnum sortBy = convertSortByTypeEnum( brightcovePageRequest.getSort( ) );
        SortOrderTypeEnum sortOrder = convertSortOrderTypeEnum( brightcovePageRequest.getSort( ) );

        return executeQuery(brightcovePageRequest, size, page, sortBy, sortOrder);
        
    }

    protected abstract BrightcoveResponse<T> executeQuery( S brightcovePageRequest, int size,
    								int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder );

    /**
     * 
     * Take a set of Videos from the Brightcove Mapi Wrapper and converts them to a friendly
     * BrightcoveVideo. This conversion is required to link all videos to the rest of the framework.
     * This method is used as a template for a base BrightcoveVideo but can be overridden or
     * extended in subclasses to provide the right Video DTO for a given response.
     * 
     * @param videoList list of videos from the Brightcove api
     * @param page the page that was requested from the api
     * 
     * @return a formatted BrightcoveResponse with Brightcove videos
     * 
     */
    protected BrightcoveResponse<BrightcoveVideo> convertToBrightcoveResponse( 
            Videos videoList, int page, SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder, S brightcovePageRequest ) {
        
        List<BrightcoveVideo> brightcoveItemCollection = new ArrayList< BrightcoveVideo > ( );
        
        for (Video video : videoList) {
        	brightcoveItemCollection.add( new BrightcoveVideo(video) );
        }
                
        return new VideoResponse( videoList.getTotalCount( ), page, brightcoveItemCollection.size( ), 
                                  brightcoveItemCollection );
       
    }
   
    /**
     * 
     * Take a set of Videos from the Brightcove Mapi Wrapper and converts them to a friendly
     * BrightcoveVideo. This conversion is required to link all videos to the rest of the framework.
     * This method is used as a template for a base BrightcoveVideo but can be overridden or
     * extended in subclasses to provide the right Video DTO for a given response.
     * 
     * @param videoList list of videos from the Brightcove api
     * @param page the page that was requested from the api
     * 
     * @return a formatted BrightcoveResponse with Brightcove videos
     * 
     */
    protected BrightcoveResponse<BrightcoveVideo> convertToBrightcoveResponse( Video video, int page ) {
        
        List<BrightcoveVideo> brightcoveItemCollection = new ArrayList< BrightcoveVideo > ( );   

    	brightcoveItemCollection.add( new BrightcoveVideo(video) );
                
        return new VideoResponse( 1, page, brightcoveItemCollection.size( ), 
                                  brightcoveItemCollection );
       
    }

    /**
     * 
     * Take a set of Videos from the Brightcove Mapi Wrapper and converts them to a friendly
     * BrightcoveVideo. This conversion is required to link all videos to the rest of the framework.
     * This method is used as a template for a base BrightcoveVideo but can be overridden or
     * extended in subclasses to provide the right Video DTO for a given response.
     * 
     * @param videoList list of videos from the Brightcove api
     * @param page the page that was requested from the api
     * 
     * @return a formatted BrightcoveResponse with Brightcove videos
     * 
     */
    protected BrightcoveResponse<BrightcoveVideo> convertToBrightcoveResponse( Videos videoList, int page ) {
        return convertToBrightcoveResponse(videoList, page, videoList.getTotalCount());
    }

    protected BrightcoveResponse<BrightcoveVideo> convertToBrightcoveResponse( List<Video> videoList, int page ) {
        
        return convertToBrightcoveResponse(videoList, page, videoList.size());
        
    }

    protected BrightcoveResponse<BrightcoveVideo> convertToBrightcoveResponse(List<Video> videoList, int page, int totalResults) {
        List<BrightcoveVideo> brightcoveItemCollection = new ArrayList< BrightcoveVideo > ( );
        
        for (Video video : videoList) {
            brightcoveItemCollection.add(new BrightcoveVideo(video));
        }
        
        return new VideoResponse( totalResults, page, brightcoveItemCollection.size( ), 
                brightcoveItemCollection );
        
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
                return SortByTypeEnum.REFERENCE_ID;
            default:
                return SortByTypeEnum.CREATION_DATE;                
        }
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
     * Getter for the api fields requested for a video
     *
     */
    protected BrightcoveApiFields getBrightcoveApiFields( ) {
    
        return brightcoveApiFields;
    
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
