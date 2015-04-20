package com.brightcove.consulting.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcovePlaylist;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.Sort;
import com.brightcove.consulting.api.models.Sort.Direction;
import com.brightcove.consulting.api.models.Sort.SortProperty;
import com.brightcove.consulting.api.query.FindAllVideos;
import com.brightcove.consulting.api.query.FindPlaylistsByPlayer;
import com.brightcove.consulting.api.query.FindPlaylistsByPlayerId;
import com.brightcove.consulting.api.query.FindRelatedVideos;
import com.brightcove.consulting.api.query.FindVideoById;
import com.brightcove.consulting.api.query.FindVideosByIds;
import com.brightcove.consulting.api.query.FindVideosByPlaylist;
import com.brightcove.consulting.api.query.FindVideosBySearchFields;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.request.BrightcoveVideoRequest;
import com.brightcove.consulting.api.request.BrightcoveVideosRequest;
import com.brightcove.consulting.api.request.PlaylistPageRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;

/**
 * A service for executing various read api requests.
 *
 * @author ssayles
 */
@Component
public class BrightcoveReadApiService {

    @Autowired
    private String readToken;
    
    @Cacheable("findVideosBySearchFields")
    public BrightcoveResponse<BrightcoveVideo> findVideosBySearchFields(SearchApiPageRequest brightcovePageRequest) {
        FindVideosBySearchFields search = new FindVideosBySearchFields(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        VideoResponse vr = new VideoResponse(result.getTotalCount(), result.getPageNumber(), result.getPageSize(), result.getBrightcoveItemCollection(),
        		convertSortByTypeEnum(brightcovePageRequest.getSort()), convertSortOrderTypeEnum(brightcovePageRequest.getSort()), brightcovePageRequest);
        return vr;
    }

    @Cacheable("findAllVideos")
    public BrightcoveResponse<BrightcoveVideo> findAllVideos(BrightcovePageRequest brightcovePageRequest) {
        FindAllVideos search = new FindAllVideos(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        return result;
    }

    @Cacheable("findVideosByIds")
    public BrightcoveResponse<BrightcoveVideo> findVideosByIds(BrightcoveVideosRequest brightcovePageRequest) {
        FindVideosByIds search = new FindVideosByIds(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        return result;
    }
    
    @Cacheable("findVideosByPlaylist")
    public BrightcoveResponse<BrightcoveVideo> findVideosByPlaylist(BrightcoveVideoRequest brightcovePageRequest) {
        FindVideosByPlaylist search = new FindVideosByPlaylist(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        return result;
    }
    
    @Cacheable("findVideoById")
    public BrightcoveResponse<BrightcoveVideo> findVideoById(BrightcoveVideoRequest brightcovePageRequest) {
        FindVideoById search = new FindVideoById(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        return result;
    }

    @Cacheable("findPlaylistsByPlayer")
    public BrightcoveResponse<BrightcovePlaylist> findPlaylistsByPlayer(PlaylistPageRequest playlistPageRequest) {
        FindPlaylistsByPlayer search = new FindPlaylistsByPlayer(readToken);
        BrightcoveResponse<BrightcovePlaylist> result = search.query(playlistPageRequest);
        return result;
        
    }
    
    @Cacheable("findPlaylistsByPlayerId")
    public BrightcoveResponse<BrightcovePlaylist> findPlaylistsByPlayerId(PlaylistPageRequest playlistPageRequest) {
        FindPlaylistsByPlayerId search = new FindPlaylistsByPlayerId(readToken);
        BrightcoveResponse<BrightcovePlaylist> result = search.query(playlistPageRequest);
        return result;
        
    }
    
    @Cacheable("findRelatedVideos")
    public BrightcoveResponse<BrightcoveVideo> findRelatedVideos(BrightcoveVideoRequest brightcovePageRequest) {
        FindRelatedVideos search = new FindRelatedVideos(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(brightcovePageRequest);
        return result;
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
     * hossam modification on 14-11 creation date instead od modified date.
     */
    protected SortByTypeEnum convertSortByTypeEnum(Sort sort) {
        
        SortProperty sortProperty = sort.getSortProperty();
        switch (sortProperty) {
        
            case DISPLAY_NAME:
                return SortByTypeEnum.CREATION_DATE;
            case REFERENCE_ID:
                return SortByTypeEnum.CREATION_DATE;
            case PLAYS_TOTAL:
                return SortByTypeEnum.CREATION_DATE;
            case PLAYS_TRAILING_WEEK:
                return SortByTypeEnum.PLAYS_TRAILING_WEEK;
            case START_DATE:
                return SortByTypeEnum.CREATION_DATE;
            case PUBLISH_DATE:
                return SortByTypeEnum.CREATION_DATE;
            case CREATION_DATE:
                return SortByTypeEnum.CREATION_DATE;
            case MODIFIED_DATE:
                return SortByTypeEnum.CREATION_DATE;
            case NATURAL:
                return SortByTypeEnum.CREATION_DATE;
            default:
                return SortByTypeEnum.CREATION_DATE;                
        }
    }
}
