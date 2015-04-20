package com.brightcove.consulting.api.request;

import java.util.Set;

import com.brightcove.consulting.api.models.Sort;

public class BrightcoveVideosRequest extends BrightcovePageRequest {

    private Set<String> videoIds;

	public BrightcoveVideosRequest( int page, int size, Sort sort, Set<String> videoIds) {
        super(page, size, sort);
        
        if (videoIds == null || videoIds.isEmpty()) {
            throw new IllegalArgumentException( "videoIds must be populated" );
        }

        this.videoIds = videoIds;
    }

	public Set<String> getVideoIds() {
		return videoIds;
	}

	public void setVideoIds(Set<String> videoIds) {
		this.videoIds = videoIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((videoIds == null) ? 0 : videoIds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BrightcoveVideosRequest other = (BrightcoveVideosRequest) obj;
		if (videoIds == null) {
			if (other.videoIds != null)
				return false;
		} else if (!videoIds.equals(other.videoIds))
			return false;
		return true;
	}

	
}
