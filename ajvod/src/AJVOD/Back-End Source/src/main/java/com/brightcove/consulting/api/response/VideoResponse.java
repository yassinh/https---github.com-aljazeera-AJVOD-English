package com.brightcove.consulting.api.response;

import java.util.List;

import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.request.BrightcovePageRequest;

public class VideoResponse implements BrightcoveResponse<BrightcoveVideo> {
    
    private long totalCount;
    private int pageNumber;
    private int pageSize;
    private List<BrightcoveVideo> brightcoveItemCollection;

    private SortByTypeEnum sortBy;
    private SortOrderTypeEnum sortOrder;
    private BrightcovePageRequest brightcovePageRequest;

    public VideoResponse( long totalCount, int pageNumber, int pageSize, List<BrightcoveVideo> brightcoveItemCollection,
            SortByTypeEnum sortBy, SortOrderTypeEnum sortOrder, BrightcovePageRequest brightcovePageRequest) {
        this(totalCount, pageNumber, pageSize, brightcoveItemCollection);
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.brightcovePageRequest = brightcovePageRequest;
    }

    public VideoResponse( long totalCount, int pageNumber, int pageSize, List<BrightcoveVideo> brightcoveItemCollection ) {
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.brightcoveItemCollection = brightcoveItemCollection;
    }

    public long getTotalCount( ) {
        return this.totalCount;
    }

    public int getPageNumber( ) {
        return this.pageNumber;
    }

    public int getPageSize( ) {
        return this.pageSize;
    }

    public List< BrightcoveVideo > getBrightcoveItemCollection( ) {
        return this.brightcoveItemCollection;
    }
   
    public SortByTypeEnum getSortBy() {
        return sortBy;
    }

    public SortOrderTypeEnum getSortOrder() {
        return sortOrder;
    }

    public BrightcovePageRequest getBrightcovePageRequest() {
        return brightcovePageRequest;
    }

    // return Query
    // return String getQueryJson
}
