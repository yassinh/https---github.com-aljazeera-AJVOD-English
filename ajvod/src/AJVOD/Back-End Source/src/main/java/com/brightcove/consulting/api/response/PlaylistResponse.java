package com.brightcove.consulting.api.response;

import java.util.List;

import com.brightcove.consulting.api.models.BrightcovePlaylist;

public class PlaylistResponse implements BrightcoveResponse<BrightcovePlaylist> {

	private long totalCount;
    private int pageNumber;
    private int pageSize;
    private List<BrightcovePlaylist> brightcoveItemCollection;
    
    public PlaylistResponse( long totalCount, int pageNumber, int pageSize, List<BrightcovePlaylist> brightcoveItemCollection ) {
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.brightcoveItemCollection = brightcoveItemCollection;
    }
    
    public long getTotalCount() {
        return this.totalCount;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public List<BrightcovePlaylist> getBrightcoveItemCollection() {
        return this.brightcoveItemCollection;
    }

}
