package com.brightcove.consulting.api.request;


public class PlaylistPageRequest extends BrightcovePageRequest {

	private String playerId;
	
    public PlaylistPageRequest(int page, int size) {
        super(page, size);
    }
    
    public PlaylistPageRequest(int page, int size, String playerId) {
        super(page, size);
        
        if (playerId == null || playerId.length() < 1 ) {
            throw new IllegalArgumentException( "The player id must be populated!" );
        }
        
        this.playerId = playerId;
    }
    
    public String getPlayerId(){
		return this.playerId;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((playerId == null) ? 0 : playerId.hashCode());
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
        PlaylistPageRequest other = (PlaylistPageRequest) obj;
        if (playerId == null) {
            if (other.playerId != null)
                return false;
        } else if (!playerId.equals(other.playerId))
            return false;
        return true;
    }

}
