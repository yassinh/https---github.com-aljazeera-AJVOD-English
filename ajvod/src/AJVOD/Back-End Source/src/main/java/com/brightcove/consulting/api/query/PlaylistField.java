package com.brightcove.consulting.api.query;

/**
 * 
 * Lists of all the types of fields that can be returned in a playlist object. 
 * Used for any of the Playlist Api calls to determine how the response from
 * Brightcove should be formatted.
 * 
 * @author woladehin
 *
 */
public enum PlaylistField {

    ID, REFERENCE_ID, NAME, SHORT_DESCRIPTION, VIDEO_IDS, VIDEOS, THUMBNAIL_URL,
    FILTER_TAGS, PLAYLIST_TYPE, ACCOUNT_ID
}
